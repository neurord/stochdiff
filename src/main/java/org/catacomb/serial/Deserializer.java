package org.catacomb.serial;


import org.catacomb.interlish.reflect.ObjectBuilder;
import org.catacomb.interlish.reflect.ReflectionConstructor;
import org.catacomb.interlish.resource.ImportContext;
import org.catacomb.interlish.structure.Element;
import org.catacomb.interlish.structure.ProgressReport;
import org.catacomb.report.E;
import org.catacomb.serial.om.ElementConstructor;
import org.catacomb.serial.xml.XMLReader;




public class Deserializer {



    public static Object deserialize(String s) {
        return deserialize(s, null, null);
    }


    public static Object deserialize(String s, ProgressReport pr) {
        return deserialize(s, null, pr);
    }



    public static Object deserialize(String s, ImportContext impctx) {
        return deserialize(s, impctx, null);
    }


    public static Element readElement(String s) {
        ElementConstructor ein = new ElementConstructor();
        XMLReader reader = new XMLReader(ein);
        Object eltobj = reader.readObject(s);
        return (Element)eltobj;
    }


    public static Object deserialize(String s, ImportContext impctx, ProgressReport pr) {
        ElementConstructor ein = new ElementConstructor();
        XMLReader reader = new XMLReader(ein);
        if (pr != null) {
            reader.setProgressReport(pr);
        }
        Object eltobj = reader.readObject(s);
        Element elt = (Element)eltobj;

        // now the whole thing is in an element tree;

        ReflectionConstructor rin = new ReflectionConstructor();
        rin.setImportContext(impctx);
        ObjectBuilder builder = new ObjectBuilder(rin);


        Object ret = null;
        if (elt != null) {
            ret = builder.buildFromElement(elt);
        } else {
            E.error("null element read from " + s);
        }

        return ret;
    }



    public static Object deserialize(Element elt) {

        ReflectionConstructor rin = new ReflectionConstructor();
        rin.setImportContext(null);
        ObjectBuilder builder = new ObjectBuilder(rin);


        Object ret = builder.buildFromElement(elt);

        return ret;
    }


}
