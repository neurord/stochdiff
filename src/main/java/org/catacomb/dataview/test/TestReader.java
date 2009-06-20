

package org.catacomb.dataview.test;


import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.report.E;
import org.catacomb.serial.Serializer;


public class TestReader {


    public static void check(String fnm) {

        E.info("TestReader checking " + fnm);

        Object obj = ResourceAccess.getContentLoader().getResource(fnm, null);
        E.info("   got " + obj);
        String s = Serializer.serialize(obj);
        E.info("   after reserialization ");
        System.out.println(s);
    }

}
