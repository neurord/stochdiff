package org.catacomb.druid.swing;


import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.event.TextActor;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.report.E;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;


public class DInt extends JPanel implements DComponent, LabelActor, TextActor {

    static final long serialVersionUID = 1001;

    public final static int LOG = 1;
    public final static int LIN = 2;

    public int scale;

    int min0;
    int max0;


    DTextField dTextField;


    LabelActor labelActor;

    int value;

    boolean rangeEditable;


    public DInt(int val, int mn, int mx, String scl) {
        setScale(scl);
        rangeEditable = true;
        setRange(mn, mx);

        String sval = "" + val;
        setLayout(new BorderLayout(4, 0));

        dTextField = new DTextField(sval, 8);
        add("West", dTextField);
        dTextField.setLineBorder(0xc0c0c0);
        dTextField.setTextActor(this);
        dTextField.enableReturnEvents();

        // dFloatSlider = new DFloatSlider(this, val, mn, mx, scl);
        // dFloatSlider.setLabelActor(this);
        // add("Center", dFloatSlider);

        setRange(mn, mx);
        setValue(val);
    }


    public void setProperties(int val, int min, int max,
                              String scale, boolean strictLims) {
        setScale(scale);
        setRange(min, max);
        setValue(val);
        if (strictLims) {
            rangeEditable = false;
        }
    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }


    private void setScale(String scl) {
        if (scl == null || scl.startsWith("log")) {
            scale = LOG;
        } else if (scl.startsWith("lin")) {
            scale = LIN;
        } else {
            E.error(" - unrecognized scale in DFloat " + scl);
            scale = LOG;
        }
    }


    public void setRange(int min, int max) {
        if (rangeEditable) {
            min0 = min;
            max0 = max;
        } else {
            E.warning("set range called, but range not editable");
        }
    }

    public void setLabel(String s) {
        // dFloatSlider.setLabel(s);
    }


    public void setBg(Color c) {
        // dFloatSlider.setBg(c);
        setBackground(c);
    }


    public int getValue() {
        return value;
    }


    public void setValue(int i) {
        setValue(i, null);
    }

    public void setValue(int i, Object src) {
        value = i;
        // dFloatSlider.setValue(i);
        showValue();
    }


    public void setFromSlider(int i) {
        value = i;
        showValue();
        notifyValueChange();
    }


    private void showValue() {
        String stxt ="" + value;
        dTextField.setText(stxt);
    }


    public void notifyValueChange() {
        if (labelActor != null) {
            labelActor.labelAction("value_changed", true);
        }
    }



    public void setLabelActor(LabelActor lact) {
        labelActor = lact;
    }



    public void setEditable(boolean b) {
        dTextField.setEditable(b);
    }


    public void labelAction(String s, boolean b) {
        String sval = dTextField.getText();
        if (s.equals("accept_text")) {

            try {
                int i = (new Integer(sval)).intValue();
                setValue(i);
                // dFloatSlider.setValue(d);

                notifyValueChange();
            } catch (Exception ex) {
                E.error(" - not a number " + sval);
            }

        } else if (s.equals("accept_double")) {
            // double d = dFloatSlider.getValue();
            // setValue(d);
            // dTextField.setFieldText(Formatter.print(d));
            // POSERR

            notifyValueChange();


        } else {
            E.error("unhandled label action in DFloat " + s);
        }
    }




    public void textChanged(String stxt) {

    }



    public void textEntered(String txt) {
        exportFromText();
    }


    public void textEdited(String txt) {
        exportFromText();
    }

    private void exportFromText() {
        String sval = dTextField.getText();
        try {
            int i = (new Integer(sval)).intValue();
            setValue(i);
            //     dFloatSlider.setValue(d);
            notifyValueChange();

        } catch (Exception ex) {
            E.error(" - not a number " + sval);
        }


    }


    public void focusGained() {
        // TODO Auto-generated method stub

    }


    public void focusLost() {
        // TODO Auto-generated method stub

    }

}
