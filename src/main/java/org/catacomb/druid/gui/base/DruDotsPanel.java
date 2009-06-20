
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DDotsPanel;

import java.awt.Color;


public class DruDotsPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    DDotsPanel ddp;

    public DruDotsPanel() {
        setSingle();
        ddp = new DDotsPanel();
        addDComponent(ddp);
        addBorder(2, 2, 14, 0);
    }


    public void setBg(Color c) {
        ddp.setBg(c);
    }


}
