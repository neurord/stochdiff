package org.catacomb.druid.gui.base;

import java.awt.Dimension;

import org.catacomb.druid.swing.DScrollPane;


public class DruScrollingInfoPanel extends DruInfoPanel {

    static final long serialVersionUID = 1001;

    DScrollPane dsp;


    public DruScrollingInfoPanel() {
        this("");
    }

    public DruScrollingInfoPanel(String text) {
        this(text, 0, 0);
    }

    public DruScrollingInfoPanel(String text, int w, int h) {
        super("", w, h);
        setBorderLayout(2, 2);
        getGUIPeer().setMinimumSize(new Dimension(50, 50));

        dsp = new DScrollPane();
        dsp.setVerticalScrollBarAlways();

        removeDComponent(htmlPane);
        dsp.setViewportView(htmlPane);
        addDComponent(dsp);
        newTextAction = APPEND;
        setTooltipTarget(htmlPane);

        if (text != null) {
            showInfo(text);
        }
    }


    public void textAdded() {
        dsp.scrollToBottom();
    }

}
