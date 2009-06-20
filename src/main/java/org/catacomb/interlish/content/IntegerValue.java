package org.catacomb.interlish.content;


public class IntegerValue extends PrimitiveValue {


    private int intval;


    public IntegerValue() {
        super();
        intval = 0;
    }

    public IntegerValue(int i) {
        super();
        intval = i;
    }


    public String toString() {
        return "" + intval;
    }

    public void silentSetInteger(int i) {
        intval = i;
        logChange();
    }

    public int getInteger() {
        return intval;
    }


    public void reportableSetInteger(int i, Object src) {
        silentSetInteger(i);
        reportValueChange(src);
    }

    public void copyFrom(IntegerValue src) {
        silentSetInteger(src.getInteger());

    }



}
