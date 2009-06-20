package org.catacomb.interlish.structure;


public interface GraphicsView extends Presenter {


    void attachGraphicsController(Object obj);

    void setXRange(double low, double high);

    void setFixedAspectRatio(double ar);

    void addRangeWatcher(RangeWatcher rw);

}
