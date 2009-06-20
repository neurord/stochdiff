package org.catacomb.dataview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.catacomb.graph.gui.PaintInstructor;
import org.catacomb.graph.gui.Painter;
import org.catacomb.graph.gui.WorldTransform;
import org.catacomb.report.E;


// this runs headlessly, and draws a graph on a given graphics device
// it uses a paint instructor like the normal catacomb graphs, but
// none of the window stuff


public class GraphMaker {

    String xlabel = null;
    String ylabel = null;
    double xmin = 0.;
    double xmax = 1.;
    double ymin = 0.;
    double ymax = 1.;

    Graphics2D graphics;
    Painter painter;

    int leftMargin = 70;
    int bottomMargin = 48;
    int topMargin = 0;
    int rightMargin = 0;

    int width;
    int height;


    BufferedImage image;

    public GraphMaker(int w, int h, Color cbg) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        graphics = ge.createGraphics(image);

        if (cbg != null) {
            graphics.setColor(cbg);
        } else {
            graphics.setColor(Color.gray);
        }
        graphics.fillRect(0, 0, w, h);
        width = w;
        height = h;

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
    }



    public void setXAxisLabel(String xl) {
        xlabel = xl;
    }

    public void setYAxisLabel(String yl) {
        ylabel = yl;
    }

    public void setXRange(double a, double b) {
        xmin = a;
        xmax = b;
    }

    public void setYRange(double a, double b) {
        ymin = a;
        ymax = b;

    }



    public void drawData(PaintInstructor pin, File fout) {
        WorldTransform wt = new WorldTransform();
        wt.setMargins(leftMargin, rightMargin, bottomMargin, topMargin);
        wt.setCanvasSize(width, height);
        wt.setXYXYLimits(xmin, ymin, xmax, ymax);

        painter = new Painter(wt);
        painter.setGraphics(graphics);
        pin.instruct(painter);

        AxisPainter ap = new AxisPainter(graphics, wt);
        ap.drawAxes();
        ap.labelAxes(xlabel, ylabel);


        try {
            ImageIO.write(image, "PNG", fout);

        } catch (Exception ex) {
            E.error("cant write " + fout + " " + ex);
        }

    }




}
