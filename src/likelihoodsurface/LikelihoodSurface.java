package likelihoodsurface;

import beast.core.*;
import beast.core.Runnable;
import beast.core.util.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dlouis on 06/05/16.
 *
 * Evaluate pre-specified points on the likelihood surface
 *
 * As with MCMC the state specifies all the parameters that are to be modified
 *
 * Gridders can be used to either specify discrete states that should be evaluated for a given parameter (using values
 * input), or a sequence of states using (for:by:to input). Every combination of gridder values are then evaluated
 * (careful about adding too many gridders, the recursion will become too big and the result will have too many
 * dimensions to analyze!)
 *
 * Values are logged into log files or the screen exactly as with MCMC
 *
 * Every parameter in the state should be associated with only one Gridder.
 *
 * For now only Real parameters can be gridded. (Integer parameters should work too, but haven't been tested).
 * Boolean and Integer parameters will be easy to add later.
 * Gridding trees should be easy enough in principle, but would need a list of TreeParsers. It would be more elegant to
 * simply specify a Nexus/Newick file and read in trees from there.
 *
 */
public class LikelihoodSurface extends Runnable {


    final public Input<State> startStateInput =
            new Input<>("state", "elements of the state space", Input.Validate.REQUIRED);

    final public Input<Distribution> posteriorInput =
            new Input<>("distribution", "probability distribution to sample over (e.g. a posterior)",
                    Input.Validate.REQUIRED);

    final public Input<List<Gridder>> griddersInput =
            new Input<>("gridder", "Specifies which points in the likelihood space to evaluate for each parameter",
                    new ArrayList<>(), Input.Validate.REQUIRED);

    final public Input<List<Logger>> loggersInput =
            new Input<>("logger", "loggers for reporting progress of MCMC chain",
                    new ArrayList<>(), Input.Validate.REQUIRED);





    /**
     * The state that takes care of managing StateNodes,
     * operations on StateNodes and propagates store/restore/requireRecalculation
     * calls to the appropriate BEASTObjects.
     */
    protected State state;
    protected List<Gridder> gridders;

    public LikelihoodSurface() {    }


    @Override
    public void initAndValidate() {
        Log.info.println("===============================================================================");
        Log.info.println("Citations for this model:");
        Log.info.println(getCitations());
        Log.info.println("===============================================================================");

        gridders = griddersInput.get();

        // State initialisation
       // final HashSet<StateNode> operatorStateNodes = new HashSet<>();
       // for (final Operator op : operatorsInput.get()) {
       //     for (final StateNode stateNode : op.listStateNodes()) {
       //         operatorStateNodes.add(stateNode);
       //     }
       // }

        this.state = startStateInput.get();

        this.state.initialise();
        this.state.setPosterior(posteriorInput.get());



        /* sanity check: all gridder state nodes should be in the state */
        boolean found;
        for (final Gridder gridder : griddersInput.get()) {
            found = false;
            for (final StateNode stateNode : state.stateNodeInput.get()) {
                if (stateNode.getID() == gridder.getParameterID()) {
                    //System.out.println(stateNode.getID()+" found!");
                    if (found) {
                        throw new RuntimeException("Gridder " + gridder.getID() + "is associated with more than one "
                                + "state node. This should not be possible!");
                    } else {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                throw new RuntimeException("Gridder " + gridder.getID() + "has no state nodes in the state. "
                        + "Each gridder should should be associated with one state node. "
                        + "Remove the gridder or add its statenode(s) to the state and/or set estimate='true'.");
            }


        }

        /* Is it necessary that each state node is gridded?
        // sanity check: all state nodes should be operated on
        for (final StateNode stateNode : stateNodes) {
            if (!operatorStateNodes.contains(stateNode)) {
                Log.warning.println("Warning: state contains a node " + stateNode.getID() + " for which there is no operator.");
            }
        }*/

    } // init

    public void log(final int sampleNr) {
        for (final Logger log : loggers) {
            log.log(sampleNr);
        }
    } // log

    public void close() {
        for (final Logger log : loggers) {
            log.close();
        }
    } // close

    protected boolean debugFlag;       // Does nothing at the moment
    protected double LogLikelihood;
    protected Distribution posterior;

    protected List<Logger> loggers;

    @Override
    public void run() throws IOException, SAXException, ParserConfigurationException {
        // set up state (again). Other beastObjects may have manipulated the
        // StateNodes, e.g. set up bounds or dimensions
        state.initAndValidate();
        // also, initialise state with the file name to store and set-up whether to resume from file
        state.setStateFileName(stateFileName);

        state.setEverythingDirty(true);
        posterior = posteriorInput.get();

        LogLikelihood = state.robustlyCalcPosterior(posterior);

        final long startTime = System.currentTimeMillis();

        state.storeCalculationNodes();

        debugFlag = Boolean.valueOf(System.getProperty("beast.debug"));

        //System.err.println("Start state:");
        //System.err.println(state.toString());

        loggers = loggersInput.get();

        // put the loggers logging to stdout at the bottom of the logger list so that screen output is tidier.
        Collections.sort(loggers, (o1, o2) -> {
            if (o1.isLoggingToStdout()) {
                return o2.isLoggingToStdout() ? 0 : 1;
            } else {
                return o2.isLoggingToStdout() ? -1 : 0;
            }
        });
        // warn if none of the loggers is to stdout, so no feedback is given on screen
        boolean hasStdOutLogger = false;
        boolean hasScreenLog = false;
        for (Logger l : loggers) {
            if (l.isLoggingToStdout()) {
                hasStdOutLogger = true;
            }
            if (l.getID() != null && l.getID().equals("screenlog")) {
                hasScreenLog = true;
            }
        }
        if (!hasStdOutLogger) {
            Log.warning.println("WARNING: If nothing seems to be happening on screen this is because none of the loggers give feedback to screen.");
            if (hasScreenLog) {
                Log.warning.println("WARNING: This happens when a filename  is specified for the 'screenlog' logger.");
                Log.warning.println("WARNING: To get feedback to screen, leave the filename for screenlog blank.");
                Log.warning.println("WARNING: Otherwise, the screenlog is saved into the specified file.");
            }
        }

        // initialises log so that log file headers are written, etc.
        for (final Logger log : loggers) {
            log.init();
        }

        int totalStates = doRecursion(0, 0);

        Log.info.println();
        //operatorSchedule.showOperatorRates(System.out);

        Log.info.println();
        final long endTime = System.currentTimeMillis();
        Log.info.println("Total calculation time: " + (endTime - startTime) / 1000.0 + " seconds");
        close();

        Log.warning.println("End likelihood: " + LogLikelihood);
        // System.err.println(state);


        state.storeToFile(totalStates);
    } // run;


    /**
     * Recurses through all states given by the gridders
     *
     * @param index
     * @param sampleNr
     * @return The number of states evaluated
     * @throws IOException
     */
    protected int doRecursion(int index, int sampleNr) throws IOException {

        Gridder gridder = griddersInput.get().get(index);
        int dim = gridder.getDimension();

        for (int i = 0; i < dim; i++) {
            gridder.setNext();

            if (index == griddersInput.get().size()-1) {
                state.acceptCalculationNodes();
                state.checkCalculationNodesDirtiness();

                LogLikelihood = posterior.calculateLogP();

                state.setEverythingDirty(false);
                log(sampleNr);

                sampleNr++;
            } else
                sampleNr = doRecursion(index+1, sampleNr);
        }
        return(sampleNr);
    }


}