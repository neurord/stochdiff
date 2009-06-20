
package org.catacomb.druid.blocks;

import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruAutonomousDialog;
import org.catacomb.druid.gui.base.DruDialog;
import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;




public class AutonomousDialog implements AddableTo, Realizer {

    public String name;
    public String title;
    public String id;
    public String controllerClass;
    public SColor backgroundColor;

    public Panel panel;

    public boolean modal;

    public void add(Object obj) {
        if (obj instanceof Panel) {
            panel = (Panel)obj;
        } else {
            E.error("cant add " + obj);
        }
    }


    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;

        gpath = gpath.extend(id);

        if (backgroundColor != null) {
            ctx.setBg(backgroundColor.getColor());
        }

        DruPanel druPanel = (DruPanel)(panel.realize(ctx, gpath));

        DruAutonomousDialog druad = new DruAutonomousDialog();
        druad.setControllerPath(controllerClass);
        druad.setName(name);

        DruDialog dd = new DruDialog(new DruFrame("dummy"), title);
        dd.setDruPanel(druPanel);

        dd.setModal(modal);

        druad.setDialog(dd);

        ctx.addComponent(druad, gpath);

        return druad;
    }



}






