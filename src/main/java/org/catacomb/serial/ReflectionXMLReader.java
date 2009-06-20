package org.catacomb.serial;


import org.catacomb.interlish.reflect.ReflectionConstructor;
import org.catacomb.serial.xml.XMLReader;

public class ReflectionXMLReader {


    // should deprecate this.... only used for cocomac

    public static Object deserialize(String src) {
        return readObject(src, null);
    }


    public static Object readObject(String src, String pkg) {
        ReflectionConstructor rin = new ReflectionConstructor();
        rin.addSearchPackage(pkg);

        XMLReader reader = new XMLReader(rin);

        Object obj = reader.read(src);

        return obj;

    }

}
