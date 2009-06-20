package org.catacomb.serial.quickxml;

import org.catacomb.util.FileUtil;

import java.io.File;




public class XMLFileElement extends Element {

    File felt;



    public XMLFileElement(File fdir, String enm) {
        super(enm);
        if (fdir.exists()) {

        } else {
            fdir.mkdirs();
        }

        felt = new File(fdir, enm + ".xml");

        if (felt.exists()) {
            String s = FileUtil.readStringFromFile(felt);
            populateFrom(s);

        } else {
            sync();
        }


    }



    public void sync() {
        String s = dump();
        FileUtil.writeStringToFile(s, felt);
    }




}
