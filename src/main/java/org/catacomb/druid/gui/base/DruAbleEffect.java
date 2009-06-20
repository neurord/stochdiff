package org.catacomb.druid.gui.base;


import org.catacomb.druid.gui.edit.Effect;
import org.catacomb.interlish.structure.Ablable;
import org.catacomb.report.E;




public class DruAbleEffect extends Effect  {


    boolean enabledState;

    String enabledValue;


    public DruAbleEffect(String tgt, boolean b, String s) {
        super(tgt);
        enabledState = b;
        enabledValue = s;
    }



    public void apply(String s) {
        Object tgt = getTarget();

        if (tgt instanceof Ablable) {
            if (s != null && s.equals(enabledValue)) {
                ((Ablable)tgt).able(true);
            } else {
                ((Ablable)tgt).able(false);
            }


        } else {
            E.error("must have ableable component, not " + tgt);
        }

    }

    public void apply(boolean b) {
        Object tgt = getTarget();

        if (tgt instanceof Ablable) {
            if (b == enabledState) {
                ((Ablable)tgt).able(true);
            } else {
                ((Ablable)tgt).able(false);
            }


        } else {
            E.error("must have ableable component, not " + tgt);
        }

    }

}
