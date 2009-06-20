
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruSlider;



public class Slider extends Panel {


    public String label;
    public String action;


    public Slider() {
    }



    public Slider(String slab) {
        label = slab;
        action = label;
    }


    public DruPanel instantiatePanel() {
        return new DruSlider(1, action);
    }



    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {

    }




}
