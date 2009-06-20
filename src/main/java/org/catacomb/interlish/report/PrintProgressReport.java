package org.catacomb.interlish.report;


import org.catacomb.interlish.structure.ProgressReport;
import org.catacomb.report.E;




public class PrintProgressReport implements ProgressReport {

    double fmin = 0.;
    double fmax = 1.;



    public void setStarted() {
        E.info("ppp finished");
    }


    public void setFocus(double min, double max) {
        fmin = min;
        fmax = max;
    }

    public void setFraction(double f) {
        E.info("ppp percentage " + (fmin + f * (fmax - fmin)) * 100.);
    }


    public void setIndeterminate(boolean b) {

    }

    public void setText(String txt) {

    }


    public double getFraction() {
        return 0.;
    }

    public String getText() {
        return "";
    }


    public void update() {

    }

    public void setFinished() {
        E.info("ppp finished");
    }


}
