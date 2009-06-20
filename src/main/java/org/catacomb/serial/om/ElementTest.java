package org.catacomb.serial.om;

import org.catacomb.serial.ElementXMLReader;
import org.catacomb.serial.xml.XMLWriter;
import org.catacomb.util.DiffStrings;
import org.catacomb.util.FileUtil;

import java.io.File;


public class ElementTest {


    public static void main(String[] argv) {

        File f = new File(argv[0]);

        String s = FileUtil.readStringFromFile(f);

        Object obj = ElementXMLReader.read(s);

        String sout = XMLWriter.serialize(obj);

        /*
        sout = sout.replaceAll("    ", "  ");
        sout = sout.replaceAll("Time", "abc");
        */

        System.out.println("pre length " + s.length() + ", post length " + sout.length());

        DiffStrings.compareNonWhitespace(s, sout);
    }


}
