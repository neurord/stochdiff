
package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DScrollPane;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.report.E;

import java.awt.Color;
import java.awt.Dimension;


public class DruScrollPanel extends DruSubcontainerPanel {
    static final long serialVersionUID = 1001;

    DScrollPane dsp;

    //   DruBorderPanel contentPanel;


    public DruScrollPanel() {
        super();
        dsp = new DScrollPane();

        setSingle();
        getGUIPeer().addDComponent(dsp);
    }


    public void subAddPanel(DruPanel drup) {
        dsp.setViewportView(drup.getGUIPeer());
    }


    public void subAddDComponent(DComponent dcpt) {
        dsp.setViewDComponent(dcpt);
    }




    public void setVerticalScrollbarAsNeeded() {
        dsp.setVerticalScrollbarAsNeeded();
    }

    public void setVerticalScrollbarAlways() {
        dsp.setVerticalScrollBarAlways();
    }


    public void setVerticalScrollbarNever() {
        dsp.setVerticalScrollbarNever();
    }


    public void setHorizontalScrollbarAsNeeded() {
        dsp.setHorizontalScrollbarAsNeeded();
    }

    public void setHorizontalScrollbarAlways() {
        dsp.setHorizontalScrollbarAlways();
    }

    public void setHorizontalScrollbarNever() {
        dsp.setHorizontalScrollbarNever();
    }




    public void setPreferredSize(int w, int h) {
        dsp.setPreferredSize(new Dimension(w, h));
        getGUIPeer().setPreferredSize(new Dimension(w, h));
    }





    public void setBg(Color c) {
        dsp.setBg(c);
        super.setBg(c);

    }


    public void subRemoveAll() {
        E.missing();
    }


}
