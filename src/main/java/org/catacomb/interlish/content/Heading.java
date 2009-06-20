package org.catacomb.interlish.content;

import org.catacomb.be.Direction;


public class Heading {

    double hx;
    double hy;


    public Heading() {
        set(0., 1.);
    }


    public Heading(double x, double y) {
        set(x, y);
    }


    public void set(double x, double y) {
        double d = Math.sqrt(x*x + y*y);
        hx = x / d;
        hy = y / d;
    }


    public String toString() {
        return ("heading(" + hx + ", " + hy + ")");
    }

    public Direction getDirection() {
        return new Direction(hx, hy);
    }


    public double getX() {
        return hx;
    }

    public double getY() {
        return hy;
    }

    public void setBearing(double deg) {
        double rad = (deg / 180.) * Math.PI;
        double xv = Math.sin(rad);
        double yv = Math.cos(rad);
        set(xv, yv);
    }


    public double getBearing() {
        double b =  makeBearing(hx, hy);
        return b;
    }


    public static double makeBearing(double vx, double vy) {
        double rad = Math.atan2(vx, vy);
        double deg = 180. * (rad / Math.PI);
        if (deg < 0.) {
            deg += 360.;
        }

        /*
        Heading h = new Heading();
        h.setBearing(deg);
        if (Math.abs(h.getX() - vx) > 1.e-6 || Math.abs(h.getY() - vy) > 1.e-6) {
              E.error("bearing check failed " + vx + " " + vy + " " +
                    " " + h.getX() + " " + h.getY());
        }
        */

        return deg;
    }


    public Heading getOpposite() {
        return new Heading(-1 * hx, -1 * hy);
    }

}
