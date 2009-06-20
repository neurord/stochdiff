package org.catacomb.druid.gui.edit;


import org.catacomb.druid.event.TextActor;
import org.catacomb.druid.swing.DTextField;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.Ablable;
import org.catacomb.interlish.structure.TextField;
import org.catacomb.interlish.structure.Value;
import org.catacomb.interlish.structure.ValueWatcher;
import org.catacomb.report.E;




public class DruTextField extends DruGCPanel
    implements TextActor,  Ablable, TextField, ValueWatcher {

    static final long serialVersionUID = 1001;

    DTextField dTextField;

    StringValue stringValue;


    public DruTextField() {
        this(null, 20);
    }


    public DruTextField(String s) {
        this(s, s.length());
    }


    public DruTextField(String mn, int width) {
        super();

        dTextField = new DTextField("", width);
        setActionMethod(mn);

        addSingleDComponent(dTextField);
        dTextField.setTextActor(this);

        setLineBorder(0xc0c0c0);
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
        }
        stringValue = sv;
        if (stringValue == null) {
            able(false);

        } else {
            dTextField.setText(stringValue.getString());
            stringValue.addValueWatcher(this);
            able(stringValue.isAble());
        }
    }



    public void setLineBorder(int icol) {
        dTextField.setLineBorder(icol);
    }



    public void textChanged(String s) {
        stringValue.reportableSetString(dTextField.getText(), this);
    }

    public void textEntered(String s) {
        if (hasAction()) {
            action();
        }
    }

    public void textEdited(String s) {
        stringValue.editCompleted();
    }




    public void setReturnAction(String action) {
        setAction(action);
        dTextField.enableReturnEvents();
    }


    public void focusGained() {
        // TODO Auto-generated method stub

    }


    public void focusLost() {
        // TODO Auto-generated method stub

    }



}
