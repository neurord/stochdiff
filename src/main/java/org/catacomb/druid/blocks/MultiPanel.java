package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.*;
import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.util.ArrayList;


public abstract class MultiPanel extends Panel implements AddableTo {

    public ArrayList<Realizer> realizers;

    public int xspace;
    public int yspace;


    public MultiPanel() {
        xspace = 0;
        yspace = 0;
    }


    public void add(Object obj) {
        if (realizers == null) {
            realizers = new ArrayList<Realizer>();
        }
        if (obj instanceof Realizer) {
            realizers.add((Realizer)obj);
        } else {
            E.error("cant add non-realizer " + obj + " to " + this);
        }
    }


    public void checkPanelCount(int np) {
        if (getPanelCount() != np) {
            E.warning("wrong number of panels - got " + getPanelCount() + " but need " + np);
        }
    }

    public int getPanelCount() {
        int npan = 0;
        if (realizers != null) {
            npan = realizers.size();
        }
        return npan;
    }





    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {



        if (realizers != null) {
            for (Realizer rlz : realizers) {

                Object panobj = rlz.realize(ctx, gpath);

                if (panobj instanceof DruPanel) {
                    dp.addPanel((DruPanel)panobj);

                } else if (panobj instanceof PanelWrapper) {
                    DruPanel dpw = ((PanelWrapper)panobj).getPanel();
                    dp.addPanel(dpw);

                } else if (panobj instanceof DruMenu) {
                    dp.addMenu((DruMenu)panobj);

                } else if (panobj instanceof DruSplitterBar) {
                    dp.addDComponent(((DruSplitterBar)panobj).getGUIPeer());

                    //     } else if (panobj instanceof JComponent) {
                    //        dp.add((JComponent)panobj);

                } else {
                    E.error(" (Druid build) - non-panel object in MultiPanel " + panobj + " "
                            + panobj.getClass().getName());
                }
            }
        }
    }


}
