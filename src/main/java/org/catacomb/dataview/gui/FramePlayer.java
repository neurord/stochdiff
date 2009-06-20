package org.catacomb.dataview.gui;

import org.catacomb.report.E;


public class FramePlayer implements Runnable {

    FramePlayerController frameController;

    boolean shouldStop;


    public FramePlayer(FramePlayerController fc) {
        frameController = fc;
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
        while (frameController.canAdvance()) {

            double spd = frameController.getSpeed();

            int msecs = (int)(100. / spd);

            try {
                Thread.sleep(msecs);
            } catch (Exception ex) {

            }

            if (shouldStop) {
                break;
            }

            if (frameController.canAdvance()) {
                frameController.advance();
            }
        }
        E.info("run finished");
    }



}
