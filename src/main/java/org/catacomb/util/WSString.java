package org.catacomb.util;


public class WSString {
    String sfull;
    String ss;

    public WSString(String s) {
        sfull = s;
        ss = s.trim();
    }


    public String toString() {
        return sfull;
    }

    public int hashCode() {
        return ss.hashCode();
    }

    public boolean equals(Object o2) {
        boolean ret = false;
        if (o2 instanceof WSString) {
            ret = ss.equals(((WSString)o2).ss);
        }
        return ret;
    }


}
