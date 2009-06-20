package org.catacomb.druid.gui.base;


import org.catacomb.druid.gui.edit.Effect;
import org.catacomb.interlish.structure.Suggestible;
import org.catacomb.report.E;




public class DruSuggestEffect extends Effect  {


    public DruSuggestEffect(String tgt) {
        super(tgt);
    }

    public void apply(String s) {
        apply();
    }


    public void apply() {
        Object tgt = getTarget();

        if (tgt instanceof Suggestible) {
            ((Suggestible)tgt).suggest();

        } else {
            E.error("must have suggestible component, not " + tgt);
        }

    }

    public void apply(boolean b) {
        apply();
    }

}
