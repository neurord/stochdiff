
package org.catacomb.druid.gui.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import org.catacomb.druid.event.TextActor;
import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DButton;
import org.catacomb.druid.swing.DPanel;
import org.catacomb.druid.swing.DTextArea;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.Ablable;
import org.catacomb.interlish.structure.TextArea;
import org.catacomb.interlish.structure.Value;
import org.catacomb.interlish.structure.ValueWatcher;
import org.catacomb.report.E;


public class DruExpandingTextArea extends DruGCPanel
    implements TextActor, Ablable, TextArea, ValueWatcher {
    static final long serialVersionUID = 1001;


    DTextArea dTextArea;

    DPanel rtPanel;
    DPanel rbPanel;

    DButton upButton;
    DButton downButton;


    StringValue stringValue;

    boolean collapsed;

    boolean autoResize = false;


    public DruExpandingTextArea(String mn, int width, int height) {
        super();

        setBorderLayout(0, 0);

        setActionMethod(mn);

        dTextArea = new DTextArea(width, height, DTextArea.SCROLLABLE);

        addDComponent(dTextArea, DBorderLayout.CENTER);


        rtPanel = new DPanel();
        rtPanel.setLayout(new BorderLayout());
        rbPanel = new DPanel();
        rbPanel.setLayout(new GridLayout(2, 1, 2, 2));
        rtPanel.add(rbPanel, DBorderLayout.NORTH);


        upButton = new DButton("");
        upButton.setActionCommand("remove");
        upButton.setIconSource("up-hat.gif");
        upButton.setPadding(2, 2, 2, 2);

        downButton = new DButton("");
        downButton.setActionCommand("add");
        downButton.setIconSource("down-hat.gif");
        downButton.setPadding(2, 2, 2, 2);
        rbPanel.add(upButton);
        rbPanel.add(downButton);

        addDComponent(rtPanel, DBorderLayout.WEST);


        ExpandingTextAreaController etac =  new ExpandingTextAreaController(dTextArea);

        //   DruActionRelay relay = new DruActionRelay(etac);

        upButton.setLabelActor(etac);
        downButton.setLabelActor(etac);


        //      dTextArea.setLabelActor(this);
        dTextArea.setTextActor(this);

        setLineBorder(0xc0c0c0);
        collapsed = false;
    }


    public void collapse() {
        if (!collapsed) {
            removeDComponent(dTextArea);
            removeDComponent(rtPanel);

            collapsed = true;
        }

    }

    public void uncollapse() {
        if (collapsed) {
            addDComponent(dTextArea, DBorderLayout.CENTER);
            addDComponent(rtPanel, DBorderLayout.WEST);
            collapsed = false;
        }
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
                    if (autoResize) {
                        dTextArea.resizeUpToText();
                    }
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
            if (autoResize) {
                dTextArea.resizeUpToText();
            }
        }
    }


    public void textChanged(String s) {
        stringValue.reportableSetString(dTextArea.getText(), this);
        // valueChange(dTextArea.getText()); // ever called?

        // could cause too much line counting?
        if (autoResize) {

            //  E.info("auto resizing text are ");

            dTextArea.resizeUpToText();
        }
    }

    public void textEntered(String s) {
        E.info("text entered");
    }

    public void textEdited(String s) {
        // E.info("text edited");
    }



    public void setBg(Color c) {
        rtPanel.setBg(c);
        rbPanel.setBg(c);
        upButton.setBg(c);
        downButton.setBg(c);
        //      dTextArea.setBg(Color.white);
        super.setBg(c);
    }

    public void setTextBg(int icol) {
        Color c = new Color(icol);
        dTextArea.setBg(c);
    }

    public void setLineBorder(int icol) {
        dTextArea.setLineBorder(icol);
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


    public void setAutoResize(boolean b) {
        autoResize = b;

    }


    public void focusGained() {
        // TODO Auto-generated method stub

    }


    public void focusLost() {
        // TODO Auto-generated method stub

    }



}
