package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruScrollableTextPanel;



public class ScrollableTextPanel extends Panel {

    public String content;

    public String show;

    public boolean dragSource;



    public DruPanel instantiatePanel() {
        return  new DruScrollableTextPanel();
    }



    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {
    }




}
