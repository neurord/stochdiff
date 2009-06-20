package org.catacomb.druid.gui.edit;

import java.awt.Color;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DRadioButtons;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



public class DruRadioButtons extends DruGCPanel implements LabelActor, Ablable, Choice,
    StringValueEditor, ValueWatcher {

    static final long serialVersionUID = 1001;

    DRadioButtons dRadioButtons;


    StringValue stringValue;

    @SuppressWarnings("unused")
    public DruRadioButtons(String lab, String act, String layout) {

        setActionMethod(act);

        String[] sa = new String[0];
        dRadioButtons = new DRadioButtons(sa, layout);

        addSingleDComponent(dRadioButtons);

        setStringValue(new StringValue());

        dRadioButtons.setLabelActor(this);
    }



    public void setStringValue(StringValue bv) {
        if (stringValue != null) {
            stringValue.removeValueWatcher(this);
        }
        stringValue = bv;
        if (stringValue == null) {
            dRadioButtons.setEnabled(false);
        } else {
            dRadioButtons.setSelected(stringValue.getString());
            stringValue.addValueWatcher(this);
        }
    }



    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {
            valueChange(stringValue.getString());

        } else {
            if (stringValue == pv) {
                if (stringValue == null) {
                    dRadioButtons.setEnabled(false);
                } else {
                    dRadioButtons.setSelected(stringValue.getString());
                }
            } else {
                E.error("value changed by called with mismatched value");
            }
        }
    }


    public void postApply() {
        dRadioButtons.setMouseActor(this);
    }


    public void able(boolean b) {
        dRadioButtons.setEnabled(b);
    }


    public void setValue(String s) {
        dRadioButtons.setSelected(s);
    }


    public void setOptions(String[] sa) {
        dRadioButtons.setOptions(sa, sa);
    }


    public void setOptions(String[] sopts, String[] slabs) {
        dRadioButtons.setOptions(sopts, slabs);

    }


    public String getSelected() {
        return dRadioButtons.getSelected();
    }



    public void labelAction(String s, boolean b) {
        //  E.info("rb setting string value to " + s);
        stringValue.reportableSetString(s, this);
//	   valueChange(getSelected());
    }


    public void setStringValue(String s) {
        setSelected(s);
    }


    public void setSelected(String s) {
        dRadioButtons.setSelected(s);
    }


    public void unselect() {
        setSelected(null);
    }


    public void setAutoSelect(int autoSelect) {
        dRadioButtons.setSelectedIndex(autoSelect);
    }

    public void setBg(Color c) {
        dRadioButtons.setBg(c);
    }
    public void setFg(Color c) {
        dRadioButtons.setFg(c);
    }

}
