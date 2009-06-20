
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruInfoEffect;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruButton;



public class InfoButton extends AbstractButton {


    public String text;

    public InfoButton() {
        super();
    }




    public DruPanel instantiatePanel() {
        return new DruButton("?");
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {


        DruButton drup = (DruButton)dp;


        if (image != null) {
            drup.setImage(image);
        }

        applyDefaultRollover(drup);
        applyPadding(drup);


        drup.addEffect(new DruInfoEffect(title, text, ctx.getInfoAggregator()));

        drup.setTip(tip);

    }



}
