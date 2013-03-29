package org.catacomb.geom;

import java.util.ArrayList;

import org.catacomb.be.Position;
import org.catacomb.interlish.content.Heading;
import org.catacomb.report.E;



public class Geom {

    public static double allowedFraction(double x, double y, double vx, double vy,
                                         double[] xyxy) {
        double fc = fCross(x, y, x+vx, y+vy, xyxy[0], xyxy[1], xyxy[2], xyxy[3]);
        return fc;
    }



    private final static double fCross(double xa, double ya, double xb, double yb,
                                       double xc, double yc, double xd, double yd) {

        /*
        double xt = xd;
        double yt = yd;
        xd = xc;
        yd = yc;
        yc = yt;
        xc = xt;
        */
        // switch signs of vb, vd ?? ******
        double va = xb - xa;
        double vb = xd - xc;
        double vc = yb - ya;
        double vd = yd - yc;
        double det = vd * va - vb * vc;
        double fcr = -1.;
        double gcr = -1.;
        if (det != 0.0) {

            double rx = xc - xa;  // ******* error - was xd - xa
            double ry = yc - ya;  ///        was yd - ya;

            fcr = (vd * rx  - vb * ry) / det;
            gcr = (vc * rx -  va * ry) / det;  // pos - sign?
        }
        if (gcr < 0. || gcr > 1.) {
            fcr = -1.;
        }
        return fcr;
    }


    public static double perimeterDistance(Position pos, Heading h, double radius) {
        double vx = h.getX();
        double vy = h.getY();
        double px = pos.getX();
        double py = pos.getY();
        double a = 1.;
        double b = 2 * vx * px + 2 * vy * py;
        double c = px*px + py*py - radius*radius;
        double det = Math.sqrt(b * b - 4 * a * c);
        double da = (-1 * b + det) / 2.;
        double db = (-1 * b - det) / 2;
        return Math.max(da, db); // want the positive one
    }



    public static double distanceTo(Position pos, double[] xyxy) {
        double x = pos.getX();
        double y = pos.getY();
        double d1q = distance2(x, y, xyxy[0], xyxy[1]);
        double d2q = distance2(x, y, xyxy[2], xyxy[3]);
        double dsq = distance2(xyxy[0], xyxy[1], xyxy[2], xyxy[3]);

        double ret = 0.;
        if (Math.abs(d1q-d2q) < dsq) {
            // its an acute angled triangle;
            double ddq = d1q - d2q;
            double w = 2 * d1q + 2 * d2q - dsq - ddq*ddq/dsq;
            ret = 0.5 * Math.sqrt(w);

        } else {
            ret = Math.sqrt(Math.min(d1q, d2q));
        }
        return ret;
    }


    public static double altDistanceTo(Position pos, double[] xyxy) {
        // slower, for testing
        double x = pos.getX();
        double y = pos.getY();
        double d1 = distance(x, y, xyxy[0], xyxy[1]);
        double d2 = distance(x, y, xyxy[2], xyxy[3]);
        double ds = distance(xyxy[0], xyxy[1], xyxy[2], xyxy[3]);
        double cos1 = (ds*ds + d1*d1 - d2*d2) / (2 * ds * d1);
        double cos2 = (ds*ds + d2*d2 - d1*d1) / (2 * ds * d2);
        double sin1 = Math.sqrt(1. - cos1*cos1);
        double dperp = d1 * sin1;
        double ret = Math.min(d1, d2);
        if (cos1 > 0. && cos2 > 0.) {
            ret = dperp;
        }
        return ret;
    }




    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx*dx + dy*dy);
    }


    public static double distance2(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx*dx + dy*dy;
    }


    public static void main(String[] argv) {
        double[] xyxy = new double[4];
        for (int i = 0; i < 4; i++) {
            xyxy[i] = 5 * Math.random();
        }

        for (int ip = 0; ip < 10; ip++) {
            Position p = new Position(5 * Math.random(), 5 * Math.random());
            E.info("seg distances " + distanceTo(p, xyxy) +
                   " " + altDistanceTo(p, xyxy));
        }
    }


    public static double distanceBetween(Position pos, Position pse) {
        double dx = pse.getX() - pos.getX();
        double dy = pse.getY() - pos.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }


    public static Position[] getBoundary(ArrayList<Position> apts) {
        // TODO - actually do it!
        Position[] pa = apts.toArray(new Position[0]);
        return pa;
    }

    public static Position translatedRotatedPoint(Position b, Position pc, Heading h) {
        double vc = h.getX();
        double vs = h.getY();
        double xc = pc.getX();
        double yc = pc.getY();
        Position ret = new Position(xc + vc * b.getX() - vs * b.getY(),
                                    yc + vs * b.getX() + vc * b.getY());
        return ret;
    }


    public static Position[] translatedRotatedPoints(Position[] bdry, Position pc, Heading h) {

        int nb = bdry.length;
        Position[] rp = new Position[nb];
        double vc = h.getX();
        double vs = h.getY();
        double xc = pc.getX();
        double yc = pc.getY();

        for (int i = 0; i < nb; i++) {
            Position b = bdry[i];
            rp[i] = new Position(xc + vc * b.getX() - vs * b.getY(),
                                 yc + vs * b.getX() + vc * b.getY());
        }
        return rp;
    }



    public static Position[] copyPositionArray(Position[] pa) {
        int np = pa.length;
        Position[] ret = new Position[np];
        for (int i = 0; i < np; i++) {
            ret[i] = pa[i].copy();
        }
        return ret;
    }


    public static Position[] translatedRotatedPoints(Position[] bdry, Position pt, Heading h, Position pcen) {
        // rotate around pcen, then translate so 0,0 goes to pt

        int nb = bdry.length;
        Position[] rp = new Position[nb];
        double vc = h.getX();
        double vs = h.getY();
        double xt = pt.getX();
        double yt = pt.getY();

        double xcr = pcen.getX();
        double ycr = pcen.getY();

        for (int i = 0; i < nb; i++) {
            Position b = bdry[i];
            double dx = b.getX() - xcr;
            double dy = b.getY() - ycr;

            double xnew = xt + vc * dx - vs * dy + xcr;
            double ynew = yt + vs * dx + vc * dy + ycr;
            rp[i] = new Position(xnew, ynew);
        }
        return rp;
    }


    public static double intersectFraction(Position pa, Position pb, double[] xyxy) {
        return fCross(pa.getX(), pa.getY(), pb.getX(), pb.getY(),
                      xyxy[0], xyxy[1], xyxy[2], xyxy[3]);
    }



    public static double angleTo(Position position) {
        return Math.atan2(position.getY(), position.getX());
    }



    public static void rotateAbout(Position[] rp, Position rpc, double rad) {
        int nb = rp.length;
        for (int i = 0; i < nb; i++) {
            rp[i].rotateAbout(rpc, rad);
        }

    }


}


