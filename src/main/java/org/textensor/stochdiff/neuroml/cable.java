package org.textensor.stochdiff.neuroml;



public class cable {

    public String id;
    public String name;

    public meta meta_group;

    public String fractAlongParent;

    meta meta;

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }



    public String getLabel() {
        String ret = name;
        if (meta_group != null) {
            ret = meta_group.getLabel();
        }
        return ret;
    }

}
