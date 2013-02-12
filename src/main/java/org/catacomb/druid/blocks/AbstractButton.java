
package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.edit.DruButton;
import org.catacomb.druid.swing.RolloverEffect;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.util.ArrayList;



public abstract class AbstractButton extends Panel implements AddableTo {


    public String image;

    public String label;
    public String action;

    public int padding;

    public int xPadding;
    public int yPadding;

    public String hover;

    public boolean able = true;

    public ArrayList<BaseEffect> effects;



    public AbstractButton() {
    }



    public AbstractButton(String slab) {
        label = slab;
        action = label;
    }


    private int getRo(String s, int idef) {
        int ret = idef;
        if (s == null) {
            ret = idef;

        } else if (s.equals("none")) {
            ret = RolloverEffect.NONE;

        } else if (s.equals("up")) {
            ret = RolloverEffect.ETCHED_UP;

        } else if (s.equals("down")) {
            ret = RolloverEffect.ETCHED_DOWN;

        } else {
            E.warning("unrecognized hover " + s);
        }
        return ret;
    }

    public void applyMenuRollover(DruButton drup) {
        drup.setRolloverPolicy(getRo(border, RolloverEffect.NONE),
                               getRo(hover, RolloverEffect.ETCHED_UP));
    }


    public void applyDefaultRollover(DruButton drup) {
        drup.setRolloverPolicy(getRo(border, RolloverEffect.ETCHED_DOWN),
                               getRo(hover, RolloverEffect.ETCHED_UP));
    }


    public void applyPadding(DruButton drup) {

        if (xPadding > 0 || yPadding > 0) {
            drup.setPadding(xPadding, xPadding, yPadding, yPadding);

        } else if (padding > 0) {
            drup.setPadding(padding);

        } else {
            drup.setPadding(6, 6, 2, 2);
        }
    }





    public void add(Object obj) {
        if (effects == null) {
            effects = new ArrayList<BaseEffect>();
        }
        if (obj instanceof BaseEffect) {
            effects.add((BaseEffect)obj);
        } else {
            E.error("cannot add non effect " + obj);
        }
    }

}
