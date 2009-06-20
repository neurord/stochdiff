
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruButton;



public class Button extends AbstractButton {


    public Button() {
        super();
    }



    public Button(String slab) {
        super(slab);
    }




    public DruPanel instantiatePanel() {
        return new DruButton(label, action);
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {


        DruButton drup = (DruButton)dp;


        if (image != null) {
            drup.setImage(image);
        }

        applyDefaultRollover(drup);
        applyPadding(drup);

        drup.able(able);

        drup.setEffects(realizeEffects(effects, ctx, gpath));

        if (title == null || title.length() == 0) {
            if (label == null && image != null) {
                drup.setTitle("[image button]");
            } else {
                drup.setTitle(label + " (button)");
            }
        }
    }



}
