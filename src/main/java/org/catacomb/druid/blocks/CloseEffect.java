

package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruCloseEffect;
import org.catacomb.druid.gui.edit.Effect;


public class CloseEffect extends BaseEffect {


    public String target;



    public Effect realize(Context ctx, GUIPath gpath) {
        Effect eff = new DruCloseEffect(target);

        ctx.getMarketplace().addViewer("TargetStore", eff, "access");

        return eff;
    }



}
