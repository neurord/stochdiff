
package org.catacomb.druid.gui.edit;

import org.catacomb.druid.swing.DProgressReport;
import org.catacomb.interlish.structure.ProgressReport;


import java.awt.Color;


public class DruProgressReport extends DruGCPanel implements ProgressReport {
    static final long serialVersionUID = 1001;


    DProgressReport dProgressReport;


    String text;
    double fraction;

    double fmin;
    double fmax;

    boolean indeterminate;

    public DruProgressReport() {
        super();

        fmin = 0.;
        fmax = 1.;

        dProgressReport = new DProgressReport();

        addSingleDComponent(dProgressReport);

    }

    public void setFocus(double min, double max) {
        fmin = min;
        fmax = max;
    }


    public void setBg(Color c) {
        dProgressReport.setBg(c);
        super.setBg(c);
        setEtchedUpBorder(c);

    }


    public void setIndeterminate(boolean b) {
        indeterminate = b;
    }


    public void setStarted() {
        fraction = 0.;
        update();
    }


    public void setFraction(double f) {
        indeterminate = false;
        fraction = fmin + f * (fmax - fmin);
    }

    public void setText(String txt) {
        text = txt;
        update();
    }


    public double getFraction() {
        return fraction;
    }

    public String getText() {
        return text;
    }

    public void update() {
        if (indeterminate) {
            dProgressReport.setIndeterminate(true);
        } else {
            dProgressReport.setIndeterminate(false);
            dProgressReport.setValue((int)(fraction * DProgressReport.imax));
        }

        if (text != null) {
            dProgressReport.setString(text);
        } else {
            dProgressReport.setString("");
        }
    }


    public void setFinished() {
        fraction = 1.;
        update();
    }


}








