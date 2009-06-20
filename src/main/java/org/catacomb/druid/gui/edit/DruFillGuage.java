
package org.catacomb.druid.gui.edit;


import org.catacomb.druid.event.LabelActor;
import org.catacomb.druid.swing.DFillGuage;

import java.awt.Color;


public class DruFillGuage extends DruGCPanel implements LabelActor {
    static final long serialVersionUID = 1001;


    DFillGuage dFillGuage;


    public DruFillGuage(double v, String t) {
        super();


        dFillGuage = new DFillGuage();
        dFillGuage.setStringPainted(true);



        addSingleDComponent(dFillGuage);

        showValue(v, t);

        dFillGuage.setMouseActor(this);
    }


    public void setBg(Color c) {
        dFillGuage.setBg(c);
        super.setBg(c);
        setEtchedUpBorder(c);

    }


    public void showValue(double d, String t) {
        dFillGuage.showValue(d, t);
    }



    public void labelAction(String s, boolean b) {
        exportInfo();
    }


}








