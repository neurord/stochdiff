package org.catacomb.interlish.content;

import org.catacomb.report.E;


public class ColorValue extends PrimitiveValue {


    private int cval;


    public ColorValue() {
        super();
        cval = 0;
    }

    public ColorValue(int ic) {
        super();
        cval = ic;
    }

    public ColorValue(String s) {
        super();
        silentSetColor(s);
    }


    public void silentSetColor(String s) {
        try {
            cval = Integer.decode(s).intValue();

        } catch (NumberFormatException ex) {
            E.error("cant decode color string " + s);
        }
    }


    public void silentSetColor(int ic) {
        cval = ic;
        logChange();
    }

    public int getIntColor() {
        return cval;
    }


    public void clear() {
        silentSetColor(0);
    }

    public void reportableSetColor(int ic, Object src) {
        silentSetColor(ic);
        /*
        E.info("color set to " + ((ic >>16) & 255) + " " + ((ic >> 8) & 255) + " " +
                 ((ic & 255)));
        */
        reportValueChange(src);
    }

    public void reportableSetColor(String s, Object src) {
        silentSetColor(s);
        reportValueChange(src);
    }

    public void copyFrom(ColorValue src) {
        silentSetColor(src.getIntColor());
    }

    public String getAsString() {
        String ret = "000000" + Integer.toHexString(cval);
        ret = "0x" + ret.substring(ret.length()-6, ret.length());
        return ret;
    }

}
