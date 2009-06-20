package org.catacomb.dataview.formats;

import java.io.File;

import org.catacomb.datalish.Box;
import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.graph.gui.Geom;
import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;
import org.catacomb.util.ArrayUtil;


public class Mesh2plusTimeDisplay implements DataHandler {

    double zval = 0.;

    double zrange = 0.1;



    String[] plotNames;

    double[] frameValues;

    double[][][] mesh;
    double[][][] data;

    boolean[] mask;

    double[][][] innerMesh;

    int varIdx;
    int frameIdx;

    private Box meshBox;

    String plot;

    String viewStyle = "density";

    TableDataHandler coHandler = new TableDataHandler();


    double zMask = -999.;

    public void setZValue(double d) {
        zval = d;
    }


    public String getMagic() {
        return "cctdif2d";
    }


    public void read(File f) {
        TableDataReader tdr = new TableDataReader(coHandler);
        MeshDataReader mdr = new MeshDataReader(f, tdr);
        mdr.read();
        tdr.fix();

        mesh = mdr.getMesh();

        mask = new boolean[mesh.length];
        for (int i = 0; i < mask.length; i++) {
            mask[i] = true;
        }


        data = mdr.getData();
        frameValues = mdr.getFrameValues();

        String[] vn = mdr.getValueNames();
        plotNames = new String[1 + vn.length];
        plotNames[0] = "mesh";
        for (int i = 0; i < vn.length; i++) {
            plotNames[1 + i] = vn[i];
        }
    }



    public void makeMeshMask() {
        int nel = mesh.length;
        double[] d = new double[nel];
        double md = 1.e6;
        double xcen = 0.;
        for (int i = 0; i < nel; i++) {
            double x = mesh[i][2][0] - zval;
            d[i] = x;
            if (Math.abs(x) < md) {
                md = Math.abs(x);
                xcen = x;
            }
        }
        mask = new boolean[nel];
        for (int i = 0; i < nel; i++) {
            if (Math.abs(d[i] - xcen) < 0.5 * zrange) {
                mask[i] = true;
            } else {
                mask[i] = false;
            }
        }

        zMask = zval;
    }









    public String[] getPlotNames() {
        return plotNames;
    }


    public void setPlot(String s) {
        varIdx = varidx(s);
    }

    private int varidx(String s) {
        int dv = -1;
        for (int i = 0; i < plotNames.length; i++) {
            if (plotNames[i].equals(s)) {
                dv = i - 1; // ADHOC
                break;
            }
        }
        return dv;
    }



    public double[] getFrameData(String s, int fidx) {
        int vi = varidx(s);
        double[] ret = data[fidx][vi];
        return ret;
    }


    public void setDisplayVariable(int i) {
        varIdx = i;
    }


    public double getMinValue() {
        int vi = varIdx;
        if (vi < 0) {
            vi = 0;
        }
        return ArrayUtil.minD(data[frameIdx][vi]);

    }


    public double getMaxValue() {
        int vi = varIdx;
        if (vi < 0) {
            vi = 0;
        }

        return ArrayUtil.maxD(data[frameIdx][vi]);
    }


    public boolean antialias() {
        // TODO Auto-generated method stub
        return false;
    }


    private void makeInnerMesh() {
        innerMesh = new double[mesh.length][2][];
        for (int i = 0; i < mesh.length; i++) {
            double[][] di = Geom.innerPolygon(mesh[i][0], mesh[i][1]);
            innerMesh[i][0] = di[0];
            innerMesh[i][1] = di[1];
        }
    }


    public void instruct(Painter p) {
        if (varIdx < 0) {
            instructMesh(p);
        } else {
            instructAreas(p);
        }
    }


    public void instructMesh(Painter p) {

        if (Math.abs(zMask - zval) > 0.1 * zrange) {
            makeMeshMask();
        }


        int nel = mesh.length;
        p.setColorWhite();

        for (int i = 0; i < nel; i++) {
            if (mask[i]) {
                double[] xb = mesh[i][0];
                double[] yb = mesh[i][1];

                p.drawPolygon(xb, yb, xb.length);
            }
        }

        if (innerMesh == null) {
            makeInnerMesh();
        }
        p.setColorCyan();

        /*
        for (int i = 0; i < nel; i++) {
           double[] xb = innerMesh[i][0];
           double[] yb = innerMesh[i][1];

          //  p.drawPolygon(xb, yb, xb.length);
        }
        */
    }



    public void instructAreas(Painter p) {

        if (Math.abs(zMask - zval) > 0.01) {
            makeMeshMask();
        }


        if (viewStyle.equals("density")) {
            instructDensity(p);
        } else {
            E.info("missing code - drawing density");
            instructDensity(p);
        }
    }


    public void instructDensity(Painter p) {
        if (frameIdx >= 0 && frameIdx < data.length && varIdx >= 0 && varIdx < data[frameIdx].length) {
            double[] dat = data[frameIdx][varIdx];

            p.drawColoredCells(mesh, dat);
            //E.missing("check prior change to add mask");
            // p.drawColoredCells(mesh, dat, mask);
        }
    }




    public Box getLimitBox() {
        if (meshBox == null) {
            if (mesh != null) {
                meshBox = new Box();
                for (int i = 0; i < mesh.length; i++) {
                    meshBox.push(mesh[i][0], mesh[i][1]);
                }
            }
        }
        return meshBox;
    }


    public double[] getFrameValues() {
        return frameValues;
    }

    public int getContentStyle() {
        return FRAMES2D;
    }


    public void setFrame(int ifr) {
        frameIdx = ifr;
    }


    public String[] getViewOptions() {
        String[] ret = {"density", "particles"};
        return ret;
    }


    public void setViewStyle(String s) {
        viewStyle = s;
    }


    public DataHandler getCoHandler() {
        return coHandler;
    }


    public boolean hasData() {
        return true;
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

}
