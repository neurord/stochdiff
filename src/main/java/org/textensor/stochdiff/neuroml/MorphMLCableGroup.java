package org.textensor.stochdiff.neuroml;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;


public class MorphMLCableGroup implements AddableTo {

    public String name;


    public ArrayList<cable> cables = new ArrayList<cable>();


    public void add(Object obj) {
        if (obj instanceof cable) {
            cables.add((cable)obj);

        } else {
            E.warning("cannot add " + obj);
        }
    }


    public String getName() {
        return name;
    }

}
