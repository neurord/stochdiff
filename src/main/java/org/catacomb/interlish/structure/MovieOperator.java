package org.catacomb.interlish.structure;

import java.io.File;

import org.catacomb.interlish.report.Logger;


public interface MovieOperator {


    void reset();

    void start();

    void stop();

    void resume();

    void faster();

    void slower();

    void showFrame(int ifr);

    void pauseDePause();

    int getNFrame();

    void setMovieStateDisplay(MovieStateDisplay msd);

    void record(File f, Logger l);



}
