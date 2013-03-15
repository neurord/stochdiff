package org.catacomb.numeric.difnet.calc;

import org.catacomb.numeric.difnet.StateNode;
import org.catacomb.numeric.difnet.Stimulus;



public final class NetMapNode {

    int peerIndex;
    int index;

    boolean fixed;
    boolean locallyFixed;

    double value;
    double flux;

    double capacitance;

    double appliedValue;
    double appliedFlux;
    double appliedDrive;
    double appliedConductance;

    NetMapLink[] upLink;
    NetMapLink[] downLink;

    // workspace for setup and sparse matrix;
    boolean mark;
    double diag;
    double rhs;


    void readState(StateNode dnNode, double time) {
        value = dnNode.getValue(null);
        appliedValue = dnNode.getAppliedValue(null); // ****** EFF only for fixed nodes;

        locallyFixed = false;
        appliedFlux = 0.;

        diag = 0.;
        rhs = 0.;

        // may be worth separating out the stimulated nodes so we don't
        // have to call this everywhere
        Stimulus stim = dnNode.getStimulus();
        if (stim != null) {
            int typ = stim.getType();
            double v = stim.getValue(time);
            if (typ == Stimulus.VALUE) {
                value = appliedValue = v;
                locallyFixed = true;

                diag = 1.;
                rhs = 0.;

            } else if (typ == Stimulus.FLUX) {
                appliedFlux = v;
            }
        }
    }


    void writeState(StateNode dnNode) {
        dnNode.setValue(null, value);
        if (fixed || locallyFixed) {
            dnNode.setFlux(null, flux);
        }
    }


    boolean isFree() {
        return (!fixed && !locallyFixed);
    }

}
