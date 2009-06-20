package org.catacomb.dataview.read;



public interface ContentType {



    boolean claims(byte[] hdr);


    ContentReader makeReader(byte[] ba, FUImportContext ctxt);


}
