package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DFloat;
import org.catacomb.interlish.content.DoubleValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.awt.Color;

public class DruFloat extends DruGCPanel
    implements LabelActor, Ablable, DoubleValueEditor, ValueWatcher {
    static final long serialVersionUID = 1001;

    DFloat dFloat;

    public LabelActor labelActor;


    DoubleValue doubleValue;



    public DruFloat(double val, double min, double max, String scale) {
        super();

        dFloat = new DFloat(val, min, max, scale);

        addSingleDComponent(dFloat);
        dFloat.setLabelActor(this);
    }


    public void setProperties(double val, double min, double max, String scale) {
        dFloat.setProperties(val, min, max, scale);
    }




    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {
            valueChange(doubleValue.getDouble());

        } else {
            if (doubleValue == pv) {
                if (doubleValue == null) {
                    dFloat.setValue(0.0);
                    dFloat.setEnabled(false);
                } else {
                    dFloat.setValue(doubleValue.getDouble());
                }
            } else {
                E.error("value changed by called with mismatched value");
            }
        }
    }


    public void setDoubleValue(DoubleValue dv) {
        if (doubleValue != null) {
            doubleValue.removeValueWatcher(this);
        }
        doubleValue = dv;
        if (doubleValue == null) {
            dFloat.setEnabled(false);
        } else {
            dFloat.setValue(doubleValue.getDouble());
            doubleValue.addValueWatcher(this);
        }
    }



    public void setLabel(String s) {
        dFloat.setLabel(s);
    }

    public void setEditable(boolean b) {
        dFloat.setEditable(b);
        dFloat.setEnabled(b);
    }


    public void postApply() {
        dFloat.setMouseActor(this);
    }


    public void setBg(Color c) {
        dFloat.setBg(c);
        super.setBg(c);
    }


    public void able(boolean b) {
        dFloat.setEnabled(b);
    }




    public void labelAction(String s, boolean b) {
        double d = dFloat.getValue();
        if (doubleValue != null) {
            doubleValue.reportableSetDouble(d, this);
        } else {
            E.warning("no holder - dropping value change " + d);
        }

    }



}
