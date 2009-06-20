

package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruAbleEffect;
import org.catacomb.druid.gui.edit.Effect;


public class AbleEffect extends BaseEffect {

    public boolean when;

    public String state;

    public String target;



    public Effect realize(Context ctx, GUIPath gpath) {
        Effect eff = new DruAbleEffect(target, when, state);

        ctx.getMarketplace().addViewer("TargetStore", eff, "access");

        return eff;
    }



}
