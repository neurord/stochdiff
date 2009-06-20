

package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.edit.DruWindowEditEffect;
import org.catacomb.druid.gui.edit.Effect;


public class WindowEditEffect extends BaseEffect {

    public String config;

    public String target;



    public Effect realize(Context ctx, GUIPath gpath) {
        Effect eff = new DruWindowEditEffect(target, config);

        ctx.getMarketplace().addViewer("TargetStore", eff, "access");

        return eff;
    }



}
