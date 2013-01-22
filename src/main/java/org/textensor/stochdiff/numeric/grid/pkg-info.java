/**
 *
 *
 * SteppedStochasticCalc contains the particle number based calculation with fixed timesteps
 * where a number of diffusion events and reactions is generated for each reaction, volume and
 * volume-volume connection at each timestep.
 *
 *
 * The DeterministicGridCalc is the opposite extereme - everything isn continuous.
 *
 * In between there shold be the  MixedStochasicCalc which treats large densities continuously, and
 * small ones approximately.
 *
 * And a SemiExactSteppedStochasticCalc, that is exact for very small number densities where the
 * changes within a step can be significant - direct or next reaction method.
 *
 *
 *
 * Performance
 *
 * Java -
 *      Math.random costs about 3 times as much as NRRandom.random
 *      Math.log costs more than either and should be avoided within the main loop.
 *
 *
 * C -
 *     GCJ - unclear - vanilla use doesn't improve on Math.log. May be a matter
 *     of compiler and linker options...
 *
 */
