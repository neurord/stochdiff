package org.catacomb.druid.gui.edit;


import org.catacomb.interlish.structure.InfoLog;
import org.catacomb.interlish.structure.LogMessageGenerator;
import org.catacomb.report.E;





public class DruLoggedButton extends DruButton implements LogMessageGenerator {
    static final long serialVersionUID = 1001;

    InfoLog infoLog;


    public DruLoggedButton(String s) {
        super(s);
    }


    public void setLog(InfoLog ilog) {
        infoLog = ilog;
        E.info("set log in logButton");
    }


    public void labelAction(String s, boolean b) {

        int ival = (int)(0.5 + 4 * Math.random());

        if (infoLog != null) {
            infoLog.addInfoMessage(ival, "button press", "button " + getLabel() + " has been pressed");
        }

        super.labelAction(s, b);
    }



}
