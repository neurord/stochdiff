
package org.catacomb.graph.drawing;

import org.catacomb.be.ReReferencable;
import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;


import java.awt.Color;



public abstract class FixedDrawingComponent implements ReReferencable, Cloneable {


    public String lineColor;
    public String lineWidth;


    protected Color p_color;
    protected double p_width;
    private int p_widthStyle;


    public final static int PIXELS = 0;
    public final static int RELATIVE = 1;


    public String fillColor;

    private Color p_fill;


    private int p_closure;
    public final static int OPEN = 1;
    public final static int CLOSED = 2;
    public final static int FILLED = 3;




    public FixedDrawingComponent() {
        p_width=1.;
        p_color = Color.black;
    }


    public FixedDrawingComponent copy() {
        Object ret = null;
        try {
            ret = clone();
        } catch (Exception ex) {
            E.error("whats this " + ex);
        }
        return (FixedDrawingComponent)ret;
    }


    public boolean isWrapper() {
        return false;
    }


    public void setColor(Color c) {
        p_color = c;
    }


    public void setWidth(double d) {
        p_width = d;
        p_widthStyle = PIXELS;
    }



    public Color getColor() {
        return p_color;
    }

    public void setLineColor(Color c) {
        p_color = c;
    }



    public void setLineWidth(double d) {
        p_width = d;
    }

    public double getWidth() {
        return p_width;
    }

    public boolean widthIsPixels() {
        return (p_widthStyle == PIXELS);
    }

    public void setFillColor(Color c) {
        p_fill = c;
        p_closure = FILLED;
    }

    public Color getFillColor() {
        return p_fill;
    }


    public abstract void instruct(Painter p, double offx, double offy, double scale);



    public void reReference() {

        p_widthStyle = PIXELS; // parseWidthStyle(lineWidth);

        p_width = parseWidth(lineWidth);

        p_color = parseColor(lineColor);

        if (fillColor == null) {
            p_closure = CLOSED;
        } else {
            p_closure = FILLED;
            p_fill = parseColor(fillColor);
        }
    }


    public boolean isOpen() {
        return (p_closure == OPEN);
    }


    public boolean isClosed() {
        return (p_closure == CLOSED);
    }


    public boolean isFilled() {
        return (p_closure == FILLED);
    }


    public void setClosed() {
        p_closure = CLOSED;
    }

    public void setOpen() {
        p_closure = OPEN;
    }

    public void setFilled() {
        p_closure = FILLED;
    }


    public Color parseColor(String s) {
        Color ret = Color.black;
        if (s != null && s.startsWith("#") && s.length() == 7) {
            String s1 = s.substring(1, s.length());
            int icol = Integer.parseInt(s1, 16);
            ret = new Color(icol);

        } else if (s == null || s.length() == 0) {

        } else {
            E.error(" - cant read color " + s);
        }
        return ret;
    }




    public int parseWidthStyle(String win) {
        String w = win;
        int iret = PIXELS;
        if (w == null) {
            // leave as is;

        } else {
            w = w.trim();
            if (w.endsWith("px")) {
                iret = PIXELS;
            } else {
                iret = RELATIVE;
            }
        }
        return iret;
    }


    public double parseWidth(String win) {
        String w = win;
        double ret = 1.;
        if (w != null) {
            w = w.trim();

            if (w.endsWith("px")) {
                w = w.substring(0, w.length()-2);
            }

            ret = (new Double(w)).doubleValue();
        }
        return ret;
    }


    public Shape makeShape() {
        Shape shp = new Shape();

        shp.setFillColor(Color.black);
        if (isClosed()) {
            shp.setClosure(Shape.CLOSED);

        } else if (isOpen()) {
            shp.setClosure(Shape.OPEN);

        } else if (isFilled()) {
            shp.setClosure(Shape.FILLED);
            shp.setFillColor(getFillColor());
        }

        shp.setLineColor(p_color);
        shp.setLineWidth(p_width);
        applyToShape(shp);
        shp.makePoints();
        shp.syncArrays();
        return shp;
    }


    public abstract void applyToShape(Shape shp);


}
