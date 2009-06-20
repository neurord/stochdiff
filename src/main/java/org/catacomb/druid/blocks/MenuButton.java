package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruMenuButton;


public class MenuButton extends AbstractButton {


    public String show;

    public MenuButton() {
    }


    public MenuButton(String s) {
        label = s;
    }


    public DruPanel instantiatePanel() {
        return new DruMenuButton(label);
    }




    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruMenuButton drum = (DruMenuButton)dp;
        if (action != null) {
            drum.setAction(action);
        }
        drum.setID(id);

        if (image != null) {
            drum.setImage(image);
        }

        applyDefaultRollover(drum);
        applyPadding(drum);

        if (show != null) {
            drum.setPopupToShow(show);
            ctx.getMarketplace().addViewer("TargetStore", drum, "access");
        }

    }





}
