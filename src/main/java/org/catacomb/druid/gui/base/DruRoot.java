
package org.catacomb.druid.gui.base;

import org.catacomb.interlish.structure.ControllerSpecifier;
import org.catacomb.interlish.structure.Documented;


public class DruRoot implements ControllerSpecifier, Documented {

    public String name;

    public String documentation;

    public String controllerPath;

    public void setControllerPath(String s) {
        controllerPath = s;
    }

    public String getControllerPath() {
        return controllerPath;
    }



    public void setName(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public void setDoc(String s) {
        documentation = s;
    }

    public String getDoc() {
        return documentation;
    }

}
