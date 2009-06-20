package org.catacomb.druid.swing;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.event.TextActor;
import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.MouseActor;
import org.catacomb.interlish.structure.MouseSource;
import org.catacomb.report.E;


import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

public class DFloat extends JPanel
    implements DComponent, LabelActor, MouseSource, TextActor {

    static final long serialVersionUID = 1001;

    public final static int LOG = 1;
    public final static int LIN = 2;

    public int scale;

    double min0;
    double max0;


    DTextField dTextField;
    DFloatSlider dFloatSlider;

    LabelActor labelActor;

    double value;




    public DFloat(double val, double mn, double mx, String scl) {
        min0 = mn;
        max0 = mx;

        setScale(scl);


        setLayout(new BorderLayout(4, 0));
        dTextField = new DTextField("         ", 8);
        add("West", dTextField);
        dTextField.setLineBorder(0xc0c0c0);
        dTextField.setTextActor(this);
        dTextField.enableReturnEvents();

        dFloatSlider = new DFloatSlider(this, val, mn, mx, scale);
        dFloatSlider.setLabelActor(this);
        add("Center", dFloatSlider);
        showValue();

        dTextField.setTextActor(this);

    }


    public void setTooltip(String s) {
        dFloatSlider.setToolTipText(s);
        dTextField.setToolTipText(s);
    }


    public void setProperties(double val, double min, double max, String strscl) {
        min0 = min;
        max0 = max;
        setScale(strscl);
        dFloatSlider.setRange(min0, max0);
        dFloatSlider.setValue(val);
    }


    private final void setScale(String scl) {
        if (scl == null) {
            if (min0 > 0.) {
                scale = LOG;
            } else {
                scale = LIN;
            }

        } else if (scl.startsWith("log")) {
            scale = LOG;

        } else if (scl.startsWith("lin")) {
            scale = LIN;
        } else {
            E.error(" - unrecognized scale in DFloat " + scl);
            scale = LOG;
        }

        if (dFloatSlider != null) {
            dFloatSlider.setScale(scale);
        }
    }


    public void setMouseActor(MouseActor ma) {
        dFloatSlider.setMouseActor(ma);

    }


    public void setLabel(String s) {
        dFloatSlider.setLabel(s);
    }


    public void setBg(Color c) {
        dFloatSlider.setBg(c);
        setBackground(c);
    }



    public double getValue() {
        return value;
    }


    public void setValue(double d) {
        setValue(d, null);
    }

    @SuppressWarnings("unused")
    public void setValue(double d, Object src) {
        value = d;
        dFloatSlider.setValue(d);
        showValue();
    }


    public void setFromSlider(double d) {
        value = d;
        showValue();
        notifyValueChange();
    }


    private void showValue() {
        // TODO this 4 should be a settable no of sig figs
        dTextField.setText(String.format("%.4g", new Double(value)));
        // Formatter.format(value, 0.1 * dFloatSlider.getTotalRange()));
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


    private void exportFromText() {
        String sval = dTextField.getText();
        try {
            double d = (new Double(sval)).doubleValue();
            setValue(d);
            dFloatSlider.setValue(d);
            notifyValueChange();

        } catch (Exception ex) {
            E.error(" - not a number " + sval);
        }


    }




    public void labelAction(String s, boolean b) {
        if (s.equals("return_pressed")) {
            // should be caught by text actor?;

        } else if (s.equals("accept_text")) {
            exportFromText();

        } else if (s.equals("accept_double")) {
            double d = dFloatSlider.getValue();
            setValue(d);
            showValue();

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


    public void focusGained() {
        // TODO Auto-generated method stub

    }


    public void focusLost() {
        // TODO Auto-generated method stub

    }

}
