package org.catacomb.interlish.content;


public class DoubleValue extends PrimitiveValue {


    private double dble;

    private DoubleValue peer;


    public DoubleValue() {
        super();
        dble = 0.0;
    }



    public DoubleValue(double d) {
        super();
        dble = d;
    }

    public String toString() {
        return String.format("%.3g", new Double(dble));
    }


    public void setPeer(DoubleValue dv) {
        peer = dv;
        peer.reportableSetDouble(dble, this);
    }


    public void releasePeer() {
        peer = null;
    }


    public void silentSetDouble(double d) {
        dble = d;
        logChange();
        if (peer != null) {
            peer.silentSetDouble(d);
        }
    }

    public double getDouble() {
        double ret = 0.;
        if (peer != null) {
            ret = peer.getDouble();
            dble = ret;
        } else {
            ret = dble;
        }
        return ret;
    }


    public void setValue(double d) {
        reportableSetDouble(d, null);
    }

    public void reportableSetDouble(double d, Object src) {
        silentSetDouble(d);
        reportValueChange(src);
        if (peer != null) {
            peer.reportableSetDouble(d, src);
        }
    }



    public void copyFrom(DoubleValue src) {
        silentSetDouble(src.getDouble());
    }





}
