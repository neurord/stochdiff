package org.textensor.stochdiff.model;

import org.textensor.stochdiff.inter.BodyValued;


public class MaxAspectRatio implements BodyValued {


    public double value;


    public void setBodyValue(String s) {
        value = Double.parseDouble(s);
        //  E.info("max elt side set body val " + s + "   " + value);
    }

}
