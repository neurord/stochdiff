package org.catacomb.numeric.difnet;

import org.catacomb.be.Timestep;



/**  diffusion on full or a-cyclic networks. An example of the
 *   latter is an isolated neurons in an isopotential baths.
 *   Loosely called a cable solver.
 *
 *   Implementations should check for and, where possible, exploit, the
 *   absence of loops, as with some form of sparse in-place matrix
 *   solution (cf Hines Method). When applied to neurons, the diffusing
 *   quantity may be the voltage or a diffusible specie.
 *
 *
 */
public interface DiffusionCalculator {

    /** Initialises the internal representation of the network structure.
     *  In general this nethod will only be called once, possibly by the
     *  constructor. All geometry specific but value independent calculation
     *  can be done here. Subsequent callse to <code>diffuse</code>
     *  use the same precomputed mapping.
     *
     * @param dnet the diffusive net properties defining the network geometry.
     */
    void init(NetStructure dnet);


    /** Run the diffusion calculation for the supplied network, diffusible
     *  quantity and timestep. The values in the DiffusiveNet are read with
     *  <code> getValue(DiffusibleQuantity)</code>
     *  and set after the calculation with
     *  <code> setValue(DiffusibleQuantity, double)</code>
     *
     * @param difnet the NetState to evolve. Note that its structure
     *               object must be the same as that used to initialise the
     *               net diffuser.
     * @param dq the diffusible quantity to use. One net may contain several
     *           different diffusing quantitites - membrane potential, ions,
     *           indicators and others.
     *           NB as yet dq is ignored MISSING
     * @param timestep the timestep to be taken
     *
     * @throws something if the NetState's structure doesnt match the one
     *                   the calculator was initialized on
     */
    void advance(NetState dn, DiffusibleQuantity dq, Timestep ts);

}









