package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;


public class Requisite implements Realizer {

    public String id;
    public String source;


    public Object realize(Context pctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;

        gpath = gpath.extend(id);

        Context ctx = new Context(pctx);
        Druid druid = new Druid(source, ctx);

        druid.whizzNoBang();


        //  druid.setID(id);
        pctx.addComponent(druid, gpath);

        return null; // druid.getRootComponent();
    }



}
