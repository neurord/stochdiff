package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.event.TextActor;
import org.catacomb.interlish.interact.DComponent;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class DTextField extends DPanel implements DComponent,
    DocumentListener, KeyListener, FocusListener {

    static final long serialVersionUID = 1001;

    LabelActor labelActor;
    TextActor textActor;

    String tag;
    boolean ignoreEvents = false;

    JTextField jTextField;

    boolean returnEvents;

    boolean reportFocus = false;

    boolean changedWhileFocused;

    public DTextField(String sid, int w) {
        super();
        tag = sid;
        jTextField = new JTextField(w);

        jTextField.setBorder(BorderUtil.makeEmptyBorder(2));

        setSingle();
        add(jTextField);
//      jTextField.setEnabled(true);
//     jTextField.setEditable(true);
        jTextField.getDocument().addDocumentListener(this);

        returnEvents = false;
        jTextField.addFocusListener(this);
    }


    public void enableReturnEvents() {
        if (returnEvents) {
            // nothing to do;
        } else {
            returnEvents = true;
            jTextField.addKeyListener(this);
        }
    }


    public void enableFocusEvents() {
        reportFocus = true;
    }

    protected JTextField getJTextField() {
        return jTextField;
    }


    public String getTag() {
        return tag;
    }


    public void setText(String s) {

        ignoreEvents = true;
        jTextField.setText(s);

        ignoreEvents = false;
    }


    public void setEditable(boolean b) {
        jTextField.setEditable(b);
    }

    public void setEnabled(boolean b) {
        jTextField.setEnabled(b);
        if (b) {
            jTextField.setBackground(Color.white);
        } else {
            jTextField.setBackground(new Color(236, 236, 236));
        }
    }


    public void setLabelActor(LabelActor bl) {
        labelActor = bl;
    }


    public void setTextActor(TextActor tl) {
        textActor = tl;
    }


    public void changedUpdate(DocumentEvent d) {
        flagChange();
    }


    public void insertUpdate(DocumentEvent d) {
        flagChange();
    }


    public void removeUpdate(DocumentEvent d) {
        flagChange();
    }


    public void flagChange() {
        if (ignoreEvents) {

        } else {
            changedWhileFocused = true;

            if (textActor != null) {
                textActor.textChanged(getText());
            }
        }
    }



    public String getText() {
        return jTextField.getText();
    }

    /*
    Document doc = jTextField.getDocument();
    int nc = doc.getLength();
    String sret = null;
    try {
       sret = jTextField.getText(0, nc);
    } catch (Exception e) {
    }
    return sret;
    }
    */



    protected void reportAction() {
        if (labelActor != null) {
            labelActor.labelAction(getText(), true);
        }
    }


    public void keyTyped(KeyEvent e) {
    }


    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (labelActor != null) {
                labelActor.labelAction("return_pressed", true);
            }

            if (textActor != null) {
                textActor.textEntered(getText());
            }
        }
    }


    public void keyReleased(KeyEvent e) {
    }


    public void setLineBorder(int icol) {
        setBorder(BorderFactory.createLineBorder(new Color(icol)));
    }


    public void focusGained(FocusEvent e) {
        changedWhileFocused = false;
        if (reportFocus) {
            textActor.focusGained();
        }
    }


    public void focusLost(FocusEvent e) {
        if (changedWhileFocused) {
            if (ignoreEvents) {
                // do just that;
            } else if (textActor != null) {
                textActor.textEdited(getText());

            }
        }
        if (reportFocus) {
            textActor.focusLost();
        }

    }


}
