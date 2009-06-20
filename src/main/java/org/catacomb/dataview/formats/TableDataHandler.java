package org.catacomb.dataview.formats;

import java.io.File;

import org.catacomb.datalish.Box;
import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;


public class TableDataHandler implements DataHandler {

    double max = Double.NaN;
    double min = Double.NaN;

    double[][] data;

    String[] columnNames;


    public DataHandler getCoHandler() {
        return null;
    }


    public int getContentStyle() {
        return DataHandler.STATIC;
    }


    public double[] getFrameValues() {
        // TODO Auto-generated method stub
        return null;
    }


    public String getMagic() {
        return "cctbl";
    }


    public double getMaxValue() {
        if (Double.isNaN(max)) {
            evalLims();
        }
        return max;
    }


    public double getMinValue() {
        if (Double.isNaN(min)) {
            evalLims();
        }
        return max;
    }


    private void evalLims() {
        if (data.length > 1 && data[0].length > 0) {
            max = data[1][0];
            min = max;
            for (int i = 1; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    double d = data[i][j];
                    if (max < d) {
                        max = d;
                    }
                    if (min > d) {
                        min = d;
                    }
                }
            }

        } else {
            max = 0.;
            min = 0.;
        }

    }





    public String[] getPlotNames() {
        return columnNames;
    }


    public String[] getViewOptions() {
        String[] sa = {"lines"};
        return sa;
    }


    public boolean hasData() {
        return (data != null && data.length > 0);
    }


    public void read(File f) {
        E.missing();
    }


    public void setFrame(int ifr) {

    }


    public void setPlot(String s) {
        E.info("time to set plot " + s);
    }


    public void setViewStyle(String s) {


    }


    public boolean antialias() {

        return false;
    }


    public Box getLimitBox() {
        Box b = new Box();
        b.extendTo(data[0][0], getMinValue());
        b.extendTo(data[0][data[0].length - 1], getMaxValue());
        return b;
    }


    public void instruct(Painter p) {
        p.setColorWhite();
        for (int i = 1; i < data.length; i++) {
            p.drawPolyline(data[0], data[i], data[0].length);
        }
    }


    public void setColumnNames(String[] cn) {
        columnNames = cn;
    }


    public void setData(double[][] db) {
        data = db;
    }


    public String getXAxisLabel() {
        // TODO Auto-generated method stub
        return null;
    }


    public String getYAxisLabel() {
        // TODO Auto-generated method stub
        return null;
    }


    public ViewConfig getViewConfig(String s) {
        // TODO Auto-generated method stub
        return null;
    }


    public void setZValue(double d) {
        // TODO Auto-generated method stub

    }

}
