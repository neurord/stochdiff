package org.catacomb.druid.blocks;

import org.catacomb.datalish.SColor;
import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.build.Realizer;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.Effect;
import org.catacomb.interlish.structure.IDd;
import org.catacomb.report.E;


import java.awt.Color;
import java.util.ArrayList;



public abstract class Panel implements Realizer, IDd {


    public String id;
    public String title;

    public String tip;
    public String info;


    public String border;

    public int borderWidth;

    public int paddingLeft;
    public int paddingRight;
    public int paddingTop;
    public int paddingBottom;

    public int prefWidth;
    public int prefHeight;


    public String borderTitle;

    public SColor backgroundColor;

    static int panelcount = 0;


    public Panel() {
        paddingLeft = 0;
        paddingRight = 0;
        paddingTop = 0;
        paddingBottom = 0;

        panelcount += 1;
    }


    public String getID() {
        return id;
    }


    public final Object realize(Context ctx, GUIPath gpathin) {
        GUIPath gpath = gpathin;
        gpath = gpath.extend(id);


        DruPanel drup = instantiatePanel();

        if (backgroundColor != null) {
            drup.setBackgroundColor(backgroundColor.getColor());

        } else {
            backgroundColor = new SColor(ctx.getBg());
            drup.setFallbackBackgroundColor(ctx.getBg());
        }



        populatePanel(drup, ctx, gpath);

        apply(drup, ctx);

        ctx.addComponent(drup, gpath);

        return drup;
    }


    public abstract DruPanel instantiatePanel();

    public abstract void populatePanel(DruPanel drup, Context ctx, GUIPath gpath);




    private void apply(DruPanel drup, Context ctx) {
        drup.setFallbackBackgroundColor(ctx.getBg());
        drup.setFallbackForegroundColor(ctx.getFg());

        if (id != null && id.length() > 0) {
            drup.setID(id);
        }

        if (prefWidth > 0 || prefHeight > 0) {
            if (prefHeight == 0) {
                prefHeight = prefWidth;
            }
            if (prefWidth == 0) {
                prefWidth = prefHeight;
            }

            drup.setPreferredSize(prefWidth, prefHeight);
        }

        applyBorder(drup, ctx);

        if (title != null) {
            drup.setTitle(title);
        }
        drup.setTip(tip);
        drup.setInfo(info);


        drup.setInfoReceiver(ctx.getInfoAggregator());

        drup.postApply();
    }


    private final void applyBorder(DruPanel drup, Context ctx) {

        int[] bds = { paddingLeft, paddingRight, paddingTop, paddingBottom };

        boolean nonzero = false;

        for (int i = 0; i < 4; i++) {
            if (bds[i] == 0) {
                bds[i] = borderWidth;
            }
            if (bds[i] != 0) {
                nonzero = true;
            }
        }

        if (nonzero) {
            drup.setEmptyBorder(bds[0], bds[1], bds[2], bds[3]);
        }



        if (border != null) {
            if (border.equals("etched")) {
                drup.addEtchedBorder(backgroundColor.getColor());

            } else if (border.equals("sunken")) {
                drup.addSunkenBorder(backgroundColor.getColor());

            } else if (border.equals("none")) {

            } else {
                E.error(" - border style not recognised " + border + " (only 'etched' is defined)");
            }
        }
        if (borderTitle != null) {
            Color c = backgroundColor.getColor();
            if (border != null) {
                // leave as bg;
            } else {
                c = c.darker();
            }

            drup.addTitledBorder(borderTitle, c);
        }

    }



    public ArrayList<Effect> realizeEffects(ArrayList<BaseEffect> arl, Context ctx, GUIPath gpath) {
        ArrayList<Effect> ret = null;
        if (arl != null) {
            ret = new ArrayList<Effect>();

            for (BaseEffect bef : arl) {
                ret.add(bef.realize(ctx, gpath));
            }
        }
        return ret;
    }

}
