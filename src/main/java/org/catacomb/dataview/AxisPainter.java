package org.catacomb.dataview;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.catacomb.graph.gui.WorldTransform;


public class AxisPainter {

    Graphics2D graphics;
    WorldTransform wt;


    public AxisPainter(Graphics2D g, WorldTransform t) {
        graphics = g;
        wt = t;
    }


    public void drawAxes() {
        int lm = wt.getLeftMargin();
        int bm = wt.getBottomMargin();
        int width = wt.getWidth();
        int height = wt.getHeight();
        graphics.setColor(Color.lightGray);

        graphics.fillRect(0, 0, lm, height);
        graphics.fillRect(0, height - bm, width, bm);
        graphics.setColor(Color.black);

        drawXAxis();
        drawYAxis();

    }


    public void labelAxes(String xlabel, String ylabel) {
        int lm = wt.getLeftMargin();
        int bm = wt.getBottomMargin();
        int width = wt.getWidth();
        int height = wt.getHeight();


        FontMetrics fm = graphics.getFontMetrics();


        if (xlabel != null) {

            int ilx = (width - lm) / 2 - fm.stringWidth(xlabel) / 2;
            int ily = height - 3;
            graphics.drawString(xlabel, lm + ilx, ily);
        }


        if (ylabel != null) {
            int ilx = 18;
            int ily = (height - bm) / 2 + fm.stringWidth(ylabel) / 2;


            AffineTransform at = graphics.getTransform();

            graphics.translate((double) ilx, (double) ily);
            graphics.rotate(-1 * Math.PI / 2);

            graphics.drawString(ylabel, 0, 0);


            graphics.setTransform(at);
        }
    }


    public final void drawXAxis() {

        int[] intervals = { 1, 2, 5 };
        int ntick = 5;

        int lm = wt.getLeftMargin();

        int bm = wt.getBottomMargin();


        int width = wt.getWidth();
        int height = wt.getHeight();


        graphics.drawLine(lm, height - bm, width, height - bm);

        double[] xr = wt.getXRange();

        double xran = Math.abs(xr[1] - xr[0]);
        double dx = 1.5 * xran / ntick;

        double log = Math.log(dx) / Math.log(10.);
        double powten = (int) Math.floor(log);
        int iiind = (int)(2.999 * (log - powten));

        int ii = intervals[iiind];
        dx = Math.pow(10.0, powten) * ii;

        int i0 = (int)(xr[0] / dx);
        int i1 = (int)(xr[1] / dx);

        for (int i = i0; i <= i1; i++) {
            double xx = i * dx;
            String lab = "0";
            if (i == 0) {
                // OK;

            } else if (dx >= 0.999 && dx < 1.e4) {
                lab = String.valueOf((int)(xx));
            } else {
                lab = String.valueOf((float)(xx));
            }
            int off = lab.length();
            off = 1 - 4 * off;
            if (i * dx < 0.0) {
                off -= 4;
            }

            int ix = (int)(lm + (width - lm) * (xx - xr[0]) / (xr[1] - xr[0]));

            graphics.drawString(lab, ix + off, height - bm + 20);
            graphics.drawLine(ix, height - bm, ix, height - bm + 5);

        }
    }


    public final void drawYAxis() {
        FontMetrics fm = graphics.getFontMetrics();

        int[] intervals = { 1, 2, 5 };
        int ntick = 5;


        int height = wt.getHeight();
        int lm = wt.getLeftMargin();

        int bm = wt.getBottomMargin();


        graphics.drawLine(lm, height - bm, lm, 0);

        double[] yr = wt.getYRange();
        double ylow = yr[0];
        double yhigh = yr[1];

        double yran = Math.abs(yhigh - ylow);
        double dy = 1.5 * yran / ntick;

        double log = Math.log(dy) / Math.log(10.);
        double powten = (int) Math.floor(log);
        int iiind = (int)(2.999 * (log - powten));

        int ii = intervals[iiind];
        dy = Math.pow(10.0, powten) * ii;

        int i0 = (int)(ylow / dy);
        int i1 = (int)(yhigh / dy);

        for (int i = i0; i <= i1; i++) {
            double yy = i * dy;
            String lab = "0";
            if (i == 0) {
                // OK;

            } else if (dy >= 0.999 && dy < 1.e4) {
                lab = String.valueOf((int)(yy));
            } else {
                lab = String.valueOf((float)(yy));
            }

            int iy = height - bm - (int)((height - bm) * (yy - ylow) / (yhigh - ylow));

            int off = fm.stringWidth(lab);

            graphics.drawString(lab, lm - 12 - off, iy + 4);
            graphics.drawLine(lm - 5, iy, lm, iy);


        }
    }


}
