package org.catacomb.interlish.reflect;

import org.catacomb.interlish.service.ContentLoader;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.report.E;


// all non-trivial instantiation (ie not primitive types) should go thrueh here in the end ??? REFAC
public class ExternInstantiator {


    public Object newExternalValue(String type) {
        ContentLoader xl = ResourceAccess.getContentLoader();
        Object ret = null;

        String emsg = "";

        if (xl.hasFactoryFor(type)) {
            ret = (xl.getFactoryFor(type)).make(type);

        } else {

            try {
                Class c = Class.forName(type);
                ret = c.newInstance();
            } catch (Exception ex) {
                emsg = "" + ex;
            }

        }

        if (ret == null) {
            E.error("model instantiator is unable to make " + type + " : " +
                    "no factory, and direct instantiation failed - " + emsg);

        }

        return ret;
    }



}
