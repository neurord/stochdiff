package org.catacomb.druid.swing;

import java.awt.Color;

import org.catacomb.interlish.structure.Colored;
import org.catacomb.interlish.structure.Named;



public class ToggleItem implements Colored {


    boolean b;

    Object ref;

    public ToggleItem(Object obj) {
        ref = obj;
    }


    public String toString() {
        String ret = null;
        if (ref instanceof Named) {
            ret = ((Named)ref).getName();

        } else {
            ret = ref.toString();
        }
        return ret;

    }


    public boolean isOn() {
        return b;
    }

    public Object getRef() {
        return ref;
    }

    public void toggle() {
        b = !b;
    }

    public void setOff() {
        b = false;
    }

    public void setOn() {
        b = true;
    }


    public Color getColor() {
        Color ret = null;
        if (ref instanceof Colored) {
            ret = ((Colored)ref).getColor();
        }
        return ret;
    }
}
