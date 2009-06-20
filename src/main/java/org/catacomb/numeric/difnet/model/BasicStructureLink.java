package org.catacomb.numeric.difnet.model;

import org.catacomb.numeric.difnet.StructureLink;
import org.catacomb.numeric.difnet.StructureNode;
import org.catacomb.numeric.geom.Carrot;


public class BasicStructureLink implements StructureLink {

    BasicStructureNode nodeA;
    BasicStructureNode nodeB;

    boolean flip; // POSERR needed?

    double area;
    double capacitance;       // GETSET
    double conductance;       // GETSET

    double activeArea;        // GETSET


    public BasicStructureLink() {

    }

    public BasicStructureLink(BasicStructureNode bsna, BasicStructureNode bsnb) {
        nodeA = bsna;
        nodeB = bsnb;
    }





    public void setFlip() {
        flip = true;
    }


    public void applyAreaConductance(double g) {
        conductance = activeArea * g;
//     E.info("lk cond " + conductance);
    }

    public void applyAreaCapacitance(double c) {
        capacitance = activeArea * c;
//      E.info("lk cap " + capacitance);
    }



    public void applyAxialConductance(double g) {
        double cond = Carrot.conductance(nodeA.getX(), nodeA.getY(), nodeA.getZ(), nodeA.getRadius(),
                                         nodeB.getX(), nodeB.getY(), nodeB.getZ(), nodeB.getRadius());

        conductance = g * cond;
//      E.info("lk conductance " + conductance);
    }




    public BasicStateLink newState(BasicStateNode sna, BasicStateNode snb) {
        return new BasicStateLink(this, sna, snb);
    }



    public void setNodeA(StructureNode sn) {
        nodeA = (BasicStructureNode)sn;
    }

    public StructureNode getNodeA() {
        return nodeA;
    }

    public void setNodeB(StructureNode sn) {
        nodeB = (BasicStructureNode)sn;
    }

    public StructureNode getNodeB() {
        return nodeB;
    }


    public void calculateArea() {
        double farea = Carrot.area(nodeA.getX(), nodeA.getY(), nodeA.getZ(), nodeA.getRadius(),
                                   nodeB.getX(), nodeB.getY(), nodeB.getZ(), nodeB.getRadius());
        double har = farea / 2.;
        nodeA.incrementArea(har);
        nodeB.incrementArea(har);
    }


    public double getActiveArea() {
        return activeArea;
    }


    public void setActiveArea(double activeArea) {
        this.activeArea = activeArea;
    }


    public double getCapacitance() {
        return capacitance;
    }



    public double getConductance() {
        return conductance;
    }




}




