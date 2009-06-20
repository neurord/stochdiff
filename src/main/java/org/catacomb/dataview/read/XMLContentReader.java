package org.catacomb.dataview.read;

import org.catacomb.serial.Deserializer;



public class XMLContentReader extends BaseContentReader {

    byte[] bytes;

    public XMLContentReader(byte[] ba, FUImportContext ctxt) {
        super(ctxt);
        bytes = ba;
    }


    public Object getMain() {
        //	 String sdata = new String(bytes, "US-ASCII");
        String sdata = new String(bytes);
        Object ret = Deserializer.deserialize(sdata, getContext());
        return ret;
    }




}
