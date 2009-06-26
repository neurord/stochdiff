package org.catacomb.druid.chunk;

import org.catacomb.druid.dialog.Dialoguer;
import org.catacomb.druid.gui.edit.DruSlider;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.report.Logger;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.interlish.structure.MovieController;
import org.catacomb.interlish.structure.MovieOperator;
import org.catacomb.report.E;

import java.io.File;


public class MoviePlayController implements Controller, MovieController {


    @IOPoint(xid="slider")
    public DruSlider slider;


    MovieOperator moperator;


    public MoviePlayController() {

    }



    public void setMovieOperator(MovieOperator mop) {
        moperator = mop;
        moperator.setMovieStateDisplay(this);
        if (slider != null) {
            slider.setNFrame(moperator.getNFrame());
        }
    }


    public void syncFromOperator() {
        //  E.info("movctrl sync from op " + moperator.getNFrame());
        slider.setNFrame(moperator.getNFrame());
    }


    public void attached() {
        if (slider != null && moperator != null) {
            E.info("slider setting nframe " + moperator.getNFrame());
            slider.setNFrame(moperator.getNFrame());

        }
    }

    public void play() {
        moperator.start();
    }

    public void rewind() {
        moperator.reset();
    }


    public void pause() {
        moperator.pauseDePause();
    }

    public void faster() {
        moperator.faster();
    }

    public void slower() {
        moperator.slower();
    }

    public void stop() {
        moperator.stop();
    }

    public void record() {
        File f = Dialoguer.getFileToWrite("output");
        if (f != null) {

            Logger pl = Dialoguer.getProgressLogger();

            moperator.record(f, pl);
        }
    }


    public void jumpToFrame(int ifr) {
        E.info("mop jtf " + ifr);
        moperator.showFrame(ifr);
    }


    public void frameChangedTo(int ifr, String desc) {
        slider.pointShown(ifr, desc);
    }


    public void sliderMoved() {
        int ifr = slider.getValue();
        moperator.showFrame(ifr);
    }
    public void sliderMoved(String s) {
        sliderMoved();
    }

    public void setIsPaused() {
        // TODO text on pause/resume button;
    }



}