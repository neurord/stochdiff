
package org.catacomb.numeric.difnet;



/** properties of a network on which a diffusion eqution can be solved.
 * including isolated branched neurons, cell-bath-pipette systems,
 * or gap junction networks.
 *
 * The properties object contains the geometry of the network, and can
 * make multiple networks withthis geometry with its newDiffusiveNet
 * method. For each node in the netowork there is a corresponding
 * NodeProperties object here. Structural information belongs in the
 * NodeProperties objects. State information goes in the Nodes
 * themselves.
 *
 * The diffusion calculation is performed by a NetDiffuser operating on a
 * DiffusiveNet, and calling the get.. and set.. methods in its nodes
 * and links.
 */


public interface NetStructure {


    /** gets the properties of the nodes in the network which take part
     * in the diffusion process.
     */
    StructureNode[] getNodes();

    /** gets the properties of the links in the network which take part
     *  in the diffusion calculation.
     */
    StructureLink[] getLinks();


    NetState newState();

}
