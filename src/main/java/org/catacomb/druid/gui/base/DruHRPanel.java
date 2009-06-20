
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DHRPanel;


public class DruHRPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    public DruHRPanel() {
        setSingle();
        addDComponent(new DHRPanel(0xb0b0b0));
        addBorder(2, 2, 8, 8);
    }




}
