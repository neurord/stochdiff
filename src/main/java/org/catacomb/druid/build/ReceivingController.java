
package org.catacomb.druid.build;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.io.File;



public class ReceivingController implements Controller {



    public void attached() {
    }

    @SuppressWarnings("unused")
    public void loadFile(File f) {
        E.override("should override laodFile in " + this);
    }

    @SuppressWarnings("unused")
    public void saveToFile(File f) {
        E.override("should override saveFile in " + this);
    }

    @SuppressWarnings("unused")
    public void show(Object obj) {
        E.override("should override show(obj) in " + this);
    }


}
