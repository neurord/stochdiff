package org.catacomb.numeric.data;

import org.catacomb.interlish.reflect.Narrower;



public class FloatRow {

    double[] value;



    public FloatRow(double[] v) {
        value = v;
    }


    // REFAC - use same as is used in deserializer
    public FloatRow(String s) {
        value = Narrower.readDoubleArray(s);
    }



    public double[] getValue() {
        return value;
    }

}
