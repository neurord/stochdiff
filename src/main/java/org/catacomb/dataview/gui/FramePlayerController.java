package org.catacomb.dataview.gui;



import java.awt.image.BufferedImage;
import java.io.File;

import org.catacomb.druid.gui.edit.DruButton;
import org.catacomb.druid.gui.edit.DruSlider;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.Controller;
import org.catacomb.interlish.structure.FrameDisplay;
import org.catacomb.movie.gif.AnimatedGifEncoder;
import org.catacomb.report.E;
import org.catacomb.util.AWTUtil;


public class FramePlayerController implements Controller {


    @IOPoint(xid = "slider")
    public DruSlider frameSlider;

    @IOPoint(xid = "PauseButton")
    public DruButton pauseButton;

    String sourcePath;
    String displayPath;

    int[] indexes;
    String[] displayValues;


    // DataviewController dataviewController;

    // DataExtractor dataExtractor;


    double speed = 1.;

    int shownFrame;

    FramePlayer framePlayer;

    boolean isPaused;

    FrameDisplay frameDisplay;


    public FramePlayerController() {

    }



    public void attached() {
        //   E.info("attached fp controller " + frameSlider);
    }


    public void applyData(double[] dat) {
        displayValues = new String[dat.length];
        indexes = new int[dat.length];
        for (int i = 0; i < dat.length; i++) {
            displayValues[i] = String.format("%.4g", new Double(dat[i]));
            indexes[i] = i;
        }
        frameSlider.setValues(displayValues);
        showFrame(0);
    }


    public void showFrame(int iframein) {
        int iframe = iframein;
        if (iframe < 0) {
            iframe = 0;
        }

        if (iframe >= indexes.length) {
            iframe = indexes.length - 1;
        }


        if (frameSlider != null) {
            frameSlider.showValue(iframe);
        }
        shownFrame = iframe;
        if (frameDisplay != null) {
            frameDisplay.showFrame(iframe);
        }
    }


    public void show(Object obj) {
    }



    public void sliderMoved() {
        int ival = frameSlider.getValue();
        showFrame(ival);
    }


    public void rewind() {
        stop();
        showFrame(0);
    }



    public void pause() {
        if (isPaused) {
            dePause();
            start();

        } else {
            rePause();
            stop();
        }
    }


    public void dePause() {
        isPaused = false;
        pauseButton.setLabelText(" pause ");
    }


    private void rePause() {
        isPaused = true;
        pauseButton.setLabelText("resume");
    }


    public void play() {
        stop();
        rewind();
        start();
        dePause();
    }


    private void start() {
        if (indexes != null) {
            framePlayer = new FramePlayer(this);
            framePlayer.start();
        }
    }


    public void stop() {
        if (framePlayer != null) {
            framePlayer.stop();
        }
    }



    public void faster() {
        speed *= 1.3;
    }


    public void slower() {
        speed /= 1.3;
    }


    public boolean canAdvance() {
        return (indexes != null && shownFrame < indexes.length - 1);
    }


    public void advance() {
        showFrame(shownFrame + 1);
    }


    public double getSpeed() {
        return speed;
    }



    public void record() {
        E.missing();
        /*
         * File f = FileChooser.getWriteFile(); if (f != null) {
         * makeAnimatedGif(f); }
         */

    }


    public void miniRecord() {
        E.missing();
        /*
         * File f = FileChooser.getWriteFile(); if (f != null) {
         * makeMiniAnimatedGif(f, 160, 100); }
         */
    }



    public void makeMovie(File f) {
        makeAnimatedGif(f);
    }


    public void makeThumbnailMovie(File f) {
        makeMiniAnimatedGif(f, 160, 160);
    }



    public void makeAnimatedGif(File f) {
        stop();
        rewind();


        AnimatedGifEncoder enc = new AnimatedGifEncoder();

        enc.start(f);
        enc.setDelay(160); // ms

        int ifr = 0;
        E.info("animated gif - frame " + ifr);
        // enc.addFrame(dataviewController.getBufferedImage(1));

        while (canAdvance()) {
            advance();
            ifr += 1;
            E.info("animated gif - frame " + ifr);
            // enc.addFrame(dataviewController.getBufferedImage(1));
        }

        enc.finish();

    }



    public void makeMiniAnimatedGif(File f, int wsclin, int hsclin) {
        int wscl = wsclin;
        int hscl = hsclin;
        stop();
        rewind();


        AnimatedGifEncoder enc = new AnimatedGifEncoder();

        enc.start(f);
        enc.setDelay(200); // 2 frame per sec

        int nfr = indexes.length;

        int stepsize = nfr / 10;
        if (stepsize < 1) {
            stepsize = 1;
        }

        int ifr = 0;




        while (canAdvance()) {
            E.info("animated gif - frame " + ifr);
            BufferedImage bim = null; // dataviewController.getBufferedImage(ithick);

            if (ifr == 0) {
                int wf = bim.getWidth();
                int hf = bim.getHeight();
                double fo = ((float)wscl) / hscl;
                double ff = ((float)wf) / hf;
                if (ff > fo) {
                    hscl = (int)(wscl / ff);
                } else {
                    wscl = (int)(hscl * ff);
                }
                //      ithick = ((wf + wscl / 2) / wscl);

                // bim = dataviewController.getBufferedImage(ithick);
            }

            BufferedImage bufim = AWTUtil.getScaledBufferedImage(bim, wscl, hscl);

            enc.addFrame(bufim);
            ifr += 1;

            for (int i = 0; i < stepsize; i++) {
                if (canAdvance()) {
                    advance();
                }
            }
        }

        enc.finish();

    }


    public void setFrameDisplay(FrameDisplay fd) {
        frameDisplay = fd;

    }


}
