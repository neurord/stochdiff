package org.catacomb.interlish.content;


public class BooleanValue extends PrimitiveValue {


    private boolean boolval;

    private BooleanValue peer;


    public BooleanValue() {
        super();
        boolval = false;
    }

    public BooleanValue(boolean b) {
        super();
        boolval = b;
    }


    public String toString() {
        return (boolval ?  "true" : "false");
    }

    public void silentSetBoolean(boolean b) {
        boolval = b;
        logChange();
        if (peer != null) {
            peer.silentSetBoolean(b);
        }
    }

    public boolean getBoolean() {
        boolean ret = boolval;
        if (peer != null) {
            ret = peer.getBoolean();
        }
        return ret;
    }

    public boolean is() {
        return getBoolean();
    }


    public void reportableSetBoolean(boolean b, Object src) {
        silentSetBoolean(b);
        reportValueChange(src);
        if (peer != null) {
            peer.reportableSetBoolean(b, src);
        }
    }


    public void setPeer(BooleanValue dv) {
        peer = dv;
    }

    public void releasePeer() {
        peer = null;
    }

    public void copyFrom(BooleanValue src) {
        boolval = src.boolval;
    }



}
