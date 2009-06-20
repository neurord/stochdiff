package org.catacomb.interlish.structure;


public interface InfoMessage {

    int UNKNOWN = 0;
    int INFO = 1;
    int WARNING = 2;
    int ERROR = 3;
    int FATAL = 4;

    String[] textLevels = { "unknown", "INFO", "WARNING", "ERROR", "FATAL" };

    String getPlainText();

    int getLevel();

    String getContext();  // eg "net access", "deprecated", "memory"

    boolean sameAs(InfoMessage im);

    String getHTML();

}
