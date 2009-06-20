package org.catacomb.report;


public interface Reporter {


    public void report(String s);

    public void reportInfo(String s);

    public void reportWarning(String s);

    public void reportError(String s);

}
