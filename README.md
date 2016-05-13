# BEAST2 likelihood surface

Evaluate pre-specified points on the likelihood surface

- As with MCMC the state specifies all the parameters that are to be modified

- Gridders can be used to either specify discrete states that should be evaluated for a given parameter (using values input), or a sequence of states using (for:by:to input). Every combination of gridder values are then evaluated (careful about adding too many gridders, the recursion will become too big and the result will have too many dimensions to analyze!)

- Values are logged into log files or the screen exactly as with MCMC

- Every parameter in the state should be associated with only one Gridder.

- For now only Real parameters can be gridded. (Integer parameters should work too, but haven't been tested).
- Boolean and Integer parameters will be easy to add later.
- Gridding trees should be easy enough in principle, but would need a list of TreeParsers. It would be more elegant to simply specify a Nexus/Newick file and read in trees from there.
