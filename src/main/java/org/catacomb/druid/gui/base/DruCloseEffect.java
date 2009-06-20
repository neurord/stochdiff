package org.catacomb.druid.gui.base;


import org.catacomb.druid.gui.edit.Effect;
import org.catacomb.interlish.structure.Closable;
import org.catacomb.report.E;




public class DruCloseEffect extends Effect  {




    public DruCloseEffect(String s) {
        super(s);
    }



    public void apply(boolean b) {

        Object obj = getTarget();
        if (obj instanceof Closable) {
            ((Closable)obj).close();


        } else {
            E.warning("non-closable target in close effect " + obj + " " + this);
        }

    }



}
