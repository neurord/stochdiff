
package org.catacomb.druid.build;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.io.File;



public class ReceivingController implements Controller {



    public void attached() {
    }


    public void loadFile(File f) {
        E.override("should override laodFile in " + this);
    }


    public void saveToFile(File f) {
        E.override("should override saveFile in " + this);
    }

    public void show(Object obj) {
        E.override("should override show(obj) in " + this);
    }


}
