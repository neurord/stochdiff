package org.catacomb.druid.swing;

import java.awt.Color;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.interlish.interact.DComponent;


public class DSimpleSlider extends JSlider implements DComponent, ChangeListener {

    LabelActor labelActor;

    public DSimpleSlider(int min, int max, String label) {
        super(min, max);
        addChangeListener(this);
    }


    public void setLabelActor(LabelActor lact) {
        labelActor = lact;
    }


    private void notifyChange() {
        if (labelActor != null) {
            labelActor.labelAction("change", true);
        }
    }


    public int getSliderValue() {
        return getValue();
    }

    public void export() {
        notifyChange();
    }


    public void stateChanged(ChangeEvent e) {
        notifyChange();
    }


    public void setTooltip(String s) {
        super.setToolTipText(s);
    }


    public void setBg(Color c) {
        setBackground(c);

    }

}
