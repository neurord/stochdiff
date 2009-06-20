
package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.split.SplitterLayout;
import org.catacomb.report.E;

public class DruMultiSplitPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    public final static int VERTICAL = 1;
    public final static int HORIZONTAL = 2;

    public DruMultiSplitPanel(int dir) {


        // REFAC  - do like box with subpanel


        if (dir == VERTICAL) {
            getGUIPeer().setLayout(new SplitterLayout(SplitterLayout.VERTICAL));
        } else if (dir == HORIZONTAL) {
            getGUIPeer().setLayout(new SplitterLayout(SplitterLayout.HORIZONTAL));
        } else {
            E.error("unrecognized dir " + dir);
        }
    }




    public void addPanel(DruPanel drup) {
        setColors(drup);
        getGUIPeer().add(drup.getGUIPeer(), drup.getTitle());
    }





}
