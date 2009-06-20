

package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruSuggestEffect;
import org.catacomb.druid.gui.edit.Effect;


public class SuggestEffect extends BaseEffect {


    public String target;



    public Effect realize(Context ctx, GUIPath gpath) {
        Effect eff = new DruSuggestEffect(target);

        ctx.getMarketplace().addViewer("TargetStore", eff, "access");

        return eff;
    }



}
