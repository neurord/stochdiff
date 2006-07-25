package org.textensor.stochdiff.geom;


public class GPosition implements Position, Movable {

    double x;
    double y;
    double z;

    public GPosition(double ax, double ay, double az) {
        x = ax;
        y = ay;
        z = az;
    }

    public GPosition(Position p) {
        this(p.getX(), p.getY(), p.getZ());
    }



    public GPosition() {
        this(0.,0., 0.);
    }

    public double getX() {
        return x;
    }


    public double getY() {
        return y;
    }


    public double getZ() {
        return z;
    }

    public void moveTo(double ax, double ay, double az) {
        x = ax;
        y = ay;
        z = az;

    }

    public void add(Position position) {
        x += position.getX();
        y += position.getY();
        z += position.getZ();
    }

}
