package org.catacomb.numeric.data;

import org.catacomb.interlish.reflect.Narrower;




public class FloatVector extends DataItem {

    double[] value;


    public FloatVector(String nm, double[] v) {
        super(nm);
        value = v;
    }


    public FloatVector(String nm, String sva) {
        super(nm);
        value = Narrower.readDoubleArray(sva);
    }





    public double[] getValue() {
        return value;
    }


    public int length() {
        return value.length;
    }


    public String getStringValue() {
        StringBuffer sb = new StringBuffer();
        if (value.length > 0) {
            sb.append(value[0]);
        }
        for (int i = 1; i < value.length; i++) {
            sb.append(", " + value[i]);
        }
        return sb.toString();
    }


    public DataItem getMarked() {
        return this;
    }
}
