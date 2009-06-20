package org.catacomb.numeric.difnet;




/** any network on which a diffusion eqution can be solved.
 * including isolated branched neurons, cell-bath-pipette systems,
 * or gap junction networks.
 *
 * The diffusion calculation is perfrormed by a NetDiffuser operating on a
 * DiffusiveNet, and calling the get.. and set.. methods in its nodes
 * and links.
 */

public interface NetState {


    /** gets the nodes in the network which take part in the diffusion
     *  process.
     */
    StateNode[] getNodes();

    /** gets the links in hte netowork which take part in the diffusion
     *  calculation.
     */
    StateLink[] getLinks();


    StateNode getNode(int inode);

    double getValueAt(int i);

    boolean useIntrinsics();


    boolean forceFullMatrix();


    void setError();

    void setOK();

    boolean isError();

    double getTime();

    void setTime(double t);

}

