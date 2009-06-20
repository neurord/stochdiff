package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DCheckbox;
import org.catacomb.interlish.content.BooleanValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.awt.Color;
import java.util.ArrayList;


public class DruCheckbox extends DruGCPanel
    implements LabelActor, Toggle, Ablable, BooleanValueEditor, ValueWatcher {

    static final long serialVersionUID = 1001;

    ArrayList<Effect> effects;
    DCheckbox dCheckbox;

    BooleanValue booleanValue;


    public DruCheckbox() {
        this(null, null);
    }


    public DruCheckbox(String lab, String mn) {
        super();
        dCheckbox = new DCheckbox(lab);
        setActionMethod(mn);

        addSingleDComponent(dCheckbox);
        dCheckbox.setLabelActor(this);

        setBooleanValue(new BooleanValue());
    }


    public void setBooleanValue(BooleanValue bv) {
        if (booleanValue != null) {
            booleanValue.removeValueWatcher(this);
        }
        booleanValue = bv;
        if (booleanValue == null) {
            dCheckbox.setEnabled(false);
        } else {
            dCheckbox.setSelected(booleanValue.getBoolean());
            booleanValue.addValueWatcher(this);
        }
        //  syncEffects();
    }



    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {
            valueChange(booleanValue.getBoolean());

        } else {
            if (booleanValue == pv) {
                if (booleanValue == null) {
                    dCheckbox.setEnabled(false);
                } else {
                    dCheckbox.setSelected(booleanValue.getBoolean());
                }
            } else {
                E.error("value changed by called with mismatched value");
            }
            syncEffects();
        }
    }



    public void postApply() {
        dCheckbox.setMouseActor(this);
    }


    public void setBg(Color c) {
        dCheckbox.setBg(c);
        super.setBg(c);
    }


    public void able(boolean b) {
        dCheckbox.setEnabled(b);
    }



    public void setInitialValue(boolean b) {
        if (booleanValue != null) {
            booleanValue.reportableSetBoolean(b, null);

        } else {
            E.warning("cant set state - no value holder");
        }
    }



    public void applyState() {
        boolean b = dCheckbox.isSelected();
        booleanValue.reportableSetBoolean(b, this);

        syncEffects();
    }

    private void syncEffects() {
        if (effects != null) {
            boolean b = booleanValue.getBoolean();

            for (Effect eff : effects) {
                eff.apply(b);
            }
        }

    }




    public void labelAction(String s, boolean b) {
        applyState();
    }


    public void setEffects(ArrayList<Effect> arl) {
        effects = arl;
    }


    public boolean isSelected() {
        return booleanValue.getBoolean();
    }


    public void setState(boolean b) {
        booleanValue.reportableSetBoolean(b, null);
        // TODO Auto-generated method stub

    }

}
