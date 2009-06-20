
package org.catacomb.dataview;



import org.catacomb.dataview.gui.FramePlayer;
import org.catacomb.druid.gui.edit.DruButton;
import org.catacomb.druid.gui.edit.DruSlider;
import org.catacomb.interlish.structure.*;
import org.catacomb.movie.gif.AnimatedGifEncoder;
import org.catacomb.numeric.data.DataExtractor;
import org.catacomb.report.E;
import org.catacomb.util.AWTUtil;


import java.awt.image.BufferedImage;
import java.io.File;


/// XXXXXXXXXXXXX dont use
// this is being replaced by code in gui, but without direct
// refs to dataviewController etc


public class FrameController implements Controller, GUISourced {

    DruSlider frameSlider;

    String sourcePath;
    String displayPath;

    int[] indexes;
    String[] displayValues;


    DataviewController dataviewController;

    DataExtractor dataExtractor;


    double speed = 1.;

    int shownFrame;

    FramePlayer framePlayer;

    boolean isPaused;

    DruButton pauseButton;



    public FrameController(String src, String dply) {
        super();
        sourcePath = src;
        displayPath = dply;
    }


    public void setDataviewController(DataviewController dvc) {
        dataviewController = dvc;
    }

    public void setDataSource(DataExtractor dex) {
        dataExtractor = dex;
        if (frameSlider != null) {
            applyData();
        }
    }

    public void attached() {
    }


    public void markNeeded() {
        dataExtractor.mark(sourcePath);
        dataExtractor.mark(displayPath);
    }



    public void applyData() {
        indexes = dataExtractor.getIntVector(sourcePath);
        double[] adv = dataExtractor.getVector(displayPath);

        displayValues = new String[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            displayValues[i] = "" + adv[indexes[i]];

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
            iframe = indexes.length-1;
        }

        dataviewController.showFrame(indexes[iframe]);   // same method name - diff arg meaning POSERR
        if (frameSlider != null) {
            frameSlider.showValue(iframe);
        }
        shownFrame = iframe;
    }



    public String getGUITargets() {
        return "Slider PauseButton";
    }


    public String getGUISources() {
        return "*";
    }

    @SuppressWarnings("unused")
    public void show(Object obj) {
    }


    public void setSlider(DruSlider dfs) {
        frameSlider = dfs;
        if (dataExtractor != null) {
            applyData();
        }
    }


    public void setPauseButton(DruButton db) {
        pauseButton = db;
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
            framePlayer = null; // new FramePlayer(this);
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
        return (indexes != null && shownFrame < indexes.length-1);
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
        File f = FileChooser.getWriteFile();
        if (f != null) {
        makeAnimatedGif(f);
            }
            */

    }


    public void miniRecord() {
        E.missing();
        /*
        File f = FileChooser.getWriteFile();
        if (f != null) {
        makeMiniAnimatedGif(f, 160, 100);
            }
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
        enc.setDelay(160);   // ms

        int ifr = 0;
        E.info("animated gif - frame " + ifr);
        enc.addFrame(dataviewController.getBufferedImage(1));

        while (canAdvance()) {
            advance();
            ifr += 1;
            E.info("animated gif - frame " + ifr);
            enc.addFrame(dataviewController.getBufferedImage(1));
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
        enc.setDelay(200);   // 2 frame per sec

        int nfr = indexes.length;

        int stepsize = nfr / 10;
        if (stepsize < 1) {
            stepsize = 1;
        }

        int ifr = 0;

        int ithick = 1;

        while (canAdvance()) {
            E.info("animated gif - frame " + ifr);
            BufferedImage bim = dataviewController.getBufferedImage(ithick);

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
                ithick = ((wf + wscl/2) / wscl);

                bim = dataviewController.getBufferedImage(ithick);
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


}


