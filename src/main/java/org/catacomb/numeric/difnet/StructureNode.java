
package org.catacomb.numeric.difnet;

/** properties of a node in a diffusible network. The corresponding
 *  node may containing  multiple diffusible quantities, in which case the
 *  <code>dq</code> argument specifiew which to consult.
 * NB, as yet dq is unsupported MISSING
 * hasFixedValue should return true for nodes whosse value can't be changed,
 * like an isopotential bath. In this case setValue does nothing and
 * the result of getCapacitance is undefined.
 * Such odes are important in a DiffusibleNet to break loops and take advantage
 * of sparse matrix methods in computing the diffusion.
 */


public interface StructureNode {

    /** returns true only for those nodes whose value is externally fixed,
     * such as the potential or an earthed bath or a perfect voltage clamp.
     *
     * @return true if the node's value for the specified quantity is fixed
     */
    boolean hasFixedValue(DiffusibleQuantity dq);



    void setPosition(double x, double y, double z);

    void setRadius(double r);


    /** Stores an integer for later retrieval. This is required so that
     * NetDiffuser implementations can make certain optimisations
     * setting up permutations of the network for computing the diffusion.
     *
     */
    void setWork(int i);

    /** retrieves the previously set work integer.
     *
     * @return the stored integer
     */
    int getWork();

}


