package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DLabel;


public class DruLabelPanel extends DruPanel {

    static final long serialVersionUID = 1001;

    String text;

    DLabel dLabel;

    boolean htmlStyle = false;

    public DruLabelPanel(String lab, String align) {
        super();

        text = lab;

        if (align != null && align.equals("right")) {
            dLabel = new DLabel(text, DLabel.TRAILING);
            addSingleDComponent(dLabel);

        } else {
            dLabel = new DLabel(text);
            addSingleDComponent(dLabel);
        }
    }


    public void setText(String s) {
        text = s;
        if (htmlStyle && text != null && text.indexOf("<html>") < 0) {
            dLabel.setText("<html>" + s + "</html>");
        } else {
            dLabel.setText(s);
        }
    }

    public void setFontBold() {
        dLabel.setFontBold();
    }


    public void setEnabled(boolean b) {
        dLabel.setEnabled(b);
    }



    public void postApply() {

        if (info != null) {
            dLabel.setMouseActor(this);
        } else {
//        E.warning("not bothering with label action as info is null " + text);
        }
    }


    public void setStyleHTML() {
        htmlStyle = true;
        setText(text);
    }


}
