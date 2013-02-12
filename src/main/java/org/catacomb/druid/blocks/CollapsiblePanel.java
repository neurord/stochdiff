package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruCollapsiblePanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.PanelWrapper;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


public class CollapsiblePanel extends Panel implements AddableTo {

    public Realizer realizer;


    public CollapsiblePanel() {

    }


    public void add(Object obj) {
        if (obj instanceof Realizer) {
            realizer = (Realizer)obj;
        } else {
            E.error("cannot add non-realizer " + obj + " to " + this);
        }
    }


    public DruPanel instantiatePanel() {
        DruCollapsiblePanel dcp = new DruCollapsiblePanel();
        dcp.setSingle();
        return dcp;
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        DruCollapsiblePanel dcp = (DruCollapsiblePanel)dp;
        if (realizer != null) {
            Object panobj = realizer.realize(ctx, gpath);

            if (panobj instanceof DruPanel) {
                dcp.addContentPanel((DruPanel)panobj);

            } else if (panobj instanceof PanelWrapper) {
                DruPanel dpw = ((PanelWrapper)panobj).getPanel();
                dcp.addContentPanel(dpw);

            } else {
                E.error(" (Druid build) - non-panel object in MultiPanel " + panobj + " "
                        + panobj.getClass().getName());
            }
        }
    }


}
