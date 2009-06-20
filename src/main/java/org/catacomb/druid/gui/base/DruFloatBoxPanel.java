
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DBorderLayout;

import java.awt.Color;


public class DruFloatBoxPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    public final static int VERTICAL = 1;
    public final static int HORIZONTAL = 2;

    DruBoxPanel druBoxPanel;


    public DruFloatBoxPanel() {
        this(VERTICAL);
    }

    public DruFloatBoxPanel(int dir) {
        super();

        setBorderLayout(2, 2);

        druBoxPanel = new DruBoxPanel(dir);

        addPanel(druBoxPanel, DBorderLayout.NORTH);
    }




    public void setBg(Color c) {
        druBoxPanel.setBg(c);
        super.setBg(c);
    }



    public void addPanel(DruPanel drup) {
        druBoxPanel.addPanel(drup);
    }


    public void addGlue() {
        druBoxPanel.addGlue();
    }



}
