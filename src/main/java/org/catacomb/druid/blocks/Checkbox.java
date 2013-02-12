package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruCheckbox;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.util.ArrayList;


public class Checkbox extends Panel implements AddableTo {


    public String label;
    public String store;
    public String action;
    public boolean initial;

    public ArrayList<BaseEffect> effects;


    public Checkbox() {
        initial = false;
    }


    public Checkbox(String slab) {
        label = slab;
        action = label;
    }



    public void add(Object obj) {
        if (effects == null) {
            effects = new ArrayList<BaseEffect>();
        }
        if (obj instanceof BaseEffect) {
            effects.add((BaseEffect)obj);
        } else {
            E.error("cannot add non effect " + obj);
        }
    }


    public DruPanel instantiatePanel() {
        if (title == null) {
            title = label;
        }
        return new DruCheckbox(label, action);
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruCheckbox drup = (DruCheckbox)dp;
        drup.setInitialValue(initial);

        if (effects != null) {
            drup.setEffects(realizeEffects(effects, ctx, gpath));
        }

        if (title == null) {
            title = label;
        }


    }

}
