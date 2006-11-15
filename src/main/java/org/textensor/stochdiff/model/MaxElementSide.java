package org.textensor.stochdiff.model;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.BodyValued;


public class MaxElementSide implements BodyValued {

    public String region;

    public double value;


    public void setBodyValue(String s) {
        value = Double.parseDouble(s);
        //  E.info("max elt side set body val " + s + "   " + value);
    }

}
