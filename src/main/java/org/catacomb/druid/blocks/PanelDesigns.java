package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Realizer;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.interlish.structure.AdderTo;
import org.catacomb.report.E;


import java.util.ArrayList;



public class PanelDesigns implements AddableTo, AdderTo {

    ArrayList<Realizer> realizers;


    public PanelDesigns() {
        realizers = new ArrayList<Realizer>();
    }


    public void add(Object obj) {

        if (obj instanceof Realizer) {
            realizers.add((Realizer)obj);
        } else {
            E.error("cant add " + obj);
        }
    }



    public void addTo(AddableTo ato) {
        for (Realizer rlz : realizers) {
            ato.add(rlz);
        }
    }

}
