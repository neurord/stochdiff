package org.catacomb.druid.gui.base;


import org.catacomb.druid.gui.edit.Effect;
import org.catacomb.interlish.structure.InfoReceiver;




public class DruInfoEffect extends Effect  {

    String infoTitle;
    String infoText;

    InfoReceiver infoReceiver;

    public DruInfoEffect(String stit, String stxt, InfoReceiver ir) {
        super();
        infoTitle = stit;
        infoText = stxt;
        infoReceiver = ir;
    }



    public void apply(boolean b) {
        infoReceiver.receiveInfo(infoTitle, infoText);
    }



}
