
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruMenuBar;
import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.util.ArrayList;



public class MenuBar implements AddableTo, Realizer {

    public String id;

    public ArrayList<Menu> menus;


    public MenuBar() {
    }


    public void add(Object obj) {
        if (obj instanceof Menu) {
            addMenu((Menu)obj);
        } else {
            E.error("cant add " + obj);
        }
    }

    public void addMenu(Menu menu) {
        if (menus == null) {
            menus = new ArrayList<Menu>();
        }
        menus.add(menu);
    }



    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;
        gpath = gpath.extend(id);

        DruMenuBar dmb = new DruMenuBar(id);
        if (menus != null) {
            for (Menu mcfg : menus) {
                DruMenu dm = (DruMenu)(mcfg.realize(ctx, gpath));
                dmb.addMenu(dm);
            }
        }


        ctx.addComponent(dmb, gpath);

        return dmb;
    }



}
