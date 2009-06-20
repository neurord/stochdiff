

package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruCardSelectionEffect;
import org.catacomb.druid.gui.edit.Effect;


public class CardSelectionEffect extends BaseEffect {

    public String target;
    public String show;



    public Effect realize(Context ctx, GUIPath gpath) {
        Effect eff = new DruCardSelectionEffect(target, show);

        ctx.getMarketplace().addViewer("TargetStore", eff, "access");

        return eff;
    }



}
