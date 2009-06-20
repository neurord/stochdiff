package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.PanelWrapper;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;



public class SinglePanelContainer implements AddableTo {

    public Realizer realizer;



    public DruPanel realizePanel(Context ctx, GUIPath gpath) {
        DruPanel ret = null;
        if (realizer != null) {
            Object obj = realizer.realize(ctx, gpath);
            if (obj instanceof DruPanel) {
                ret = (DruPanel)obj;
            } else if (obj instanceof PanelWrapper) {
                ret = ((PanelWrapper)obj).getPanel();
            } else {
                E.error("cant get panel from " + obj);
            }
        }

        return ret;

    }


    public void add(Object obj) {
        if (obj instanceof Realizer) {
            if (realizer == null) {
                realizer = (Realizer)obj;
            } else {
                E.error("too many panels (>1) " + getClass().getName());
            }

        } else {
            E.error("cant add  non-panel to " + this);
            E.info("tried to add " + obj + " (" + obj.getClass().getName() + ")");
        }

    }

}
