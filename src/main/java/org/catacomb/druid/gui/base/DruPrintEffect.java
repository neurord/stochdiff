package org.catacomb.druid.gui.base;

import org.catacomb.druid.gui.edit.Effect;



public class DruPrintEffect extends Effect {

    String text;

    public DruPrintEffect(String s) {
        text = s;
    }



    public void apply(boolean b) {
        System.out.println(text);
    }



}
