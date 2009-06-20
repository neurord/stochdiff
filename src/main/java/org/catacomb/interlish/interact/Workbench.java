package org.catacomb.interlish.interact;

import java.awt.Color;

import org.catacomb.interlish.structure.InfoReceiver;
import org.catacomb.interlish.structure.ModeController;


public interface Workbench extends DComponent {

    void setPreferredSize(int prefw, int prefh);

    void setBg(Color bgc);

    void setSunkenBorder(Color bgc);

    void setModeController(ModeController modeController);

    void setInfoReceiver(InfoReceiver infoReceiver);

    void setCanvasColor(Color canvasColor);

    void setShelfColor(Color shelfColor);

    void setAssembly(Object ass);

}
