package org.catacomb.numeric.data;


public class FloatScalar extends DataItem {

    double value;


    public FloatScalar(String nm, String sv) {
        super(nm);
        value = (new Double(sv)).doubleValue();
    }


    public FloatScalar(String nm, double v) {
        super(nm);
        name = nm;
        value = v;
    }


    public FloatScalar(FloatVector fv) {
        super(fv.getName());
        double[] va = fv.getValue();
        if (va.length > 0) {
            value = va[0];
        } else {
            value = 0.;
        }
    }


    public double getValue() {
        return value;
    }


    public DataItem getMarked() {
        return this;
    }
}
