package org.catacomb.numeric.difnet;


/** a link in a diffusive net.


 */


public interface StateLink {

    StateNode getNodeA();

    StateNode getNodeB();

    double getConductance(DiffusibleQuantity dq);

    double getCapacitance(DiffusibleQuantity dq);

    double getIntrinsicCurrent(DiffusibleQuantity dq);

    double getDrive(DiffusibleQuantity dq);


    /** gets the shared properties object holding the non-state information
     * for this link.
     */
    StructureLink getStructureLink();
}
