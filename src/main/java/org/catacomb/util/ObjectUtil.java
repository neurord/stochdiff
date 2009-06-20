package org.catacomb.util;


public class ObjectUtil {

    public static boolean diff(Object o1, Object o2) {
        boolean ret = true;
        if (o1 == null) {
            if (o2 == null) {
                ret = false;
            }
        } else if (o1 == o2) {
            ret = false;
        }
        return ret;
    }

}
