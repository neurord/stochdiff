
package org.catacomb.druid.util.ctrl;

import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.interlish.annotation.IOPoint;
import org.catacomb.interlish.structure.*;




public class WindowCloser implements Controller, GUISourced {

    @IOPoint(xid="MainFrame")
    public DruFrame frame;


    public String getGUISources() {
        return "MainFrame";
    }


    public void attached() {
    }

    public void show(Object obj) {
    }


    public void requestClose() {
        frame.setVisible(false);
    }


}
