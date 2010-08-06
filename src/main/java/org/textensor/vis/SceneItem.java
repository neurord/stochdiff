package org.textensor.vis;

import javax.media.j3d.BranchGroup;


public class SceneItem {

    String id;
    BranchGroup bGroup;
    boolean showing = false;

    public SceneItem(String sid, BranchGroup bg) {
        id = sid;
        bGroup = bg;
    }

    public void setShowing(boolean b) {
        showing = b;
    }

    public void setID(String s) {
        id = s;
    }

    public String getID() {
        return id;
    }

    public BranchGroup getBranchGroup() {
        return bGroup;
    }

}
