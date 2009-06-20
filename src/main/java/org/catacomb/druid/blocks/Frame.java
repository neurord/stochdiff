package org.catacomb.druid.blocks;

import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.druid.gui.base.DruMenuBar;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.UIColors;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;




public class Frame implements AddableTo, Realizer {


    public String title;
    public MenuBar menuBar;
    public Panel panel;
    public String info;

    public String id;

    public SColor backgroundColor;

    public int prefWidth;
    public int prefHeight;

    public Frame() {
    }


    public Frame(String ttl, MenuBar mb, Panel p) {
        title = ttl;
        menuBar = mb;
        panel = p;
    }


    public void add(Object obj) {
        if (obj instanceof Panel) {
            if (panel != null) {
                E.error("can only have one panel in a frame");
            } else {
                panel = (Panel)obj;
            }
        } else if (obj instanceof MenuBar) {
            menuBar = (MenuBar)obj;
        } else if (obj instanceof Menu) {
            if (menuBar == null) {
                menuBar = new MenuBar();
            }
            menuBar.add(obj);
        } else {
            E.error("cant add to frame: " + obj);
        }

    }


    public Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;
        gpath = gpath.extend(id);

        if (backgroundColor != null) {
            ctx.setBg(backgroundColor.getColor());
        }

        UIColors.initialize(ctx.getBg());


        DruFrame axf = new DruFrame((title != null ? title : ""));
        axf.setID(id);

        axf.setBackgroundColor(ctx.getBg());

        if (menuBar != null) {
            DruMenuBar amb = (DruMenuBar)(menuBar.realize(ctx, gpath));

            axf.setDruMenuBar(amb);
        }

        if (panel != null) {
            DruPanel axp = (DruPanel)(panel.realize(ctx, gpath));
            axf.setDruPanel(axp);
        }

        ctx.addComponent(axf, gpath);

        if (info != null && info.length() > 1) {
            ctx.getInfoAggregator().receiveInfo(title, info);
        }

        if (prefWidth > 0 || prefHeight > 0) {
            if (prefHeight == 0) {
                prefHeight = prefWidth;
            }
            if (prefWidth == 0) {
                prefWidth = prefHeight;
            }

            axf.setPreferredSize(prefWidth, prefHeight);
        }

        return axf;
    }



}
