package org.textensor.stochdiff.neuroml;

import org.textensor.stochdiff.inter.BodyValued;


public class MetaItem implements BodyValued, MetaContainer {

    public String name;
    public String value;

    public MetaItem meta_tag;
    public MetaItem meta_name;
    public MetaItem meta_value;
    public MetaItem meta_property;


    meta meta;


    public void addMetaItem(MetaItem mi) {
        if (meta == null) {
            meta = new meta();
        }
        meta.add(mi);
    }

    public MetaItem(String n) {
        name = n;

    }

    public void setName(String s) {
        name = s;
    }

    public void setValue(String s) {
        value = s;
    }

    public void setBodyValue(String s) {
        value = s;
    }

}
