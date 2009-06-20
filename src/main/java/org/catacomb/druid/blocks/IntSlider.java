
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruInt;


public class IntSlider extends Panel {


    public String label;
    public String store;

    public String action;
    public int min;
    public int max;
    public String style;


    public DruPanel instantiatePanel() {
        return  new DruInt((min + max)/2, min, max, style);
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruInt drui = (DruInt)dp;
        drui.setLabel(label);
        drui.setMethodName(action);

    }


}
