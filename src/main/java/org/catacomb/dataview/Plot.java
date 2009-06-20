package org.catacomb.dataview;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.PaintInstructor;
import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;


public class Plot implements PaintInstructor {

    Color bgColor;

    ArrayList<PlotElement> elements = new ArrayList<PlotElement>();

    String xlabel;
    String ylabel;

    double xmin = Double.NaN;
    double xmax = Double.NaN;

    double ymin = Double.NaN;
    double ymax = Double.NaN;

    Box wkBox;

    public Plot() {
        wkBox = new Box();
    }


    public static void main(String[] argv) {
        Plot p = new Plot();
        double[] xdat = {0.1, 0.6, 3.2, 5.8};
        double[] ydat = {4.5, 3, 2, 5};
        p.setData(xdat, ydat);
        p.makeImage(300, 300, "tst.png");
    }




    public void addLine(double[] xpts, double[] ypts, Color c) {
        addLine(xpts, ypts, c, "");
    }

    public void addLine(double[][] xy, Color c) {
        int n = xy.length;
        double[] xpts = new double[n];
        double[] ypts = new double[n];
        for (int i = 0; i < n; i++) {
            xpts[i] = xy[i][0];
            ypts[i] = xy[i][0];
        }
        addLine(xpts, ypts, c);
    }

    public void addPolygon(double[][] xy, Color c) {
        int n = xy.length;
        double[] xpts = new double[n+1];
        double[] ypts = new double[n+1];
        for (int i = 0; i < n; i++) {
            xpts[i] = xy[i][0];
            ypts[i] = xy[i][1];
        }
        xpts[n] = xy[0][0];
        ypts[n] = xy[0][1];
        addLine(xpts, ypts, c);
    }



    public void addPolygon(double[] xpts, double[] ypts, Color c) {
        elements.add(new PolygonPlotElement(xpts, ypts, c));
    }


    public void addLine(double[] xpts, double[] ypts, Color c, String s) {
        elements.add(new LinePlotElement(xpts, ypts, c, s));
    }

    public void addPoints(double[] xpts, double[] ypts, Color c) {
        elements.add(new PointsPlotElement(xpts, ypts, c));
    }


    public void addSmallPoints(double[] xpts, double[] ypts, Color c) {
        elements.add(new PointsPlotElement(xpts, ypts, c, 1, 3));
    }


    public void addDots(double[] xpts, double[] ypts, Color c) {
        elements.add(new PointsPlotElement(xpts, ypts, c, 1, 1));
    }

    public void setData(double[] xp, double[] yp) {
        elements.add(new LinePlotElement(xp, yp));
    }

    public void setXRange(double xl, double xh) {
        xmin = xl;
        xmax = xh;
    }

    public void setYRange(double yl, double hy) {
        ymin = yl;
        ymax = hy;
    }

    public void setXLabel(String s) {
        xlabel = s;
    }

    public void setYLabel(String s) {
        ylabel = s;
    }


    public void makeImage(int w, int h, String fnm) {
        makeImage(w, h, new File(fnm));
    }

    public void makeImage(int w, int h, File fout) {
        double[] xyxy = {xmin, ymin, xmax, ymax};
        makeImage(w, h, xyxy, fout);
    }



    public void makeImage(int w, int h, double[] xyxy, File fout) {
        Box b = new Box();

        if (Double.isNaN(xyxy[0]) ||Double.isNaN(xyxy[1]) || Double.isNaN(xyxy[2]) || Double.isNaN(xyxy[3])) {
            Box blim = getLimitBox();
            double dx = blim.getXmax() - blim.getXmin();
            double dy = blim.getYmax() - blim.getYmin();

            double f = 0.07;
            b.extendXTo(Double.isNaN(xyxy[0]) ? blim.getXmin() - f * dx : xyxy[0]);
            b.extendYTo(Double.isNaN(xyxy[1]) ? blim.getYmin() - f * dy : xyxy[1]);
            b.extendXTo(Double.isNaN(xyxy[2]) ? blim.getXmax() + f * dx : xyxy[2]);
            b.extendYTo(Double.isNaN(xyxy[3]) ? blim.getYmax() + f * dy: xyxy[3]);


        } else {
            b.extendTo(xyxy[0], xyxy[1]);
            b.extendTo(xyxy[2], xyxy[3]);
        }

        b.tidyLimits();
        makeImage(w, h, b, fout);
    }



