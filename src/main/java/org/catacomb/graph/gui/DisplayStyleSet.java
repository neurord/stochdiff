package org.catacomb.graph.gui;


import org.catacomb.interlish.content.ColorTable;
import org.catacomb.interlish.structure.ColorMapped;
import org.catacomb.interlish.structure.Colored;
import org.catacomb.interlish.structure.IDable;
import org.catacomb.report.E;


import java.awt.Color;


public class DisplayStyleSet implements Colored, ColorMapped, IDable {

    public String id;

    public final static String[] styleNames= {"solid", "dotted", "short dash",
                                 "long dash", "dot-dash"
                                             };


    int lineStyle;

    public final static String[] widthNames = {"1", "1.5", "2", "3", "4"};
    public final static double[] widths = {1., 1.5, 2., 3., 4.};
    int lineWidthIndex;
    double lineWidth;


    static Color defaultColor = new Color(201, 202, 203);

    Color lineColor;

    String colorTableName;
    ColorTable r_colorTable;

    boolean assignedLine;
    boolean assignedColor;

    static int lineStyleCounter;
    static int colorCounter;


    public DisplayStyleSet() {
        lineStyle = 0;
        setLineWidthName("1");
        setLineColor(defaultColor);
        setColorTable("default");
        assignedLine = false;
        assignedColor = false;
    }

    public DisplayStyleSet(String s) {
        this();
        setID(s);
    }

    public void setID(String s) {
        id = s;
    }

    public String getID() {
        return id;
    }


    public String toString() {
        return id;
    }


    public Color getColor() {
        return lineColor;
    }


    public final void setLineStyle(String s) {
        int isty = -1;
        for (int i = 0; i < styleNames.length; i++) {
            if (styleNames[i].equals(s)) {
                isty = i;
                break;
            }
        }
        if (isty < 0) {
            lineStyle = 0;
            E.warning("unrecognized style " + s);

        } else {
            lineStyle = isty;
        }
        assignedLine = true;
    }

    public final void setLineWidthName(String s) {
        int isty = -1;
        for (int i = 0; i < widthNames.length; i++) {
            if (widthNames[i].equals(s)) {
                isty = i;
                break;
            }
        }
        if (isty < 0) {
            isty = 0;
            E.warning("unrecognized width " + s);
        }
        lineWidthIndex = isty;
        lineWidth = widths[isty];
    }


    public int getLineWidthIndex() {
        return lineWidthIndex;
    }


    public static double[] getIndexedWidths() {
        return widths;
    }

    public static String[] getStyleNames() {
        return styleNames;
    }


    public final void setLineColor(Color c) {
        lineColor = c;
        assignedColor = true;
    }


    public final void setColorTable(String s) {
        colorTableName = s;
        r_colorTable = null;
    }

    public final void setColorTable(ColorTable ct) {
        r_colorTable = ct;
        colorTableName = ct.getName();
    }

    public String getStringLineStyle() {
        return styleNames[lineStyle];
    }

    public int getLineStyleIndex() {
        return lineStyle;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public ColorTable getColorTable() {
        return r_colorTable;
    }


    public String getColorTableName() {
        return colorTableName;
    }


    public String getStringLineWidth() {
        return widthNames[lineWidthIndex];
    }


    public void assignIfUnset() {
        if (assignedLine) {
            // leave as is;
        } else {
            lineStyle = lineStyleCounter;
            lineStyleCounter = (lineStyleCounter + 1) % (styleNames.length);
            assignedLine = true;
        }

        if (assignedColor) {

        } else {
            Color c = new Color((int)(255 * Math.random()),(int)(255 * Math.random()),(int)(255 * Math.random()));
            setLineColor(c);
            assignedColor = true;
        }

    }



}
