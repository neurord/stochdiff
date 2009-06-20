
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.edit.Effect;

public abstract class BaseEffect {

    public abstract Effect realize(Context ctx, GUIPath gpath);

}
