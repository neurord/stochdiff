package org.catacomb.numeric.difnet.model;

import org.catacomb.numeric.difnet.DiffusibleQuantity;
import org.catacomb.numeric.difnet.StateLink;
import org.catacomb.numeric.difnet.StateNode;
import org.catacomb.numeric.difnet.StructureLink;


public class BasicStateLink implements StateLink {

    BasicStructureLink structure;

    BasicStateNode nodeA;
    BasicStateNode nodeB;


    double conductance;
    double capacitance;
    double drive;

    double intrinsicCurrent;



    public BasicStateLink(BasicStructureLink bsl, BasicStateNode bsna, BasicStateNode bsnb) {
        structure = bsl;
        nodeA = bsna;
        nodeB = bsnb;

        conductance = structure.getConductance();
        capacitance = structure.getCapacitance();

    }


    public StateNode getNodeA() {
        return nodeA;
    }

    public StateNode getNodeB() {
        return nodeB;
    }

    public double getConductance(DiffusibleQuantity dq) {
        return conductance;
    }

    public double getCapacitance(DiffusibleQuantity dq) {
        return capacitance;
    }

    // currents indepdnednt of link conductance, such as gating current;
    public double getIntrinsicCurrent(DiffusibleQuantity dq) {
        return intrinsicCurrent;
    }

    public double getDrive(DiffusibleQuantity dq) {
        return drive;
    }

    public StructureLink getStructureLink() {
        return structure;
    }

}


/*



   public void initializeState() {
      if (cset != null) {
	 setChannelSolutions();
	 cset.setPotentialDifference(getPotentialDifference());
	 cset.equilibrate();
      }
   }





   // ###### props.flip appears in each of these three methods ######
   // it specifiew whether the inside of the membrane is nodeA (flip=false)
   // or nodeB (flip-true)

   public void setChannelSolutions() {
      CcSolutionProp sa = nodeA.getSolutionProp();
      CcSolutionProp sb = nodeB.getSolutionProp();
      cset.setInternalSolution(props.flip ? sb : sa);
      cset.setExternalSolution(props.flip ? sa : sb);
   }


   private double getPotentialDifference() {
      double v = (nodeA.getPotential() - nodeB.getPotential());
      if (props.flip) v = -v;
      return v;
   }


   public void advance(CcTimestep timestep) {
      current = 0.;
      if (props.openSolutionJunction) {
	 reversal = props.getJunctionPotential(nodeA.getSolutionProp(),
					       nodeB.getSolutionProp());


      } else if (cset != null) {
	// *** should work out whether  this is necessary *** ;
	 setChannelSolutions();
	 cset.setPotentialDifference(getPotentialDifference());
	 cset.advance(timestep);

	 CcLinG clg = cset.getLinearizedConductance();
	 conductance = clg.getConductance();
	 reversal = clg.getReversalPotential();
	 current = cset.getIntrinsicCurrent();
	 if (props.flip) {
	    reversal = -reversal;
	    current = -current;
	 }

	 if (nodeA.needsFluxes() || nodeB.needsFluxes()) {
	    NameValueSet nvs = cset.getSpecieFluxes();
	    // sign shold be different for nodeA, nodeB POSERR

	    if (nodeA.needsFluxes()) nodeA.incrementFluxes(nvs, props.flip,
							   timestep);
	    if (nodeB.needsFluxes()) nodeB.incrementFluxes(nvs, props.flip,
							   timestep);

	 }

      }


   }

*/
