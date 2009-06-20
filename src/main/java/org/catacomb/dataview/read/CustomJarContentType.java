package org.catacomb.dataview.read;

import org.catacomb.serial.jar.CustomJar;




public class CustomJarContentType implements ContentType {





    public boolean claims(byte[] ba) {
        return CustomJar.claims(ba);
    }



    public ContentReader makeReader(byte[] ba, FUImportContext ctxt) {
        return new CustomJarContentReader(ba, ctxt);
    }

}
