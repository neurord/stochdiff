package org.textensor.stochdiff.model;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;

public class SpecieIDList implements AddableTo {


    public ArrayList<Specie> elements;

    public SpecieIDList() {
        elements = new ArrayList<Specie>();
    }

    public void add(Object obj) {
        if (obj instanceof Specie) {
            elements.add((Specie)obj);
        } else {
            E.error("cannot add " + obj + " to a specie list");
        }
    }

}
