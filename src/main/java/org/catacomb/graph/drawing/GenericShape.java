package org.catacomb.graph.drawing;


import org.catacomb.be.Position;
import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;
import org.catacomb.util.ArrayUtil;



public class GenericShape extends FixedDrawingComponent {




    public double curviness;

    public String closure;

    public String symmetry;


    public double[] xpts;
    public double[] ypts;


    public GenericShape() {
    }


    public GenericShape(Shape sh, Position cpos, double scl) {
        double[][] bdry = sh.getBoundaryPoints();
        xpts = fromScaledPoints(bdry[0], cpos.getX(), scl);
        ypts = fromScaledPoints(bdry[1], cpos.getY(), scl);



        curviness = sh.getCurviness();
        setLineColor(sh.getLineColor());
        setLineWidth(sh.getLineWidth());
        if (sh.isFilled()) {
            setFilled();
            setFillColor(sh.getFillColor());

        } else if (sh.isClosed()) {
            setClosed();
        } else {
            setOpen();
        }
    }



    private double[] fromScaledPoints(double[] va, double c, double scl) {
        int n = va.length;
        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (va[i] - c) / scl;
        }
        return ret;
    }


    public void reReference() {
        super.reReference();

        setFilled();
        if (closure != null) {
            if (closure.equals("open")) {
                setOpen();

            } else if (closure.equals("filled")) {
                setFilled();

            } else if (closure.equals("closed")) {
                setClosed();

            } else {
                E.warning("unrecognized closure " + closure);
            }
        }
    }





    public void instruct(Painter p, double offx, double offy, double scale) {
        int np = xpts.length;
        double[] xdr = new double[np];
        double[] ydr = new double[np];
        for (int i = 0; i < np; i++) {
            xdr[i] = offx + scale * xpts[i];
            ydr[i] = offy + scale * ypts[i];
        }
        if (isFilled()) {
            p.fillPolygon(xdr, ydr, np, getFillColor());
        }

        if (p_width > 0.5) {
            if (isClosed() || isFilled()) {
                p.drawPolygon(xdr, ydr, np, p_color, p_width, true);
            } else {
                p.drawPolyline(xdr, ydr, np, p_color, p_width, true);
            }
        }
        /*
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < np; i++) {
           sb.append(" " + xdr[i] + ", ");
        }
        E.info("painted gen shape " + sb.toString());
        */
    }




    public void applyToShape(Shape shp) {
        shp.setCurviness(curviness);
        shp.setSymmetryString(symmetry);

        shp.setXpts(ArrayUtil.copyDArray(xpts));
        shp.setYpts(ArrayUtil.copyDArray(ypts));
    }




}
