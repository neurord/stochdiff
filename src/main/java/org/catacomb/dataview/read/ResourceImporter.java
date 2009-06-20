package org.catacomb.dataview.read;

import org.catacomb.report.E;




public class ResourceImporter {


    ContentType[] types;


    public ResourceImporter() {
        types = new ContentType[3];
        int i = 0;
        types[i++] = new XMLContentType();
        types[i++] = new NumericContentType();
        types[i++] = new CustomJarContentType();
    }





    public ContentReader getReader(byte[] ba, FUImportContext ctxt) {
        byte[] hdr = new byte[256];
        if (ba.length < hdr.length) {
            System.arraycopy(ba, 0, hdr, 0, ba.length);
        } else {
            System.arraycopy(ba, 0, hdr, 0, hdr.length);
        }

        ContentType ctype = getContentType(hdr);
        return ctype.makeReader(ba, ctxt);
    }




    public ContentType getContentType(byte[] ba) {
        ContentType ret = null;
        for (int i = 0; i < types.length; i++) {
            if (types[i].claims(ba)) {
                ret = types[i];
                break;
            }
        }


        if (ret == null) {
            E.error("no type claimed - assuming jar ");
            ret = new CustomJarContentType();
        }
        return ret;
    }

}
