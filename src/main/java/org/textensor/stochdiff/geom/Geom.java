package org.textensor.stochdiff.geom;

public final class Geom {


    public static Position midpoint(Position p1, Position p2) {
        return new GPosition(0.5 * (p1.getX() + p2.getX()),
                             0.5 * (p1.getY() + p2.getY()),
                             0.5 * (p1.getZ() + p2.getZ()));
    }


    public static Translation translation(Position p) {
        return new GTranslation(p);
    }


    public static Vector fromToVector(Position p1, Position p2) {
        return new GVector(p2.getX() - p1.getX(),
                           p2.getY() - p1.getY(),
                           p2.getZ() - p1.getZ());
    }

    public static Vector unitX() {
        return new GVector(1., 0., 0.);
    }

    public static Vector unitY() {
        return new GVector(0., 1., 0.);
    }

    public static Vector unitZ() {
        return new GVector(0., 0., 1.);
    }

    public static Vector xyProjection(Vector v) {
        return new GVector(v.getDX(), v.getDY(), 0.);
    }

    public static Vector xProjection(Vector v) {
        return new GVector(0, v.getDY(), v.getDZ());
    }

    public static Vector zProjection(Vector v) {
        return new GVector(v.getDX(), v.getDY(), 0.);
    }



    public static Rotation fromZRotation(Vector v) {
        assert false: "not tested, probably wrong";
        Vector v1 = unitZ();
        double phi = angleBetween(v1, v);
        GRotation gr1 = aboutYRotation(phi);
        Vector vp = xyProjection(v);
        double theta = angleBetween(unitX(), vp);
        GRotation gr2 = aboutZRotation(theta);

        GRotation grot = gr2.times(gr1);
        return grot;
    }



    public static GRotation aboutZRotation(double angle) {
        GRotation gr = new GRotation(GRotation.Z_AXIS, angle);
        return gr;
    }

    public static GRotation aboutYRotation(double angle) {
        GRotation gr = new GRotation(GRotation.Y_AXIS, angle);
        return gr;
    }

    public static GRotation aboutXRotation(double angle) {
        GRotation gr = new GRotation(GRotation.X_AXIS, angle);
        return gr;
    }


    public static double length(Vector v) {
        double x = v.getDX();
        double y = v.getDY();
        double z = v.getDZ();
        double l2 = x*x + y*y + z*z;
        double l = Math.sqrt(l2);
        return l;
    }

    public static double dotProduct(Vector v1, Vector v2) {
        double d = 0.;
        d += v1.getDX() * v2.getDX();
        d += v1.getDY() * v2.getDY();
        d += v1.getDZ() * v2.getDZ();
        return d;
    }

    // result between 0 and PI
    public static double angleBetween(Vector v1, Vector v2) {
        double cosphi = dotProduct(v1, v2) / (length(v1) * length(v2));
        double phi = Math.acos(cosphi);
        return phi;
    }


    // the rotation angle that will move the z projection of v1 onto v2;
    public static double zRotationAngle(Vector v1, Vector v2) {
        double cx = v1.getDX() * v2.getDX() + v1.getDY() * v2.getDY();
        double cy = -1 * v1.getDY() * v2.getDX() + v1.getDX() * v2.getDY();
        double phi = Math.atan2(cy, cx);
        return phi;
    }

    // the rotation angle that will move the x projection of v1 onto v2;
    public static double xRotationAngle(Vector v1, Vector v2) {
        double ca = v1.getDY() * v2.getDY() + v1.getDZ() * v2.getDZ();
        double cb = -1 * v1.getDY() * v2.getDZ() + v1.getDZ() * v2.getDZ();
        double phi = Math.atan2(cb, ca);
        return phi;
    }

// the rotation angle that will move the x projection of v1 onto v2;
    public static double zElevation(Vector v) {
        double dy = v.getDY();
        double dx = v.getDX();
        double lxy = Math.sqrt(dx * dx + dy * dy);
        double phi = Math.atan2(v.getDZ(), lxy);
        return phi;
    }

    public static double yzRotationAngle(Vector v) {
        double phi = Math.atan2(-v.getDX(), v.getDY());
        return phi;
    }

    public static double xzRotationAngle(Vector v) {
        double phi = Math.atan2(v.getDY(), v.getDX());
        return phi;
    }

    public static Vector vector(double x, double y, double z) {
        return new GVector(x, y, z);
    }


    public static Position position(double x, double y, double z) {
        return new GPosition(x, y, z);
    }


    public static Vector getToVector(Position p) {
        return vector(p.getX(), p.getY(), p.getZ());
    }


    public static Position endPosition(Vector vr) {
        return position(vr.getDX(), vr.getDY(), vr.getDZ());
    }


    public static double distanceBetween(Position pa, Position pb) {
        return length(fromToVector(pa, pb));
    }


    public static double distanceBetween(double[] pa, double[] pb) {
        double dx = pb[0] - pa[0];
        double dy = pb[1] - pa[1];
        double dz = pb[2] - pa[2];
        double r2 = dx * dx + dy * dy + dz * dz;
        double r = Math.sqrt(r2);
        return r;
    }

    public static Position cog(Position[] perim) {
        int n = perim.length;
        GPosition pos = new GPosition();
        for (int i = 0; i < n; i++) {
            pos.add(perim[i]);
        }
        GPosition ret = new GPosition(pos.getX()/n, pos.getY()/n, pos.getZ()/n);
        return ret;
    }


    public static double getArea(Position[] perim) {
        Vector vec = getNormal(perim);
        double area = length(vec);
        return area;
    }




    public static Vector getNormal(Position[] perim) {
        GVector vec = new GVector();
        int n = perim.length;
        for (int i = 1; i < n-2; i++) {
            Vector pa = fromToVector(perim[0], perim[i]);
            Vector pb = fromToVector(perim[i], perim[i+1]);
            vec.add(crossProduct(pa, pb));
        }
        return vec;
    }


    public static Vector getUnitNormal(Position[] perim) {
        Vector va = getNormal(perim);
        double d = length(va);
        GVector ret = new GVector(va.getDX()/d, va.getDY()/d, va.getDZ()/d);
        return ret;
    }


    public static Vector crossProduct(Vector pa, Vector pb) {
        double ax = pa.getDX();
        double ay = pa.getDY();
        double az = pa.getDZ();

        double bx = pb.getDX();
        double by = pb.getDY();
        double bz = pb.getDZ();

        double rx = ay * bz - az * by;
        double ry = az * bx - ax * bz;
        double rz = ax * by - ay * bx;
        return new GVector(rx, ry, rz);
    }



}
