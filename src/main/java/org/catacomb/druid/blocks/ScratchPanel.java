package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruScratchPanel;



public class ScratchPanel extends Panel {



    public DruPanel instantiatePanel() {
        return new DruScratchPanel();
    }

    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {


    }



}
