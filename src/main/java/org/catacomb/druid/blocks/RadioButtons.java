
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruRadioButtons;



public class RadioButtons extends Panel {

    public String action;
    public String layout;

    public String label;

    public String store;

    public String options;
    public String labels;

    public String from;
    public int autoSelect;



    public RadioButtons() {
        autoSelect = -1;
    }



    public DruPanel instantiatePanel() {

        if (layout == null) {
            layout = "vertical";
        }

        return  new DruRadioButtons(label, action, layout);
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruRadioButtons drup = (DruRadioButtons)dp;

        if (options != null) {
            String[] sa = options.split(",");

            String[] sopts = new String[sa.length];
            String[] slabs = new String[sa.length];

            for (int i = 0; i < sa.length; i++) {
                String s = sa[i].trim();
                sopts[i] = s;
                slabs[i] = s;
            }

            if (labels != null) {
                String[] sb = labels.split(",");
                for (int i = 0; i < sa.length && i < sopts.length; i++) {
                    slabs[i] = sb[i].trim();
                }
            }

            drup.setOptions(sopts, slabs);
        }


        if (autoSelect >= 0) {
            drup.setAutoSelect(autoSelect);
        }

        if (from != null && from.length() > 0) {
            ctx.getMarketplace().addConsumer("ChoiceOptions", drup, from);
        }

    }

}
