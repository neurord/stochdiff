package org.catacomb.druid.xtext.base;


public class Slot {


    Guise guise;
    public String id;




    public Slot(Guise g, String sid) {
        guise = g;
        id = sid;
    }


    public String getID() {
        return id;
    }

    public Guise getGuise() {
        return guise;
    }

}
