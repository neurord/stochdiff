package org.catacomb.druid.gui.edit;

import java.awt.Color;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DSimpleSlider;



public class DruSimpleSlider extends DruGCPanel implements LabelActor {

    static final long serialVersionUID = 1001;

    DSimpleSlider dSlider;



    public DruSimpleSlider(int min, int max, String label, String action) {
        super();

        if (action != null && action.length() > 0) {
            methodName = action;
        } else {
            methodName = "sliderMoved";
        }
        dSlider = new DSimpleSlider(min, max, label);

        addSingleDComponent(dSlider);
        dSlider.setLabelActor(this);
        // dSlider.setMouseActor(this);
    }


    public void setBg(Color c) {
        dSlider.setBg(c);
        super.setBg(c);
    }

    public void setTooltip(String s) {
        dSlider.setTooltip(s);
    }


    public void labelAction(String s, boolean b) {
        int pos = dSlider.getSliderValue();
        action(pos);
    }



}
