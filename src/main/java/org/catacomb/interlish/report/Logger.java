package org.catacomb.interlish.report;


public interface Logger {

    void log(String s);

    void log(Message m);

    void optionalIncrementLog(int ifr, String string);

    void init(String string);

    void end();

}
