

package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.MouseActor;


import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;


public class DCheckbox extends JCheckBox implements DComponent, ActionListener {
    static final long serialVersionUID = 1001;

    String label;
    String actionCommand;

    RolloverEffect rollover;

    LabelActor lact;

    public DCheckbox(String lab) {
        this(lab, null);

    }

    public DCheckbox(String lab, @SuppressWarnings("unused")
                     String action) {
        super(lab);
        label = lab;

        setFont(new Font("sansserif", Font.PLAIN, 12));

        this.label = lab;
        addActionListener(this);
        setFocusPainted(false);

    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void setBg(Color c) {
        setBackground(c);
    }


    public void setMouseActor(MouseActor ma) {
        addMouseListener(new DMouseRelay(ma));
    }


    public boolean isChecked() {
        return isSelected();
    }

    public void setActionCommand(String s) {
        actionCommand = s;
    }


    public void actionPerformed(ActionEvent aev) {
        deliverAction(label, isSelected());
    }


    public String toString() {
        return "DButton " + label;
    }



    public void setLabelActor(LabelActor bl) {
        lact = bl;
    }



    public void setBaseLabel(String s) {
        setText(s);
    }


    public String getBaseLabel() {
        return label;
    }


    public void deliverAction(String sin, boolean b) {
        String s = sin;
        if (actionCommand != null) {
            s = actionCommand;
        }
        if (lact != null) {
            lact.labelAction(s, b);
        }
    }

    public String getStringIdentifier() {
        return label;
    }

}








