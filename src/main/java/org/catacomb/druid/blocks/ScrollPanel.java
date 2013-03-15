package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruScrollPanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;




public class ScrollPanel extends Panel implements AddableTo {

    public Panel panel;

    public String bars;

    public String horizontal;
    public String vertical;



    public void add(Object obj) {
        if (panel == null) {
            panel = (Panel)obj;
        } else {
            E.warning("overriding panel in ScrollPanel " + this);
        }
    }



    public DruPanel instantiatePanel() {
        return new DruScrollPanel();
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruScrollPanel dsp = (DruScrollPanel)dp;

        if (bars != null) {
            E.error("bars no longer accepted");
        }

        if (vertical == null) {
            dsp.setVerticalScrollbarAsNeeded();

        } else {
            if (vertical.equals("never")) {
                dsp.setVerticalScrollbarNever();

            } else if (vertical.equals("as_needed")) {
                dsp.setVerticalScrollbarAsNeeded();

            } else if (vertical.equals("always")) {
                dsp.setVerticalScrollbarAlways();

            } else {
                E.error("don't recognize " + vertical);
            }
        }

        if (horizontal == null) {
            dsp.setHorizontalScrollbarNever();

        } else  {
            if (horizontal.equals("never")) {
                dsp.setHorizontalScrollbarNever();

            } else if (horizontal.equals("as_needed")) {
                dsp.setHorizontalScrollbarAsNeeded();

            } else if (horizontal.equals("always")) {
                dsp.setHorizontalScrollbarAlways();

            } else {
                E.error("don't recognize " + vertical);
            }
        }


        if (panel != null) {
            DruPanel subp = (DruPanel)(panel.realize(ctx, gpath));
            dsp.addPanel(subp);
        }

    }

}
