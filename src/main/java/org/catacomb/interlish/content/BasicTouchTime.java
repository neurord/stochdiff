package org.catacomb.interlish.content;

import org.catacomb.interlish.structure.TouchTime;


public class BasicTouchTime implements TouchTime {

    int touchedAt;
    static int globalCounter;


    static {
        globalCounter = 1000;
    }


    public BasicTouchTime() {
        globalCounter += 1;
        touchedAt = globalCounter;
    }


    public void now() {
        globalCounter += 1;
        touchedAt = globalCounter;

    }


    public String toString() {
        return "(touched at " + touchedAt +")";
    }


    public int time() {
        return touchedAt;
    }

    public boolean isAfter(TouchTime ct) {
        return (touchedAt > ct.time());
    }

    public boolean isBefore(TouchTime ct) {
        return (touchedAt < ct.time());
    }


    public void never() {
        touchedAt = 0;
    }

}
