# BEAST2 likelihood surface

Evaluate pre-specified points on the likelihood surface. Good for investigating smoothness/roughness of a model's likelihood surface, parameter correlations and numerical errors (underflow/overflow)

- As with MCMC the state specifies all the parameters that are to be modified
- Gridders can be used to either specify discrete states that should be evaluated for a given parameter (using values input), or a sequence of states using (for:by:to input). Every combination of gridder values are then evaluated (careful about adding too many gridders, the recursion will become too big and the result will have too many dimensions to analyze!)
- Values are logged into log files or the screen exactly as with MCMC
- Every parameter in the state should be associated with only one Gridder.
- For now only Real parameters can be gridded. (Integer parameters should work too, but haven't been tested).
- Boolean and Integer parameters will be easy to add later.
- Gridding trees should be easy enough in principle, but would need a list of TreeParsers. It would be more elegant to simply specify a Nexus/Newick file and read in trees from there.

There are a couple of examples in the examples folder:
- Example 1: Minimal example gridding along two axes
- Example 2: Evaluating too many dimensions
- Example 3: Accessing specific indices of multidimensional parameters
- Example 4: Like example 3
- Example 5: Evaluating a list of specific values instead of a sequence (not necessarily monotonic or with equal increments in-between)
