package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DDropTextField;

import java.awt.Color;


public class DruDropBox extends DruGCPanel implements LabelActor {
    static final long serialVersionUID = 1001;

    DDropTextField textField;


    public DruDropBox() {
        setBorderLayout(0, 0);


        textField = new DDropTextField("", 30);
        textField.setLabelActor(this);
        addDComponent(textField, DBorderLayout.CENTER);

    }

    public void setBg(Color c) {
        textField.setBackground(c.brighter());
        super.setBg(c);
    }

    public void labelAction(String s, boolean b) {
        valueChange(s);
    }

    public DDropTextField getDropField() {
        return textField;
    }

    public void clear() {
        textField.setText("");
    }

    public void druDisable() {
    }

    public void setText(String s) {
        textField.setText(s);
    }

    public void druEnable() {
    }

    public String getText() {
        return textField.getText();
    }

    public Object getDropee() {
        return textField.getDropee();
    }

}
