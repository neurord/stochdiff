package org.catacomb.druid.swing;

import org.catacomb.interlish.structure.MouseSource;
import org.catacomb.report.E;


import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DButton extends DBaseButton implements ActionListener, MouseSource {

    static final long serialVersionUID = 1001;

    int rolloverOffStyle = RolloverEffect.ETCHED_DOWN;
    int rolloverOnStyle = RolloverEffect.ETCHED_UP;
    RolloverEffect rollover;

    public Color bufColor;



    public DButton(String lab) {

        if (lab != null && lab.length() > 0) {
            setText(lab);
        }
        label = lab;

        setFont(new Font("sansserif", Font.PLAIN, 12));

        addActionListener(this);
        setFocusPainted(false);
    }


    public void setTooltip(String s) {
        String swas = getToolTipText();
        if (swas != null) {
            E.warning("replacing tool tip " + swas + " with " + s + " ... ?");
        }
        setToolTipText(s);
    }


    public void setRolloverPolicy(int inorm, int ihover) {
        rolloverOffStyle = inorm;
        rolloverOnStyle = ihover;
    }

    public void applyRollover() {
        if (rollover != null) {
            removeMouseListener(rollover);
        }
        rollover = new RolloverEffect(this, rolloverOffStyle, rolloverOnStyle);
        addMouseListener(rollover);
    }




    public void setBg(Color c) {
        setBackground(c);
        if (rollover != null) {
            rollover.setBg(c);
        }
    }


    public void setFg(Color c) {
        setForeground(c);
    }



    public void actionPerformed(ActionEvent aev) {
        deliverAction(true);
        deSuggest();
    }


    public void suggest() {
        Color scol = Color.lightGray;
        Color cbg = getBackground();
        if (scol.equals(cbg)) {
            // nothing to do;
        } else {
            bufColor = cbg;
            setBg(scol);
        }
    }

    public void deSuggest() {
        if (bufColor != null) {
            setBg(bufColor);
        }
    }

    public String toString() {
        return "DButton " + label;
    }



    public String getStringIdentifier() {
        return label;
    }



    public void setPadding(int p) {
        if (rollover == null) {
            applyRollover();
        }
        rollover.setPadding(p);
    }


    public void setPadding(int pl, int pr, int pt, int pb) {
        if (rollover == null) {
            applyRollover();
        }
        rollover.setPadding(pl, pr, pt, pb);
    }


}
