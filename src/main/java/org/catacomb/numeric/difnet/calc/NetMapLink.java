
package org.catacomb.numeric.difnet.calc;

import org.catacomb.numeric.difnet.StateLink;


public final class NetMapLink {
    int peerIndex;
    NetMapNode nodeA;
    NetMapNode nodeB;
    double conductance;
    double capacitance;
    double current;
    double drive;
    boolean flip;

    // workspace for sparse matrix;
    boolean mark;
    double wsA;
    double wsB;


    void readState(StateLink dnl) {
        conductance = dnl.getConductance(null);
        drive = dnl.getDrive(null);
        capacitance = dnl.getCapacitance(null);
        current = dnl.getIntrinsicCurrent(null);
        wsA = 0.;
        wsB = 0.;
    }



    void reverseEnds() {
        flip = !flip;
        NetMapNode nodeC = nodeA;
        nodeA = nodeB;
        nodeB = nodeC;
    }


}






