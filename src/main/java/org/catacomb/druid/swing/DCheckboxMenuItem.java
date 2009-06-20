

package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;


public final class DCheckboxMenuItem extends JCheckBoxMenuItem
    implements ActionListener {
    static final long serialVersionUID = 1001;

    String actionCommand;
    String baseLabel;

    int ilang;

    public LabelActor ml;
    DPopLabel mypl;

    public DCheckboxMenuItem(String s) {
        this(s, null);
    }

    public DCheckboxMenuItem(String s, DPopLabel pl) {
        super(s);
        mypl = pl;
        setBaseLabel(s);
        setActionCommand(s);
        addActionListener(this);
    }

    public DCheckboxMenuItem(String s, DPopLabel pl, boolean b) {
        this(s, pl);
        setSelected(b);
    }


    public String toString() {
        return "DMenuItem " + baseLabel;
    }

    public void setLabelActor(LabelActor ml) {
        this.ml = ml;
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
        if (ml != null) {
            ml.labelAction(actionCommand, isSelected());
        }
    }




    public String getBaseLabel() {
        return baseLabel;
    }

    public void checkName() {
    }

}
