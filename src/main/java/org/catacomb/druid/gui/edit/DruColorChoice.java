package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DButton;
import org.catacomb.druid.swing.DColorChooser;
import org.catacomb.druid.swing.DLabel;
import org.catacomb.interlish.content.ColorValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;
import org.catacomb.util.ColorUtil;


import java.awt.Color;


public class DruColorChoice extends DruGCPanel
    implements LabelActor, Ablable, ColorValueEditor, ValueWatcher {
    static final long serialVersionUID = 1001;

    DLabel dLabel;
    DButton dButton;


    ColorValue colorValue;

    Color currentColor;
    String label;

    public LabelActor labelActor;


    public DruColorChoice(String lab, String mnm) {
        super();
        label = lab;
        methodName = mnm;
        setFlowLeft(2, 2);
        dLabel = new DLabel(label);
        dButton = new DButton("   ");
        dButton.setActionCommand("select");
        addDComponent(dButton);
        addDComponent(dLabel);

        dButton.setLabelActor(this);
        dButton.applyRollover();
        setColorValue(new ColorValue("#b0b000"));
        currentColor = new Color(colorValue.getIntColor());

        setTooltipTarget(dButton);
    }



    public void valueChangedBy(Value pv, Object src) {
        if (src == this) {
            setColor(new Color(colorValue.getIntColor()));
            valueChange(currentColor);

        } else {
            if (colorValue == pv) {
                if (colorValue == null) {
                    setColor(Color.red);

                } else {
                    setColor(new Color(colorValue.getIntColor()));
                }
            } else {
                E.error("value changed by called with mismatched value");
            }
        }
    }


    public void setColorValue(ColorValue cv) {
        if (colorValue != null) {
            colorValue.removeValueWatcher(this);
        }
        colorValue = cv;
        if (colorValue == null) {
            dButton.setEnabled(false);
        } else {
            dButton.setEnabled(true);
            setColor(new Color(colorValue.getIntColor()));

            colorValue.addValueWatcher(this);
        }
    }




    public void labelAction(String s, boolean b) {
        if (s.equals("select")) {

            Color newColor = DColorChooser.showDialog(null, label, currentColor);

            if (newColor != null) {
                colorValue.reportableSetColor(newColor.getRGB(), this);
            }

        } else {
            E.warning("unhandled action " + s);
        }
    }


    public void setBg(Color c) {
        dLabel.setBg(c);
        super.setBg(c);
    }


    public void postApply() {
        dButton.setMouseActor(this);
    }


    public void able(boolean b) {
        dButton.setEnabled(b);
        dLabel.setEnabled(b);
    }


    public void setColor(Color c) {
        if (c == null) {
            currentColor = Color.gray;
        } else {
            currentColor = c;
        }
        dButton.setBg(currentColor);
    }



    public Color getColor() {
        return currentColor;
    }


    public String getStringValue() {
        return ColorUtil.serializeColor(currentColor);
    }


}
