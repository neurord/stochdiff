

package org.catacomb.numeric.difnet.model;

import org.catacomb.numeric.difnet.NetState;
import org.catacomb.numeric.difnet.StateLink;
import org.catacomb.numeric.difnet.StateNode;


public class BasicNetState implements NetState {


    BasicNetStructure structure;

    public BasicStateNode[] nodes;
    public BasicStateLink[] links;



    static int iok = 0;
    static int ierr = 1;
    static int istat = iok;

    double time;


    public BasicNetState(BasicNetStructure bns, BasicStateNode[] nds, BasicStateLink[] lks) {
        structure = bns;
        nodes = nds;
        links = lks;
        time = 0.;
    }


    public StateNode[] getNodes() {
        return nodes;
    }


    public StateLink[] getLinks() {
        return links;
    }

    public StateNode getNode(int inode) {
        return nodes[inode];
    }

    public double getValueAt(int i) {
        return nodes[i].getValue(null);
    }


    public void setNodes(BasicStateNode[] anp) {
        nodes = anp;
    }

    public void setLinks(BasicStateLink[] alp) {
        links = alp;
    }






    public boolean useIntrinsics() {
        return false;
    }


    public boolean forceFullMatrix() {
        return false; // EXTEND
    }



    public void setError() {
        istat = ierr;
    }

    public void setOK() {
        istat = iok;
    }

    public boolean isError() {
        return (istat == ierr);
    }

    public double getTime() {
        return time;
    }

    public void setTime(double t) {
        time = t;
    }

}

