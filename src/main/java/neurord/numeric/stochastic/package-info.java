/**
 *
 * These are the functions for the innner loops of the calculation.  Once the algorithms
 * are settled, they should probably be hand-converted to c for better performance.
 *
 * The main class is the StepGenerator that generates the number of particles that
 * make a particular transition given the total number, the probability of one particle
 * going, and a random number.
 *
 * The mixed stochastic/continuous calculation takes a ReactionTable for the reactions and a
 * VolumeGrid for the morphology and does the calculation. For high number densities the
 * update algorithm should use Dufort-Frankel. For lower densities, various exact and
 * approximate stochastic methods a la Blackwell.
 *
 **/

package neurord.numeric.stochastic;
