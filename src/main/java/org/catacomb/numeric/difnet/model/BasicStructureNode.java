package org.catacomb.numeric.difnet.model;

import org.catacomb.numeric.difnet.DiffusibleQuantity;
import org.catacomb.numeric.difnet.StructureNode;


public class BasicStructureNode implements StructureNode {


    int iwork;
    boolean fixed;

    double capacitance;

    double area;

    double x;
    double y;
    double z;
    double radius;

    double initialValue;




    public BasicStateNode newState() {
        return new BasicStateNode(this);
    }

    public void setInitialValue(double d) {
        initialValue = d;
    }

    public void setFixedValue(double d) {
        initialValue = d;
        fixed = true;
    }


    public double getInitialValue() {
        return initialValue;
    }


    public void setPosition(double vx, double vy, double vz) {
        x = vx;
        y = vy;
        z = vz;
    }

    public void setRadius(double r) {
        radius = r;
    }

    double getX() {
        return x;
    }

    double getY() {
        return y;
    }

    double getZ() {
        return z;
    }

    double getRadius() {
        return radius;
    }


    public void setWork(int i) {
        iwork = i;
    }


    public int getWork() {
        return iwork;
    }


    public void setFixed(boolean b) {
        fixed = b;
    }


    public boolean hasFixedValue(DiffusibleQuantity dq) {
        return fixed;
    }

    public double getCapacitance() {
        return capacitance;
    }

    public void setArea(double d) {
        area = d;
    }

    public double getArea() {
        return area;
    }

    public void incrementArea(double d) {
        area += d;
    }

}




