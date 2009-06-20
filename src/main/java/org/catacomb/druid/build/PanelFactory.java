package org.catacomb.druid.build;

import org.catacomb.druid.gui.base.PanelPack;
import org.catacomb.interlish.structure.InfoReceiver;



public interface PanelFactory {


    boolean canMake(String sid);

    PanelPack newPanelPack(String sid);

    PanelPack getSingletonPack(String string);

    InfoReceiver getInfoReceiver();

}
