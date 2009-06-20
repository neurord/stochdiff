package org.catacomb.druid.gui.edit;


import java.awt.Color;
import java.util.ArrayList;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.event.TextActor;
import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DLabel;
import org.catacomb.druid.swing.DPanel;
import org.catacomb.druid.swing.DTextField;
import org.catacomb.druid.swing.DValueHistory;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.Ablable;
import org.catacomb.interlish.structure.TextField;
import org.catacomb.interlish.structure.UseWatcher;
import org.catacomb.interlish.structure.Value;
import org.catacomb.interlish.structure.ValueWatcher;
import org.catacomb.report.E;




public class DruLabelledTextField extends DruGCPanel
    implements TextActor,  Ablable, TextField, ValueWatcher, UseWatcher, LabelActor {

    static final long serialVersionUID = 1001;

    DLabel dLabel;
    DTextField dTextField;
    DPanel dpanel;
    StringValue stringValue;

    DValueHistory dValueHistory;

    String focusAction;

    ArrayList<Effect> effects;

    public DruLabelledTextField() {
        this("", null, 20, false);
    }


    public DruLabelledTextField(String s) {
        this("", s, s.length(), false);
    }


    public DruLabelledTextField(String lbl, String mn, int width, boolean withHist) {
        super();



        dTextField = new DTextField("", width);
        setActionMethod(mn);

        dLabel = new DLabel(lbl);
        dpanel = new DPanel();
        dpanel.setBorderLayout(4, 0);
        dpanel.add(dLabel, DBorderLayout.WEST);
        dpanel.add(dTextField, DBorderLayout.CENTER);

        if (withHist) {
            dValueHistory = new DValueHistory();
            dValueHistory.setLabelActor(this);
            dpanel.add(dValueHistory, DBorderLayout.EAST);
        }

        addSingleDComponent(dpanel);
        dTextField.setTextActor(this);

        setLineBorder(0xc0c0c0);
    }


    public void setBg(Color c) {
        dpanel.setBg(c);
        dLabel.setBg(c);
        dTextField.setBg(c);
        if (dValueHistory != null) {
            dValueHistory.setBg(c);
        }
        super.setBg(c);
    }



    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {

        } else {
            if (stringValue == pv) {
                if (stringValue == null) {
                    dTextField.setText("");
                    able(false);
                } else {
                    dTextField.setText(stringValue.getString());

                    able(stringValue.isAble());
                }
            } else {
                E.error("value changed by called with mismatched value");
            }

        }
    }


    public void usedBy(Value pv, Object src) {
        if (src != this) {
            if (dValueHistory != null) {
                dValueHistory.checkContains(((StringValue)pv).silentGetAsString());
            }
        }
    }


    // the value history wants to set value;
    public void labelAction(String s, boolean b) {
        stringValue.reportableSetString(s, this);
        dTextField.setText(s);
    }



    public void setEditable(boolean b) {
        dTextField.setEditable(b);
        dTextField.setEnabled(b);
    }


    public void able(boolean b) {
        dTextField.setEnabled(b);
    }





    public void setStringValue(StringValue sv) {
        if (stringValue != null) {
            stringValue.removeValueWatcher(this);
            stringValue.removeUseWatcher(this);

        }
        stringValue = sv;
        if (stringValue == null) {
            able(false);

        } else {
            dTextField.setText(stringValue.getString());
            stringValue.addValueWatcher(this);
            stringValue.addUseWatcher(this);
            able(stringValue.isAble());
        }
    }



    public void setLineBorder(int icol) {
        dTextField.setLineBorder(icol);
    }



    public void textChanged(String s) {
        stringValue.reportableSetString(dTextField.getText(), this);
        syncEffects();
    }

    public void textEntered(String s) {
        if (hasAction()) {
            action();
        }
        syncEffects();
    }

    public void textEdited(String s) {
        stringValue.editCompleted();
        syncEffects();
    }

    private void syncEffects() {
        if (effects != null) {
            for (Effect eff : effects) {
                eff.apply(true);
            }
        }
    }


    public void setReturnAction(String action) {
        setAction(action);
        dTextField.enableReturnEvents();
    }

    public void setFocusAction(String faction) {
        focusAction = faction;
        dTextField.enableFocusEvents();
    }

    public void focusGained() {
        if (focusAction != null) {
            performAction(focusAction, true);
        }
    }


    public void focusLost() {
        if (focusAction != null) {
            performAction(focusAction, false);
        }
    }


    public void setEffects(ArrayList<Effect> arl) {
        effects = arl;
    }



}
