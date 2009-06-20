package org.catacomb.druid.blocks;


import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruAutonomousPanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.report.E;

import java.util.ArrayList;



public class AutonomousPanel extends SinglePanelContainer implements Realizer {

    public String title;
    public String name;
    public String id;
    public String controllerClass;

    public ArrayList<Dialog> dialogs;

    public SColor backgroundColor;



    public void add(Object obj) {
        if (obj instanceof Dialog) {
            if (dialogs == null) {
                dialogs = new ArrayList<Dialog>();
            }
            dialogs.add((Dialog)obj);
        } else {
            super.add(obj);
        }
    }


    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;

        gpath = gpath.extend(id);
        DruAutonomousPanel druap = new DruAutonomousPanel();

        if (backgroundColor != null) {
            ctx.setBg(backgroundColor.getColor());
        }



        druap.setName(name);
        druap.setControllerPath(controllerClass);

        DruPanel druPanel = realizePanel(ctx, gpath);

        if (druPanel != null) {
            druap.setMainPanel(druPanel);
        } else {
            E.warning("no panel set in AutonomousPanel");
        }


        if (dialogs != null) {
            for (Dialog dlg : dialogs) {
                dlg.realize(null, ctx, gpath); // TODO - give it the real frame
            }
        }

        ctx.addComponent(druap, gpath);

        return druap;
    }



}
