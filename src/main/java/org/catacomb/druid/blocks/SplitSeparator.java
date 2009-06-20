package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruSplitterBar;


public class SplitSeparator implements Realizer {



    public final Object realize(Context ctx, GUIPath gpath) {
        return new DruSplitterBar();
    }

}
