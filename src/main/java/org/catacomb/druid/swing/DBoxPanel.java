package org.catacomb.druid.swing;

import javax.swing.Box;
import javax.swing.BoxLayout;

import javax.swing.JPanel;

import javax.swing.JComponent;

import org.catacomb.interlish.interact.DComponent;


public class DBoxPanel extends JPanel implements DComponent {
    private static final long serialVersionUID = 1L;

    public final static int VERTICAL = 1;
    public final static int HORIZONTAL = 2;


    public DBoxPanel(int dir) {

        if (dir == VERTICAL) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        } else {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }
    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public void addGlue() {
        add(Box.createGlue());
    }


    public void addVerticalStrut(int spacing) {
        add(Box.createVerticalStrut(spacing));
    }

    public void addHorizontalStrut(int spacing) {
        add(Box.createHorizontalStrut(spacing));
    }


    public void addDComponent(DComponent obj) {
        add((JComponent)obj);

    }


}