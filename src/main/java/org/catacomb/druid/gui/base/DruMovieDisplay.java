package org.catacomb.druid.gui.base;

import org.catacomb.datalish.SpriteAnimation;
import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.graph.gui.MovieDisplay;
import org.catacomb.interlish.structure.ModeController;
import org.catacomb.interlish.structure.MovieController;

import java.awt.Color;

public class DruMovieDisplay extends DruBorderPanel {
    // TODO implement something for getting movie in;

    static final long serialVersionUID = 1001;

    MovieDisplay movieDisplay;

    MovieController movieController;


    public DruMovieDisplay(int w, int h) {

        movieDisplay = new MovieDisplay(w, h-20);

        addDComponent(movieDisplay, DBorderLayout.CENTER);

        //      addSunkenBorder();
    }

    public void setModeController(ModeController mc) {
        mc.addModeSettable(movieDisplay);
    }

    public void setBg(Color c) {
        super.setBg(c);
        movieDisplay.setBg(c);
    }


    public void setMovieController(MovieController movc) {
        movieController = movc;
        movc.setMovieOperator(movieDisplay);
    }


    public void setMovie(SpriteAnimation sanim) {
        movieDisplay.setMovie(sanim);
        movieController.syncFromOperator();
    }


    public void viewChanged() {
        movieDisplay.viewChanged();
    }



    public void attachGraphicsController(Object obj) {
        movieDisplay.attach(obj);
    }



    public void setXRange(double low, double high) {
        movieDisplay.setXRange(low, high);
    }


    public void setLimits(double[] xyxy) {
        movieDisplay.setLimits(xyxy);
    }


    public double[] getXRange() {
        return movieDisplay.getXRange();
    }

    public double[] getYRange() {
        return movieDisplay.getYRange();
    }

    public void setFixedAspectRatio(double ar) {
        movieDisplay.setFixedAspectRatio(ar);
    }

    public void reframe() {
        movieDisplay.reframe();
    }

    public void reluctantReframe() {
        movieDisplay.reluctantReframe();
    }




}
