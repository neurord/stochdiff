package org.catacomb.interlish.structure;


public interface ProgressReport {

    void setStarted();

    void setFraction(double f);

    void setText(String txt);

    void setFocus(double min, double max);


    double getFraction();

    String getText();

    void update();

    void setFinished();


    void setIndeterminate(boolean b);
}
