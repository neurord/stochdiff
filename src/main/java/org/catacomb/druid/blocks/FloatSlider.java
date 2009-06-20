package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruFloat;


public class FloatSlider extends Panel {


    public String label;

    public String store;

    public String action;
    public double min;
    public double max;
    public String style;




    public DruPanel instantiatePanel() {

        DruFloat druf = new DruFloat((min + max) / 2., min, max, style);
        return druf;
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruFloat druf = (DruFloat)dp;

        druf.setLabel(label);
        druf.setMethodName(action);
        druf.setTitle(label);

    }


}
