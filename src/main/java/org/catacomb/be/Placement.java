package org.catacomb.be;



public class Placement {

    Position position;
    Direction direction;
    double time;
    // REFAC get rid of time


    public Placement(double x, double y, double vx, double vy, double t) {
        position = new Position(x, y);
        direction = new Direction(vx, vy);
        time = t;
    }

    public Placement(Position p, Direction d, double t) {
        position = new Position(p);
        direction = new Direction(d);
        time = t;
    }

    public Placement(Position p, Direction d) {
        position = new Position(p);
        direction = new Direction(d);
    }

    public Placement(double[] pxy, double[] hxy, double t) {
        this(pxy[0], pxy[1], hxy[0], hxy[1], t);
    }

    public Position getPosition() {
        return position;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getTime() {
        return time;
    }

    @SuppressWarnings("boxing")
    public String toString() {
        return String.format("pos %.3g %.3g   dir %.3g %.3g",
                             position.getX(), position.getY(),
                             direction.getXCpt(), direction.getYCpt());
    }

}
