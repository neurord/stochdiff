package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DBoxPanel;
import org.catacomb.interlish.interact.DComponent;


public class DruBoxPanel extends DruSubcontainerPanel {

    static final long serialVersionUID = 1001;

    public final static int VERTICAL = 1;
    public final static int HORIZONTAL = 2;

    boolean first = true;
    int spacing;
    int idir;


    DBoxPanel dBoxPanel;


    public DruBoxPanel() {
        this(VERTICAL);
    }


    public DruBoxPanel(int dir) {
        this(dir, 2);
    }


    public DruBoxPanel(int dir, int pspace) {
        super();

        if (dir == VERTICAL) {
            dBoxPanel = new DBoxPanel(DBoxPanel.VERTICAL);
        } else {
            dBoxPanel = new DBoxPanel(DBoxPanel.HORIZONTAL);
        }
        spacing = pspace;

        setSingle();
        getGUIPeer().addDComponent(dBoxPanel);
    }



    public void addGlue() {
        dBoxPanel.addGlue();
    }


    public void subAddPanel(DruPanel dp) {
        subAddDComponent(dp.getGUIPeer());
    }

    public void subAddDComponent(DComponent obj) {
        dBoxPanel.addDComponent(obj);

        if (first) {
            first = false;
        } else {
            if (spacing > 0) {
                if (idir == VERTICAL) {
                    dBoxPanel.addVerticalStrut(spacing);
                } else {
                    dBoxPanel.addHorizontalStrut(spacing);
                }
            }
        }
    }


    public void subRemoveAll() {
        dBoxPanel.removeAll();
    }


}
