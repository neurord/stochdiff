package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPrintEffect;
import org.catacomb.druid.gui.edit.Effect;


public class PrintEffect extends BaseEffect {


    public String text;



    public Effect realize(Context ctx, GUIPath gpath) {
        return new DruPrintEffect(text);
    }




}
