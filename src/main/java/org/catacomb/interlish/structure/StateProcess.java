package org.catacomb.interlish.structure;



public interface StateProcess {



    int RAW = 0;
    int READY = 1;
    int RUNNING = 2;
    int PAUSED = 3;
    int FINISHED = 4;
    int ERROR = 5;



    int getProcessState();

    boolean isFinished();

}
