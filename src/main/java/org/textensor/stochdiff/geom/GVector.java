package org.textensor.stochdiff.geom;


public class GVector implements Vector {


    double x;
    double y;
    double z;

    public GVector(double ax, double ay, double az) {
        x = ax;
        y = ay;
        z = az;
    }

    public GVector(Position p) {
        this(p.getX(), p.getY(), p.getZ());
    }

    public GVector(double[] d) {
        this(d[0], d[1], d[2]);
    }

    public GVector(Vector v) {
        this(v.getDX(), v.getDY(), v.getDZ());
    }


    public GVector() {
        this(0., 0., 0.);
    }

    public double getDX() {
        return x;
    }

    public double getDY() {
        return y;
    }

    public double getDZ() {
        return z;
    }

    public void add(Vector vector) {
        x += vector.getDX();
        y += vector.getDY();
        z += vector.getDZ();
    }

}
