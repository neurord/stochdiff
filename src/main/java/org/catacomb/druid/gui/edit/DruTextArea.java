
package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.TextActor;
import org.catacomb.druid.swing.DTextArea;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.awt.Color;


public class DruTextArea extends DruGCPanel
    implements TextActor, Ablable, TextArea, ValueWatcher {
    static final long serialVersionUID = 1001;

    DTextArea dTextArea;


    StringValue stringValue;


    public DruTextArea(String mn, int width, int height) {
        super();

        setActionMethod(mn);

        dTextArea = new DTextArea(width, height, DTextArea.SCROLLABLE);

        setSingle();
        addDComponent(dTextArea);

        dTextArea.setLineBorder(0xc0c0c0);

        dTextArea.setTextActor(this);
    }




    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {

        } else {
            if (stringValue == pv) {
                if (stringValue == null) {
                    dTextArea.setText("");
                    dTextArea.setEnabled(false);
                } else {
                    dTextArea.setText(stringValue.getString());

                    if (src.equals("HIGHLIGHT")) {  // ADHOC
                        dTextArea.highlightLine(stringValue.getHighlight());
                    } else {
                        dTextArea.clearHighlight();
                    }

                }
            } else {
                E.error("value changed by called with mismatched value");
            }
        }
    }





    public void setStringValue(StringValue sv) {
        if (stringValue != null) {
            stringValue.removeValueWatcher(this);
        }
        stringValue = sv;
        if (stringValue == null) {
            dTextArea.setEnabled(false);
        } else {
            dTextArea.setText(stringValue.getString());
            stringValue.addValueWatcher(this);
        }
    }





    public void textChanged(String s) {
        stringValue.reportableSetString(dTextArea.getText(), this);

        // valueChange(dTextArea.getText()); // ever called?
    }


    public void textEntered(String s) {

    }

    public void textEdited(String s) {

    }



    public void setBg(Color c) {
        dTextArea.setBg(c);
        super.setBg(c);
    }


    public void able(boolean b) {
        dTextArea.setEnabled(b);
    }

    public void setEditable(boolean b) {
        dTextArea.setEditable(b);
        dTextArea.setEnabled(b);
    }

    public void setAntialiased() {
        dTextArea.setAntialiased();
    }

    public void setPadding(int padding) {
        dTextArea.setPadding(padding);

    }

    public void setFontSize(int fs) {
        dTextArea.setFontSize(fs);
    }




    public void focusGained() {
        // TODO Auto-generated method stub

    }




    public void focusLost() {
        // TODO Auto-generated method stub

    }

}
