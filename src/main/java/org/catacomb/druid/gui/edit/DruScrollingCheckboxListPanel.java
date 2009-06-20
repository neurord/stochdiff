package org.catacomb.druid.gui.edit;

import org.catacomb.druid.gui.base.DruListClickActor;
import org.catacomb.druid.swing.DScrollPane;
import org.catacomb.report.E;


public class DruScrollingCheckboxListPanel extends DruCheckboxListPanel {

    DScrollPane dsp;


    public DruScrollingCheckboxListPanel() {
        this(10);
    }

    public DruScrollingCheckboxListPanel(int n) {
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

    @SuppressWarnings("unused")
    public void addClickAction(DruListClickActor actor) {
        E.missing();
    }

    @SuppressWarnings("unused")
    public void setOrder(int reverse_order) {
        E.missing();
    }



}
