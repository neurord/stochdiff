package org.catacomb.dataview.read;




public class NumericContentType implements ContentType {





    public boolean claims(byte[] ba) {
        String s = new String(ba);
        s = s.trim();
        return (s.startsWith("org.catacomb"));
    }


    public ContentReader makeReader(byte[] ba, FUImportContext ctxt) {
        return new NumericContentReader(ba, ctxt);
    }



}
