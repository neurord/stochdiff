package org.catacomb.graph.gui;

import org.catacomb.datalish.SpriteAnimation;
import org.catacomb.interlish.report.Logger;
import org.catacomb.interlish.structure.ModeSettable;
import org.catacomb.interlish.structure.MovieOperator;
import org.catacomb.interlish.structure.MovieStateDisplay;
import org.catacomb.movie.gif.AnimatedGifEncoder;
import org.catacomb.report.E;
import org.catacomb.util.AWTUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;

import java.io.File;



// REFAC too much in here - shouldn't be exending JPanel


public class MovieDisplay extends BasePanel
    implements ModeSettable, MovieOperator {


    static final long serialVersionUID = 1001;

    MoviePaintInstructor moviePaintInstructor;
    MovieStateDisplay movieStateDisplay;

    PickWorldCanvas pwCanvas;
    BasePanel controlPanel;
    Dimension prefDim;
    Color bgColor;


    double speed = 1.;
    boolean isPaused = false;
    MovieFramePlayer movieFramePlayer;
    int nFrame;
    int shownFrame;



    public MovieDisplay(int w, int h) {
        super();
        bgColor = Color.gray;

        prefDim = new Dimension(w, h);
        setPreferredSize(prefDim);
        pwCanvas = new PickWorldCanvas(w, h, true);
        pwCanvas.setOnGridAxes();
        pwCanvas.setFixedAspectRatio(1.0);

        setLayout(new BorderLayout(0, 0));
        add("Center", pwCanvas);
    }


    public Dimension getPreferredSize() {
        return prefDim;
    }


    public void setBg(Color c) {
        bgColor = c;
        pwCanvas.setBg(c);
    }



    public void setMode(String dom, String mod) {
        pwCanvas.setMode(dom, mod);
    }


    public void setMode(String dom, boolean b) {
        pwCanvas.setMode(dom, b);
    }



    public void setMovie(SpriteAnimation sanim) {
        setMoviePaintInstructor(new SpriteMoviePainter(sanim));
        nFrame = moviePaintInstructor.getNFrames();
        if (nFrame > 0) {
            showFrame(nFrame-1);
        }
    }



    public void setMoviePaintInstructor(MoviePaintInstructor mpi) {
        moviePaintInstructor = mpi;
        pwCanvas.setPaintInstructor(mpi);
    }



    public void attach(Object obj) {
        boolean done = false;

        if (obj instanceof MoviePaintInstructor) {
            setMoviePaintInstructor((MoviePaintInstructor)obj);
            done = true;
        }


        if (!done) {
            E.error("cant attach " + obj + " to a data Display");
        }
    }



    public void setLimits(double[] xyxy) {
        pwCanvas.setXRange(xyxy[0], xyxy[2]);
        pwCanvas.setYRange(xyxy[1], xyxy[3]);
    }


    public void setXRange(double low, double high) {
        pwCanvas.setXRange(low, high);
    }


    public double[] getXRange() {
        return pwCanvas.getXRange();
    }


    public double[] getYRange() {
        return pwCanvas.getYRange();
    }


    public void setFixedAspectRatio(double ar) {
        pwCanvas.setFixedAspectRatio(ar);
    }


    public void viewChanged() {
        if (pwCanvas != null) {
            pwCanvas.repaint();
        }
    }


    public void reframe() {
        pwCanvas.reframe();
    }


    public void advanceToFrame(int ifr) {
        if (moviePaintInstructor != null) {
            moviePaintInstructor.advanceToFrame(ifr);
            shownFrame = ifr;
            displayFrame();
        }
    }


    public void showFrame(int ifr) {
        if (moviePaintInstructor != null) {
            moviePaintInstructor.setFrame(ifr);
            shownFrame = ifr;
            displayFrame();
        }
    }

    private void displayFrame() {
        pwCanvas.repaint();
        nFrame = moviePaintInstructor.getNFrames();

        if (movieStateDisplay != null) {
            movieStateDisplay.frameChangedTo(shownFrame,
                                             moviePaintInstructor.getFrameDescription(shownFrame));
        }
    }


    public int getNFrame() {
        return nFrame;
    }



    public void reset() {
        stop();
        showFrame(0);
    }

    public void play() {
        stop();
        reset();
        start();
        dePause();
    }


    public void resume() {
        dePause();
        start();
    }


    public void pause() {
        pauseDePause();
    }

    public void pauseDePause() {
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
    }

    private void rePause() {
        isPaused = true;
    }



    public void start() {
        nFrame = moviePaintInstructor.getNFrames();
        if (canAdvance()) {
            // start from where we are;
        } else {
            reset();
        }
        if (nFrame > 0) {
            movieFramePlayer = new MovieFramePlayer(this);
            movieFramePlayer.start();
        }
    }


    public void stop() {
        nFrame = moviePaintInstructor.getNFrames();
        if (movieFramePlayer != null) {
            movieFramePlayer.stop();
        }
    }




    public void faster() {
        speed *= 1.3;
    }

    public void slower() {
        speed /= 1.3;
    }
    public boolean canAdvance() {
        return (shownFrame < nFrame - 1);
    }


    public void advance() {
        advanceToFrame(shownFrame + 1);
    }


    public double getSpeed() {
        return speed;
    }


    public void setMovieStateDisplay(MovieStateDisplay msd) {
        movieStateDisplay = msd;
    }


    public void reluctantReframe() {
        pwCanvas.reluctantReframe();

    }



    public BufferedImage getBufferedImage(int ithick) {

        // see DataviewController for original intention
        // - good for images you want to scale down to thumbs after
        //  setPaintWidthFactor(ithick);

        BufferedImage ret = AWTUtil.getBufferedImage(this);

        //      setPaintWidthFactor(1);

        return ret;
    }





    public void record(File f, Logger l) {
        stop();
        reset();

        MDThreadRunner mdtr = new MDThreadRunner(this, f, l);
        Thread thr = new Thread(mdtr);
        thr.setPriority(Thread.MIN_PRIORITY);
        thr.start();
        l.init("writing animated gif " + f);
    }



    class MDThreadRunner implements Runnable {
        File file;
        Logger logger;
        MovieDisplay movdisplay;

        MDThreadRunner(MovieDisplay md, File f, Logger l) {
            movdisplay = md;
            file = f;
            logger = l;
        }

        public void run() {
            movdisplay.threadRecord(file, logger);
        }
    }



    protected void threadRecord(File f, Logger l) {
        int delay = (int)(20. / speed);

        AnimatedGifEncoder enc = new AnimatedGifEncoder();

        enc.start(f);
        enc.setDelay(delay);

        int ifr = 0;
        E.info("animated gif - frame " + ifr);
        enc.addFrame(getBufferedImage(1));

        while (canAdvance()) {
            advance();
            ifr += 1;
            E.info("frame " + ifr);
            l.optionalIncrementLog(ifr, "frame");
            enc.addFrame(getBufferedImage(1));
        }
        enc.finish();

        l.end();
    }


}
