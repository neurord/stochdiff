package org.catacomb.serial.xml;

import org.catacomb.report.E;

import java.util.ArrayList;

public class NVPair {

    String name;

    Object value;

    ArrayList<NVPair> pairValue;



    public NVPair(String s, Object obj) {
        name = s;
        value = obj;
    }



    public NVPair(String s) {
        name = s;
        pairValue = new ArrayList<NVPair>();
    }


    public String getName() {
        return name;
    }

    public Object getValue() {
        Object ret = null;
        if (pairValue != null) {
            ret = pairValue;
        } else {
            ret = value;
        }
        return ret;
    }


    public void addNVPair(NVPair nvp) {
        if (pairValue != null) {
            pairValue.add(nvp);
        } else {
            E.error("NVPair cannot add a NVPair - null value array ");
        }
    }

}
