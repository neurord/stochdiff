package org.textensor.stochdiff.geom;


public class GTranslation implements Translation {

    Vector vec;

    public GTranslation(Position p) {
        vec = new GVector(p);
    }

    public GTranslation(Vector v) {
        vec = new GVector(v);
    }


    public void apply(Movable mov) {
        Position p0 = mov;
        mov.moveTo(p0.getX() + vec.getDX(),
                   p0.getY() + vec.getDY(),
                   p0.getZ() + vec.getDZ());

    }


    public Position getTranslated(Position p0) {
        GPosition gp = new GPosition(p0);
        apply(gp);
        return gp;
    }

}
