package org.catacomb.druid.manifest;

import org.catacomb.interlish.resource.Role;



public class ClassRole extends Role {


    public ClassRole(String res, String act, String val) {
        super(res, act, val);
    }

    public ClassRole() {
        super();
    }

    public String toString() {
        return ("fun=" + function + " val=" + value + " res=" + resource);
    }

}
