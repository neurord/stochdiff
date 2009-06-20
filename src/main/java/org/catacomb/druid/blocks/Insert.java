package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.Druid;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.report.E;


public class Insert implements Realizer {

    public String name;
    public String id;
    public String source;
    public String controllerClass;



    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;

        gpath = gpath.extend(id);

        Druid druid = new Druid(source, ctx);

        druid.buildGUI();

        if (controllerClass != null && controllerClass.length() > 1) {
            // druid.attachController(controllerClass);
            E.missing("Insert accesses druid? - probably shouldn't - " + "ignoring controller for now");

        } else {
            druid.selfActivate();
        }

        druid.setID(id);


        ctx.addComponent(druid, gpath);


        // MISSING use of name???

        return druid.getRootComponent();
    }



}
