package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.edit.DruCheckboxMenuItem;
import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.druid.gui.edit.DruMenuItem;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.util.ArrayList;


public class Menu implements AddableTo, Realizer {

    public String label;
    public String id;

    public String action;

    public ArrayList<Object> items;


    public Menu() {
    }


    public Menu(String s) {
        label = s;
    }



    public void add(Object obj) {
        if (items == null) {
            items = new ArrayList<Object>();
        }
        items.add(obj);
    }


    public void addMenuItem(MenuItem mit) {
        if (items == null) {
            items = new ArrayList<Object>();
        }
        items.add(mit);
    }



    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;
        gpath = gpath.extend(id);



        DruMenu drum = new DruMenu(label);
        drum.setID(id);

        drum.setAction(action);

        addItems(drum, ctx, gpath);

        ctx.addComponent(drum, gpath);

        return drum;
    }


    protected void addItems(DruMenu drum, Context ctx, GUIPath gpath) {
        if (items != null) {
            for (Object onxt : items) {
                if (onxt instanceof MenuItem) {
                    MenuItem mcfg = (MenuItem)(onxt);
                    drum.addItem((DruMenuItem)(mcfg.realize(ctx, gpath)));

                } else if (onxt instanceof CheckboxMenuItem) {
                    CheckboxMenuItem mcfg = (CheckboxMenuItem)(onxt);
                    drum.addItem((DruCheckboxMenuItem)(mcfg.realize(ctx, gpath)));


                } else if (onxt instanceof Separator) {
                    drum.addSeparator();

                } else if (onxt instanceof SubMenu) {
                    SubMenu smenu = (SubMenu)onxt;
                    drum.addSubMenu((DruMenu)(smenu.realize(ctx, gpath)));

                } else if (onxt instanceof String) {
                    drum.addItem((String)onxt);
                } else {
                    E.error(" - bad item in menu " + onxt);
                }
            }
        }
    }


}
