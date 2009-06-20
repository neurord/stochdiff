
package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.report.E;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;


public class DMenuItem extends JMenuItem implements ActionListener {
    static final long serialVersionUID = 1001;

    String actionCommand;
    String baseLabel;

    int ilang;

    LabelActor lactor;


    public DMenuItem(String s) {
        this(s, s);
    }


    public DMenuItem(String s, String a) {
        super(s);
        setBaseLabel(s);
        setActionCommand(a);
        addActionListener(this);
        setBackground(LAF.getBackgroundColor());
    }


    public String toString() {
        return "DMenuItem " + baseLabel;
    }




    public void setBaseLabel(String s) {
        baseLabel = s;
        if (actionCommand == null) {
            actionCommand = s;
        }
        setText(s);
    }

    public void setActionCommand(String s) {
        actionCommand = s;
    }


    public void actionPerformed(ActionEvent aev) {
        labelAction(actionCommand, true);
    }


    public void setLabelActor(LabelActor la) {
        lactor = la;
    }

    public void labelAction(String s, boolean b) {
        if (lactor == null) {
            E.warning("non-overridden label action in menu " +
                      "and no label actor set " + baseLabel);
        } else {
            lactor.labelAction(s, b);
        }
    }


    public String getBaseLabel() {
        return baseLabel;
    }

    public void checkName() {

    }

}
