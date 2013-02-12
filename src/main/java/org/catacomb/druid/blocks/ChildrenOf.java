package org.catacomb.druid.blocks;

import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.interlish.structure.AdderTo;
import org.catacomb.report.E;



public class ChildrenOf implements AdderTo {

    public String src;



    public void addTo(AddableTo ato) {

        Object obj = ResourceAccess.getResourceLoader().getResource(src, null);

        if (obj instanceof AdderTo) {
            ((AdderTo)obj).addTo(ato);
        } else {
            E.error("cannot add children from " + obj);
        }

    }



}
