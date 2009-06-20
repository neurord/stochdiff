
package org.catacomb.numeric.difnet;


/** a node in a diffusive network.
 *
 */


public interface StateNode {

    /** returns true if the node has an active stimulation scheme.
     *  Note that this is only possible if an object extends both DiffusiveNode
     *  <em>and</em> Stimulable, and has had a scheme attached.
     */
    Stimulus getStimulus();


    double getValue(DiffusibleQuantity dq);

    double getAppliedValue(DiffusibleQuantity dq);

    void setValue(DiffusibleQuantity dq, double d);


    void setFlux(DiffusibleQuantity dq, double d);


    /** gets the capacitance of this node. Fo electrical diffusion this may be
     * zero, since the capacitance is associated with the membrane, whereas
     * for chemical diffusiion, the link capacitance would be zero, and the
     * capacitance where would be related to the node volume.
     *
     * @param dq the diffusible quantity for which to get the capacitance.
     */
    double getCapacitance(DiffusibleQuantity dq);
    //  shouldn't this be part of the structure?



    /** gets the properties object holding the non-state information for this
     * node
     */
    StructureNode getStructureNode();


    void setStimulus(Stimulus stim);

}


