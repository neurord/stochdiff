package org.catacomb.graph.drawing;


import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;


public class IconWrapper extends FixedDrawingComponent {

    VectorIcon icon;
    double xRel;
    double yRel;
    double scale;

    public IconWrapper(VectorIcon vi, double x, double y, double scl) {
        icon = vi;
        xRel = x;
        yRel = y;
        scale = scl;
        if (icon == null) {
            E.warning("wrapped null icon");
        }
    }


    public boolean isWrapper() {
        return true;
    }

    @Override
    public void instruct(Painter p, double cx, double cy, double pscl) {
        if (icon != null) {
            icon.instruct(p, cx + pscl * xRel, cy + pscl * yRel, pscl * scale);
        }
    }


    public VectorIcon getIcon() {
        return icon;
    }

    public double getXRel() {
        return xRel;
    }

    public double getYRel() {
        return yRel;
    }

    public double getScale() {
        return scale;
    }

    @Override
    public void applyToShape(Shape shp) {
        // TODO who needs this? should it be the real icon??
        // - just a box for now...
        E.missing(" cannot apply icon wrapper to shape");

        double rx = 0.2;
        double ry = 0.2;


        double[] xpts = { xRel - rx, xRel - rx, xRel + rx, xRel + rx };
        double[] ypts = { yRel - ry, yRel + ry, yRel + ry, yRel - ry };
        shp.setXpts(xpts);
        shp.setYpts(ypts);
    }





}
