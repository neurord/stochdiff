
package org.catacomb.druid.blocks;


import java.util.ArrayList;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruLabelledTextField;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


public class LabelledTextField extends Panel implements AddableTo {


    public String label;
    public String store;
    public String action;
    public String focusAction;

    public int width;

    public String report;


    public String history;

    public boolean able = true;

    public ArrayList<BaseEffect> effects;


    public LabelledTextField() {
    }


    public LabelledTextField(String slab) {
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
            E.error("cant add non effect " + obj);
        }
    }
    public DruPanel instantiatePanel() {
        return new DruLabelledTextField(label, action, width, (history != null));
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruLabelledTextField dtf = (DruLabelledTextField)dp;

        if (!able) {
            dtf.able(false);
        }


        if (report != null) {
            E.deprecate();
        }

        if (action != null) {
            dtf.setReturnAction(action);
        }

        if (focusAction != null) {
            dtf.setFocusAction(focusAction);
        }

        if (history != null) {
//    	  dtf.setHistoryKey(history); // TODO
        }


        if (effects != null) {
            dtf.setEffects(realizeEffects(effects, ctx, gpath));
        }

    }

}
