package org.catacomb.util;

import java.util.zip.CRC32;



public class CRC {


    public String getCRCString(String s) {
        CRC32 crc = new CRC32();
        crc.update(s.getBytes());
        long lval = crc.getValue();
        String sval = "" + lval;
        return sval;
    }

}


