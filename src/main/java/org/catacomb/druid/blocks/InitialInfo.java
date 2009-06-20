package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;


public class InitialInfo implements Realizer {


    public String info;




    public final Object realize(Context ctx, GUIPath gpath) {
        if (info != null && info.length() > 1) {
            ctx.getInfoAggregator().receiveInfo(info);
        }

        return null;
    }

}
