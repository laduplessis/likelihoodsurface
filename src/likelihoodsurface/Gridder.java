package likelihoodsurface;

import beast.core.BEASTObject;
import beast.core.Input;
import beast.core.parameter.Parameter;
import beast.core.parameter.RealParameter;

import java.util.Arrays;

/**
 * This should be changed to an interface with a base abstract class (like Parameter)
 * Then RealGridder, IntegerGridder, BooleanGridder and TreeGridder should inherit from it.
 *
 * Created by dlouis on 09/05/16.
 */
public class Gridder extends BEASTObject {

    public final Input<Parameter> parameterInput = new Input<>("parameter", "Parameter to modify",
            Input.Validate.REQUIRED);

    public final Input<RealParameter> valuesInput = new Input<>("value", "Values to change parameter to (overrides from, to and by");

    public final Input<Double> fromInput = new Input<>("from", "Where to start gridding",
            Input.Validate.XOR, valuesInput);

    public final Input<Double> toInput = new Input<>("to", "Where to grid to",
            Input.Validate.XOR, valuesInput);

    public final Input<Double> byInput = new Input<>("by", "Step-size",
            Input.Validate.XOR, valuesInput);

    public final Input<Integer> indexInput = new Input<>("index", "Dimension of the parameter to modify",
            new Integer(0));

    double [] values;
    int index;          // Current index of the gridder
    int parameterIndex; // Index of parameter to modify

    public void initAndValidate() {

        if (valuesInput.get() == null) {

            double nrsteps = 1 + Math.abs(toInput.get()-fromInput.get())/byInput.get();

            values = new double[(int) Math.floor(nrsteps)];

            for (int i = 0; i < values.length; i++) {
                values[i] = fromInput.get()+i*byInput.get();
            }
            if (values[values.length-1] != toInput.get())
                System.out.println("Warning: 'by' not a factor of |'to' - 'from'|");


        } else {
            values = valuesInput.get().getDoubleValues();
        }
    }


    /**
     * Set parameterInput to element [index] of either values array (or sequence from:by:to)
     *
     * @param index
     */
    public void setParameter(int index) {
        parameterInput.get().setValue(this.indexInput.get(), this.getValue(index % this.getDimension()));
    }

    /**
     * parameterInput to the next entry in the values array (or sequence from:by:to)
     */
    public void setNext() {
        setParameter(index++);
    }

    public int getDimension() {
        return values.length;
    }

    public String getParameterID() {
        return parameterInput.get().getID();
    }

    public double getValue() {
        return values[0];
    }

    public double getValue(final int index) {
        return values[index];
    }

    public double getArrayValue() {
        return values[0];
    }

    public double getArrayValue(final int index) {
        return values[index];
    }

    public double[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

}
