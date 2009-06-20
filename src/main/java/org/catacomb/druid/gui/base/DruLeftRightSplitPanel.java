
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DSplitPane;

import java.awt.Color;


public class DruLeftRightSplitPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    DSplitPane dSplitPane;



    public DruLeftRightSplitPanel() {
        super();

        dSplitPane = new DSplitPane(DSplitPane.HORIZONTAL_SPLIT);
        setSingle();
        addDComponent(dSplitPane);
    }



    public void setBg(Color c) {
        dSplitPane.setBackground(c);
        super.setBg(c);

    }



    public void addPanel(DruPanel dp) {
        setColors(dp);
        dSplitPane.add(dp.getGUIPeer());
    }



}

