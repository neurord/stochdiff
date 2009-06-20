package org.catacomb.be;





public class BasicTimestep implements Timestep {

    double deltaT;

    public BasicTimestep(double d) {
        deltaT = d;
    }

    public double getDeltaT() {
        return deltaT;
    }
}
