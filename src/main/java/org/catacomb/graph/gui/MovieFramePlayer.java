package org.catacomb.graph.gui;


public class MovieFramePlayer implements Runnable {

    MovieDisplay movieDisplay;

    boolean shouldStop;


    public MovieFramePlayer(MovieDisplay md) {
        movieDisplay = md;
        shouldStop = false;
    }


    public void stop() {
        shouldStop = true;
    }


    public void start() {
        Thread thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }




    public void run() {
        while (movieDisplay.canAdvance()) {

            double spd = movieDisplay.getSpeed();

            int msecs = (int)(20. / spd);

            try {
                Thread.sleep(msecs);
            } catch (Exception ex) {

            }

            if (shouldStop) {
                break;
            }

            if (movieDisplay.canAdvance()) {
                movieDisplay.advance();
            }
        }

    }


}
