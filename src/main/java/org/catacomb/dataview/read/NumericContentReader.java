package org.catacomb.dataview.read;

import org.catacomb.report.E;


import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class NumericContentReader extends BaseContentReader {

    byte[] bytes;


    public NumericContentReader(byte[] ba, FUImportContext ctxt) {
        super(ctxt);
        bytes = ba;
    }


    public Object getMain() {
        Object ret = null;
        try {
            InputStream bais = new ByteArrayInputStream(bytes);
            ret = NumericDataRW.read(bais);

        } catch (Exception ex) {
            E.error("num binary read exception " + ex);
        }
        return ret;
    }



}

