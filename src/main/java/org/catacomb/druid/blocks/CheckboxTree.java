package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruCheckboxTreePanel;


public class CheckboxTree extends Panel {

    public String action;
    public String flavor;




    public DruPanel instantiatePanel() {
        return new DruCheckboxTreePanel();
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        DruCheckboxTreePanel drup = (DruCheckboxTreePanel)dp;

        if (action != null) {
            drup.setAction(action);
        }

        if (flavor != null) {
            ctx.getMarketplace().addConsumer("CheckboxTree", drup, flavor);
            ctx.getMarketplace().addVisible("TreeSelection", drup, flavor); // was "selection" not flavor;
        }

    }

}
