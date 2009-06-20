package org.catacomb.numeric.difnet.model;

import org.catacomb.numeric.difnet.DiffusibleQuantity;
import org.catacomb.numeric.difnet.StateNode;
import org.catacomb.numeric.difnet.Stimulus;
import org.catacomb.numeric.difnet.StructureNode;



public class BasicStateNode implements StateNode {

    BasicStructureNode structure;

    double value;
    double current; // supplied by the net diffuser for stimulated nodes;

    double capacitance;

    Stimulus stimulus;

    double appliedValue;


    public BasicStateNode(BasicStructureNode bsn) {
        structure = bsn;
        value = structure.getInitialValue();

        capacitance = structure.getCapacitance();

        if (structure.fixed) {
            appliedValue = structure.getInitialValue();
        } else {
            appliedValue = -999.;
        }
    }




    public double getValue(DiffusibleQuantity dq) {
        return value;
    }

    public double getAppliedValue(DiffusibleQuantity dq) {
        return appliedValue;
    }

    public void setValue(DiffusibleQuantity dq, double d) {
        value = d;
    }


    public void setFlux(DiffusibleQuantity dq, double d) {
        current = d;
    }


    public void setStimulus(Stimulus stim) {
        stimulus = stim;
    }

    public Stimulus getStimulus() {
        return stimulus;
    }


    /** gets the capacitance of this node. Fo electrical diffusion this may be
     * zero, since the capacitance is associated with the membrane, whereas
     * for chemical diffusiion, the link capacitance would be zero, and the
     * capacitance where would be related to the node volume.
     *
     * @param dq the diffusible quantity for which to get the capacitance.
     */
    public double getCapacitance(DiffusibleQuantity dq) {
        return capacitance;
    }

    public StructureNode getStructureNode() {
        return structure;
    }


}


/*
    public void incrementFluxes(NameValueSet nvs,
				boolean flip, Timestep tstep) {
       double dt = tstep.getDeltaT();
       //  POSERR should proifile to see if worth looking up in advance;
       String[] sa = recNames;
       for (int i = 0; i < sa.length; i++) {

	  if (sa[i].startsWith("total ")) {

	     String ssp = sa[i].substring(6, sa[i].length());
	     double flux = nvs.valueFor(ssp);

	     // flip here is wrong.... POSERR;

	     recValues[i] += dt * flux * (flip ? -1 : 1);
	     recValues[i] =  flux * (flip ? -1 : 1) * 1.e-2;
	  }

       }
    }


    public void initializeState() {
       potential = props.Vinit;
       if (controller != null) {
	  stimulus = controller.getStimulus(0, 0.);
	  if (stimulus != null && stimulus.getType() == Stimulus.VALUE) {
	     potential = stimulus.getValue();
	  }
       }
    }



*/





