
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruSimpleSlider;



public class SimpleSlider extends Panel {


    public int min = 0;
    public int max = 100;
    public String label;
    public String action;


    public SimpleSlider() {
    }



    public SimpleSlider(String slab) {
        label = slab;
        action = label;
    }


    public DruPanel instantiatePanel() {
        return new DruSimpleSlider(min, max, label, action);
    }



    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {

    }




}
