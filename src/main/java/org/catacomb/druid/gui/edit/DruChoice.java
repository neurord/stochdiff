package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.event.OptionsSource;
import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DChoice;
import org.catacomb.druid.swing.DLabel;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.awt.Color;
import java.util.ArrayList;


public class DruChoice extends DruGCPanel
    implements LabelActor, Ablable, Choice, StringValueEditor, ValueWatcher {

    static final long serialVersionUID = 1001;

    DLabel dLabel;
    DChoice dChoice;


    StringValue stringValue;

    ArrayList<Effect> effects;


    public DruChoice(String[] opts, String mnm) {
        this(null, opts, opts, mnm);
    }




    public DruChoice(String label, String mnm) {
        this(label, null, null, mnm);
    }




    public DruChoice(String label, String[] opts, String[] labs, String mnm) {
        setActionMethod(mnm);


        dChoice = new DChoice(opts, labs);


        if (label != null && label.length() > 0) {
            setBorderLayout(4, 4);
            dLabel = new DLabel(label);

            addDComponent(dLabel, DBorderLayout.WEST);
            addDComponent(dChoice, DBorderLayout.CENTER);

        } else {

            addSingleDComponent(dChoice);
        }

        setStringValue(new StringValue());
        dChoice.setLabelActor(this);
    }

    public void setEffects(ArrayList<Effect> arl) {
        effects = arl;
    }


    public void setStringValue(StringValue bv) {
        if (stringValue != null) {
            stringValue.removeValueWatcher(this);
        }
        stringValue = bv;
        if (stringValue == null) {
            dChoice.setEnabled(false);
            applyState(null);
        } else {
            String s = stringValue.getString();
            dChoice.setSelected(s);
            applyState(s);
            dChoice.setEnabled(stringValue.isAble());
            stringValue.addValueWatcher(this);
        }
    }

    private void applyState(String s) {
        if (effects != null) {
            for (Effect eff : effects) {
                eff.apply(s);
            }
        }
    }


    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {
            valueChange(stringValue.getString());

        } else {
            if (stringValue == pv) {
                if (stringValue == null) {
                    able(false);
                } else {
                    dChoice.setSelected(stringValue.getString());
                    able(stringValue.isAble());
                }
            } else {
                E.error("value changed by called with mismatched value");
            }
        }
    }




    public void unselect() {
        stringValue.reportableSetString(null, this);
    }

    public void setUpdatable(Updatable u) {
        dChoice.setUpdatable(u);
    }


    public void setAutoSelect(int ias) {
        dChoice.setAutoSelect(ias);
    }



    public void setOptionsSource(OptionsSource os) {
        dChoice.setOptionsSource(os);
    }


    public void setSelected(String s) {
        dChoice.setSelected(s);
    }


    public String getSelected() {
        return dChoice.getSelected();
    }


    public void setBg(Color c) {
        dChoice.setBg(c);
        super.setBg(c);
    }



    public void able(boolean b) {
        dChoice.setEnabled(b);
        if (dLabel != null) {
            dLabel.setEnabled(b);
        }
    }



    public void setTooltip(String s) {
        dChoice.setTooltip(s);
    }


    public void setOptions(String[] sa) {
        setOptions(sa, sa);
    }


    public void setOptions(String[] sa, String[] sl) {
        dChoice.setOptions(sa, sl);
    }


    public void updateOptions() {
        dChoice.checkOptions();
    }


    public void labelAction(String s, boolean b) {
        applyState(s);
        if (stringValue != null) {
            stringValue.reportableSetString(dChoice.getSelected(), this);
        }
    }




    public void clearSelection() {
        dChoice.clearSelection();

    }



}
