package org.catacomb.druid.load;

import org.catacomb.interlish.reflect.ObjectBuilder;
import org.catacomb.interlish.reflect.ReflectionConstructor;
import org.catacomb.interlish.service.ResourceLoader;
import org.catacomb.interlish.structure.Element;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.serial.om.ElementConstructor;
import org.catacomb.serial.xml.XMLReader;

import java.util.ArrayList;


public class DruidResourceLoader implements ResourceLoader {


    ArrayList<String> paths = new ArrayList<String>();


    public void addPath(String s) {
        paths.add(s);
    }


    public Object getResource(String configPath, String selector) {
        String s = JUtil.getXMLResource(configPath);
        ReflectionConstructor rin = new ReflectionConstructor();

        if (paths.size() == 0) {
            rin.addSearchPackage("org.catacomb.druid.blocks");
        } else {
            for (String sp : paths) {
                rin.addSearchPackage(sp);
            }
        }


        ElementConstructor ein = new ElementConstructor();
        XMLReader reader = new XMLReader(ein);
        Object eltobj = reader.readObject(s);
        Element elt = (Element)eltobj;

        ObjectBuilder builder = new ObjectBuilder(rin);
        Object ret =  builder.buildFromElement(elt);

        return ret;
    }
}
