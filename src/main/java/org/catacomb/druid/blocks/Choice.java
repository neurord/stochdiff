package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruChoice;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;

import java.util.ArrayList;


public class Choice extends Panel implements AddableTo {


    public String label;

    public String store;


    public String action;
    public String options;
    public String labels;

    public String from;
    public int autoSelect;

    public boolean required;
    public String autoset;

    private ArrayList<Option> optionsAL;
    public ArrayList<BaseEffect> effects;


    public Choice() {
        autoSelect = -1;
    }


    public Choice(String slab) {
        label = slab;
        action = label;
        autoSelect = -1;
    }


    public void add(Object obj) {
        if (obj instanceof Option) {
            if (optionsAL == null) {
                optionsAL = new ArrayList<Option>();
            }
            optionsAL.add((Option)obj);

        } else if (obj instanceof BaseEffect) {
            if (effects == null) {
                effects = new ArrayList<BaseEffect>();
            }
            effects.add((BaseEffect)obj);

        } else {
            E.error("cannot add " + obj);
        }
    }



    public DruPanel instantiatePanel() {
        return new DruChoice(label, action);
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruChoice drup = (DruChoice)dp;
        String[] sopts = null;
        String[] slabs = null;

        if (optionsAL != null) {
            int n = optionsAL.size();
            sopts = new String[n];
            slabs = new String[n];
            for (int i = 0; i < n; i++) {
                Option opt = optionsAL.get(i);
                sopts[i] = (opt.label != null ? opt.label : opt.value);
                slabs[i] = (opt.label != null ? opt.value : opt.label);
            }

        } else if (options != null) {
            String[] sa = options.split(",");

            sopts = new String[sa.length];
            slabs = new String[sa.length];

            for (int i = 0; i < sa.length; i++) {
                String s = sa[i].trim();
                sopts[i] = s;
                slabs[i] = s;
            }

            if (labels != null) {
                String[] sb = labels.split(",");
                for (int i = 0; i < sa.length && i < sopts.length; i++) {
                    slabs[i] = sb[i].trim();
                }
            }
        }

        if (sopts != null) {
            drup.setOptions(sopts, slabs);
            if (autoSelect >= 0) {
                drup.setAutoSelect(autoSelect);
            } else if (autoset != null) {
                for (int i = 0; i < sopts.length; i++) {
                    if (sopts[i].equals(autoset)) {
                        drup.setAutoSelect(i);
                    }
                }
            }
        }


        if (from != null && from.length() > 0) {
            ctx.getMarketplace().addConsumer("ChoiceOptions", drup, from);
        }

        if (effects != null) {
            drup.setEffects(realizeEffects(effects, ctx, gpath));
        }

    }

}
