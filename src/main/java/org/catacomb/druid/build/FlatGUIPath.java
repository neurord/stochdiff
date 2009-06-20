package org.catacomb.druid.build;

// REFAC probably should inherit other way round
public class FlatGUIPath extends GUIPath {


    public FlatGUIPath() {
        path="";
        unique = false;
    }


    public FlatGUIPath(String id) {
        path = id;
        unique = true;
    }


    public FlatGUIPath extend(String id) {
        FlatGUIPath ret = null;

        if (id != null && id.length() > 0) {
            ret = new FlatGUIPath(id);
        } else {
            ret = new FlatGUIPath();
            // could return this instead of a new one if could handle unique flag EFF
        }
        return ret;
    }

}
