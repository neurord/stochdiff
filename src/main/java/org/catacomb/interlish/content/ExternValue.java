package org.catacomb.interlish.content;


public class ExternValue extends PrimitiveValue {


    private Object exval;


    public ExternValue() {
        super();
        exval = null;
    }

    public ExternValue(Object obj) {
        super();
        exval = obj;
    }


    public String toString() {
        return "" + exval;
    }

    public void silentSetObject(Object obj) {
        exval = obj;
        logChange();
    }

    public Object getObject() {
        return exval;
    }


    public void reportableSetObject(Object ov, Object src) {
        silentSetObject(ov);
        reportValueChange(src);
    }



}
