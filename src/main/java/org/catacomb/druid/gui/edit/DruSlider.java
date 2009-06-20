package org.catacomb.druid.gui.edit;

import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DSlider;

import java.awt.Color;



public class DruSlider extends DruGCPanel implements LabelActor {

    static final long serialVersionUID = 1001;

    DSlider dSlider;



    public DruSlider(int npt, String action) {
        super();

        if (action != null && action.length() > 0) {
            methodName = action;
        } else {
            methodName = "sliderMoved";
        }
        dSlider = new DSlider(npt);

        addSingleDComponent(dSlider);
        dSlider.setLabelActor(this);
        dSlider.setMouseActor(this);
    }


    public void setBg(Color c) {
        dSlider.setBg(c);
        super.setBg(c);
    }


    public void setNFrame(int nfr) {
        dSlider.setNPoint(nfr);
    }

    public void showValue(int ipos) {
        dSlider.showValue(ipos);
    }


    public void setValues(String[] sa) {
        dSlider.setValues(sa);
    }


    public int getValue() {
        return dSlider.getValue();
    }


    public void labelAction(String s, boolean b) {
        action();
    }


    public void pointShown(int ifr, String desc) {
        dSlider.pointShown(ifr, desc);
    }

}
