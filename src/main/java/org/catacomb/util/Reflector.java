package org.catacomb.util;

import org.catacomb.report.E;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;


public class Reflector {



    public static HashMap<?, ?> getHashMapField(Object parent, String fieldname) {

        HashMap<?, ?> ret = null;

        Object val = get(parent, fieldname);

        if (val instanceof HashMap<?,?>) {
            ret = (HashMap<?,?>)val;

        } else {
            E.error("wrong type field value " + val + " but need hash map");
        }
        return ret;
    }




    public static ArrayList<?> getArrayListField(Object parent, String fieldname) {

        ArrayList<?> ret = null;
        Object val = get(parent, fieldname);

        if (val instanceof ArrayList<?>) {
            ret = (ArrayList<?>)val;

        } else {
            E.error("wrong type field value " + val + " but need ArrayList");
        }
        return ret;
    }



    public static String getStringField(Object parent, String fieldname) {
        String ret = null;
        Object val = get(parent, fieldname);

        if (val instanceof String) {
            ret = (String)val;

        } else {
            E.error("wrong type field value " + val + " but need String");
        }
        return ret;
    }






    private static Object get(Object parent, String fieldname) {

        Object val = null;

        try {
            Field f = parent.getClass().getField(fieldname);
            val = f.get(parent);

        } catch (Exception ex) {
            E.error("cannot get " + fieldname + " on " + parent + " " + ex);
        }

        return val;
    }


}
