package org.catacomb.druid.gui.edit;

import org.catacomb.druid.swing.DScrollPane;


public class DruScrollingListPanel extends DruListPanel {

    DScrollPane dsp;


    public DruScrollingListPanel() {
        this(10);
    }

    public DruScrollingListPanel(int n) {
        super(n);


        dsp = new DScrollPane();
        dsp.setVerticalScrollbarAsNeeded();

        removeDComponent(dList);
        dsp.setViewportView(dList);
        addDComponent(dsp);
    }

    public void setToggleAction() {
        dList.setToggleAction();
    }

}
