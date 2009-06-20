package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruToggleButton;


public class ToggleButton extends AbstractButton {

    public String group;
    public String store;
    public String value;

    public boolean initial;

    public String offImage;
    public String onImage;

    public String groupAction;

    public ToggleButton() {
        super();
    }


    public ToggleButton(String slab) {
        super(slab);
    }





    public DruPanel instantiatePanel() {
        return new DruToggleButton(label, action);
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {


        DruToggleButton drup = (DruToggleButton)dp;

        if (image != null) {
            drup.setOffImage(image);

        } else if (offImage != null) {
            drup.setOffImage(offImage);
        }

        if (onImage != null) {
            drup.setOnImage(onImage);
        }

        applyPadding(drup);





        if (group != null) {
            drup.setToggle(group, value);

            drup.setContingencyGroup(ctx.getContingencyGroup(group));
            if (initial) {
                drup.setInitialValue(true);
            }
        }


        if (groupAction != null) {
            drup.setGroupAction(groupAction);
        }

        drup.setEffects(realizeEffects(effects, ctx, gpath));

        if (title == null || title.length() == 0) {
            drup.setTitle(label + " Button");
        }

        drup.setInitialValue(initial);
    }
}


