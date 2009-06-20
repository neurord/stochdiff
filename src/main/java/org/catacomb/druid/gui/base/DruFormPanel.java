
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DFormPanel;
import org.catacomb.interlish.interact.DComponent;

import java.awt.Color;

public class DruFormPanel extends DruSubcontainerPanel {
    static final long serialVersionUID = 1001;


    DFormPanel dFormPanel;

    public DruFormPanel() {
        super();

        dFormPanel = new DFormPanel();

        setSingle();
        getGUIPeer().addDComponent(dFormPanel);
    }


    public void gridify(int rows, int cols) {
        dFormPanel.gridify(rows, cols);
    }


    public void setBg(Color c) {
        dFormPanel.setBg(c);
        super.setBg(c);
    }


    public void subAddPanel(DruPanel dp) {
        dFormPanel.addDItem(dp.getGUIPeer());
    }


    public void subAddDComponent(DComponent obj) {
        dFormPanel.addDItem(obj);
    }

    public void subRemoveAll() {
        dFormPanel.removeAll();
    }

}
