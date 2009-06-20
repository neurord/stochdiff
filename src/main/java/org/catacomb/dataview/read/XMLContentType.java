package org.catacomb.dataview.read;




public class XMLContentType implements ContentType {


    public boolean claims(byte[] ba) {
        String s = new String(ba);
        s = s.trim();
        return (s.startsWith("<"));
    }


    public ContentReader makeReader(byte[] ba, FUImportContext ctxt) {
        return new XMLContentReader(ba, ctxt);
    }


}
