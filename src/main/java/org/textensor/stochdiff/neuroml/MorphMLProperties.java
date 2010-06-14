package org.textensor.stochdiff.neuroml;

import org.textensor.stochdiff.inter.XMLContainer;


public class MorphMLProperties implements XMLContainer, MetaContainer {

    meta meta;

    public void setXMLContent(String s) {
        // we just ignore this - in Neuron 5.9 export it contains invalid XML which
        // would otherwise stop the parser
    }

    public void appendContent(String s) {

    }

    public void addMetaItem(MetaItem mi) {
        if (meta == null) {
            meta = new meta();
        }
        meta.add(mi);
    }

}