    public void makeImage(int w, int h, Box b, File fout) {

        GraphMaker gm = new GraphMaker(w, h, bgColor);


        if (xlabel != null) {
            gm.setXAxisLabel(xlabel);
        }
        if (ylabel != null) {
            gm.setYAxisLabel(ylabel);
        }

        gm.setXRange(b.getXmin(), b.getXmax());
        gm.setYRange(b.getYmin(), b.getYmax());

        gm.drawData(this, fout);
    }


    public boolean antialias() {
        return true;
    }


    public Box getLimitBox() {

        Box b = new Box();
        for (PlotElement pe : elements) {
            pe.push(b);
        }
        return b;
    }

    public void autorange(boolean fixedAR) {
        Box b = getLimitBox();
        // E.info("box " + b + " " + fixedAR);
        b.enlarge(0.1);
        if (fixedAR) {
            double dx = b.getXmax() - b.getXmin();
            double dy = b.getYmax() - b.getYmin();
            if (dy > dx) {
                double cx = 0.5 * (b.getXmin() + b.getXmax());
                setXRange(cx - dy/2, cx + dy/2);
                setYRange(b.getYmin(), b.getYmax());
            } else {
                double cy = 0.5 * (b.getYmin() + b.getYmax());
                setXRange(b.getXmin(), b.getXmax());
                setYRange(cy - dx/2, cy + dx/2);
            }

        } else {
            setXRange(b.getXmin(), b.getXmax());
            setYRange(b.getYmin(), b.getYmax());
        }
    }


    public void instruct(Painter p) {
        for (PlotElement pe : elements) {
            pe.instruct(p);
        }

        int ileg = 0;
        //  E.info("painting " + elements.size());
        for (PlotElement pe : elements) {
            String s = pe.getLabel();
            if (s != null && s.length() > 0) {
                p.setColor(pe.getColor());
                p.paintLegend(ileg, s);
                ileg += 1;
            }
        }


    }


    public void addSortedLine(double[] xpts, double[] ypts, Color col) {
        // YUK
        // must make a getSortIndexes method since java beleives you should
        // be forced to make an arrya of comparable (x,y) objects....
        int np = xpts.length;
        ArrayList<Double> xa = new ArrayList<Double>();
        HashMap<Double, Double> dhm = new HashMap<Double, Double>();
        for (int i = 0; i < xpts.length; i++) {
            Double x = new Double(xpts[i]);
            Double y = new Double(ypts[i]);
            xa.add(x);
            dhm.put(x, y);
        }
        Collections.sort(xa);
        double[] xpa = new double[xpts.length];
        double[] ypa = new double[xpts.length];
        for (int i = 0; i < np; i++) {
            Double d = xa.get(i);
            xpa[i] = d.doubleValue();
            ypa[i] = dhm.get(d).doubleValue();
        }
        addLine(xpa, ypa, col);


    }


    public void addString(double x, double y, String sc) {
        elements.add(new StringPlotElement(x, y, sc));

    }
    public void addString(double x, double y, Color c, String sc) {
        elements.add(new StringPlotElement(x, y, c, sc));
    }

    public void addArrowString(double x, double y, String sc) {
        elements.add(new StringPlotElement(x, y, Color.white, sc, 10, -10));

    }
    public void addArrowString(double x, double y, Color c, String sc) {
        elements.add(new StringPlotElement(x, y, c, sc, 10, -10));
    }


    public void addRasterRow(double[] times, double[] vs, double vmin, double vmax, double y, double thr) {
        elements.add(new RasterRowElement(times, vs, vmin, vmax, y, thr));

    }


    public void setBackgroundColor(Color bg) {
        bgColor = bg;

    }

}

