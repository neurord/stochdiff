package org.catacomb.druid.gui.base;

import java.awt.Color;

import org.catacomb.graph.gui.DrawingCanvas;
import org.catacomb.interlish.structure.GraphicsView;
import org.catacomb.interlish.structure.Presentable;
import org.catacomb.interlish.structure.RangeWatcher;
import org.catacomb.report.E;


public class DruDrawingCanvas extends DruPanel implements GraphicsView {
    static final long serialVersionUID = 1001;

    DrawingCanvas drawingCanvas;


    public DruDrawingCanvas(int w, int h) {
        super();

        drawingCanvas = new DrawingCanvas(w, h);

        setSingle();
        addDComponent(drawingCanvas);
    }


    public void setBackgroundColor(Color c) {
        super.setBackgroundColor(c);
        drawingCanvas.setBackgroundColor(c);
    }

    public void setGridColor(Color c) {
        drawingCanvas.setGridColor(c);
    }

    public void setAxisColor(Color c) {
        drawingCanvas.setAxisColor(c);
    }

    public void setMode(int imode) {
    }

    public void viewChanged() {
        drawingCanvas.viewChanged();
    }

    public void setMouseMode(String s) {
        drawingCanvas.setMouseMode(s);
    }

    public void setOnGridAxes() {
        drawingCanvas.setOnGridAxes();
    }

    public void setThreeD() {
        drawingCanvas.setThreeD();
    }

    public boolean isAntialiasing() {
        return drawingCanvas.isAntialiasing();
    }

    public void setAntialias(boolean b) {
        drawingCanvas.setAntialias(b);
    }

    public void reframe() {
        drawingCanvas.reframe();
    }

    public void attachGraphicsController(Object obj) {
        drawingCanvas.attach(obj);
        if (obj instanceof Presentable) {
            ((Presentable)obj).setPresenter(this);
        }
    }


    public void setXRange(double low, double high) {
        drawingCanvas.setXRange(low, high);
    }

    public void setFixedAspectRatio(double ar) {
        drawingCanvas.setFixedAspectRatio(ar);
    }



    public void addRangeWatcher(RangeWatcher rw) {
        E.missing("range watcher on drawing canvas");
    }


    public double[][] getProjectionMatrix() {
        return drawingCanvas.getProjectionMatrix();
    }


    public void setRollCenter(double x, double y, double z) {
        drawingCanvas.setRollCenter(x, y, z);

    }


    public double[] get3Center() {
        return drawingCanvas.get3Center();
    }

    public double[] get2Center() {
        return drawingCanvas.get2Center();
    }


    public void turn(double d) {
        drawingCanvas.turn(d);

    }


    public void setShowGrid(boolean b) {
        drawingCanvas.setShowGrid(b);

    }


    public void setFourMatrix(double[] fm) {
        drawingCanvas.setFourMatrix(fm);

    }

    public double[] getFourMatrix() {
        return drawingCanvas.getFourMatrix();
    }


}
