
package org.catacomb.druid.blocks;

import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruDialog;
import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;



public class Dialog implements AddableTo, Realizer {


    public String title;
    public Panel panel;

    public String id;

    public boolean modal;

    public SColor backgroundColor;


    public Dialog() {
        modal = false;
    }


    public void add(Object obj) {
        if (obj instanceof Panel) {
            panel = (Panel)obj;
        } else {
            E.error("cannot add " + obj);
        }
    }


    public Object realize(Context ctx, GUIPath gpath) {
        return realize(null, ctx, gpath);
    }

    public Object realize(DruFrame dfin, Context ctx, GUIPath gpathin) {
        DruFrame druFrame = dfin;
        GUIPath gpath = gpathin;
        gpath = gpath.extend(id);

        if (druFrame == null) {
            druFrame = new DruFrame("dummy frame");
        }

        if (backgroundColor != null) {
            ctx.setBg(backgroundColor.getColor());
        }




        DruDialog drud = new DruDialog(druFrame, (title != null ? title : ""));
        drud.setID(id);



        if (panel != null) {
            DruPanel drup = (DruPanel)(panel.realize(ctx, gpath));
            drud.setDruPanel(drup);
        }

        drud.setModal(modal);

        ctx.addComponent(drud, gpath);

        return drud;
    }



}
