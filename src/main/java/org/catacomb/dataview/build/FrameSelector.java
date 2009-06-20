package org.catacomb.dataview.build;

import org.catacomb.dataview.FrameController;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.gui.base.DruPanel;


public class FrameSelector {

    public String source;
    public String display;




    public DruPanel makePanel(Context parentContext) {
        Druid druid = new Druid("DataviewPlayer");

        druid.buildGUI();

        DruPanel drup = (DruPanel)(druid.getRootComponent());

        FrameController fcon = new FrameController(source, display);
        parentContext.addToCache(fcon);

        druid.attachSingleController(fcon);

        return drup;

    }



}
