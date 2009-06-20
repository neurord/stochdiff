package org.catacomb.druid.xtext.data;

import org.catacomb.interlish.structure.TextDisplayed;


public class XRelationType implements TextDisplayed {

    String id;


    public XRelationType(String s) {
        id = s;
    }


    public String getID() {
        return id;
    }


    public String getDisplayText() {
        return id;
    }





}
