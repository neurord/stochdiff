package org.catacomb.graph.gui;

import javax.swing.JPanel;

import org.catacomb.interlish.interact.DComponent;

import java.awt.Color;

public class BasePanel extends JPanel implements DComponent {
    static final long serialVersionUID = 1001;



    public BasePanel() {
        super();
    }


    public void setBg(Color c) {
        setBackground(c);
    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }

}
