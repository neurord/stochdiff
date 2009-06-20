package org.catacomb.serial;

import org.catacomb.interlish.structure.Element;
import org.catacomb.serial.jar.CustomJarWriter;
import org.catacomb.serial.xml.XMLWriter;
import org.catacomb.util.FileUtil;


import java.io.File;
import java.util.HashMap;

import java.util.Map;


public class Archivist {




    public static void storeXMLOnly(Object obj, File file) {


        Element elt = (new OmElementizer()).getElement(obj);
        String s = XMLWriter.serialize(elt);
        FileUtil.writeStringToFile(s, file);
    }



    private static String simpleSerialization(Object obj) {


        Element elt = (new OmElementizer()).getElement(obj);
        String s = XMLWriter.serialize(elt);

        // maybe check that nothing has been deposited in the context ?;

        return s;
    }



    public static Object simpleImport(String stxt) {
        Object ret = Deserializer.deserialize(stxt);
        return ret;
    }




    public static void storeXMLWithReferents(Object obj, File file) {


        OmElementizer elementizer = new OmElementizer();

        Element elt = elementizer.makeElement(obj);
        String smain = XMLWriter.serialize(elt);

        HashMap<String, Object> hashHM = elementizer.getContext().getHashMap();


        CustomJarWriter cjarw = new CustomJarWriter(hashHM);
        cjarw.addMain(smain);


        for (Map.Entry<String, Object> me : hashHM.entrySet()) {
            Object val = me.getValue();
            String sdata = "";

            if (val instanceof String) {
                sdata = (String)val;
            } else {
                sdata = simpleSerialization(val);
            }
            cjarw.add(me.getKey(), sdata);
        }

        cjarw.write(file);
    }



}


