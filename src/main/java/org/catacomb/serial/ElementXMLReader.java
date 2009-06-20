package org.catacomb.serial;

import org.catacomb.serial.om.ElementConstructor;
import org.catacomb.serial.xml.XMLReader;



public class ElementXMLReader {



    public static Object read(String s) {
        return deserialize(s);
    }



    public static Object deserialize(String s) {

        ElementConstructor ein = new ElementConstructor();

        XMLReader reader = new XMLReader(ein);

        Object obj = reader.readObject(s);

        return obj;
    }

}
