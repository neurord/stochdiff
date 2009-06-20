package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DInt;
import org.catacomb.interlish.content.IntegerValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.awt.Color;

public class DruInt extends DruGCPanel
    implements LabelActor, Ablable, IntegerValueEditor, ValueWatcher {
    static final long serialVersionUID = 1001;

    DInt dInt;

    public LabelActor labelActor;

    IntegerValue integerValue;


    public DruInt(int val, int min, int max, String scale) {
        super();

        dInt = new DInt(val, min, max, scale);

        addSingleDComponent(dInt);
        dInt.setLabelActor(this);
    }



    public void setProperties(int val, int min, int max,
                              String scale, boolean strictLims) {
        dInt.setProperties(val, min, max, scale, strictLims);
    }



    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {
            valueChange(integerValue.getInteger());

        } else {
            if (integerValue == pv) {
                if (integerValue == null) {
                    dInt.setValue(0);
                    dInt.setEnabled(false);
                } else {
                    dInt.setValue(integerValue.getInteger());
                }
            } else {
                E.error("value changed by called with mismatched value");
            }
        }
    }


    public void setIntegerValue(IntegerValue dv) {
        if (integerValue != null) {
            integerValue.removeValueWatcher(this);
        }
        integerValue = dv;
        if (integerValue == null) {
            dInt.setEnabled(false);
        } else {
            dInt.setValue(integerValue.getInteger());
            integerValue.addValueWatcher(this);
        }
    }




    public void setLabel(String s) {
        dInt.setLabel(s);
    }

    public void setEditable(boolean b) {
        dInt.setEditable(b);
        dInt.setEnabled(b);
    }


    public void setBg(Color c) {
        dInt.setBg(c);
        super.setBg(c);
    }


    public void able(boolean b) {
        dInt.setEnabled(b);
    }



    public void labelAction(String s, boolean b) {
        int i = dInt.getValue();

        if (integerValue != null) {
            integerValue.reportableSetInteger(i, this);
        }

    }


}
