package org.catacomb.dataview.read;

import org.catacomb.numeric.data.NumDataSet;
import org.catacomb.serial.Serializer;
import org.catacomb.util.FileUtil;

import java.io.File;




public class Exporter {



    public static void export(Object obj, File fdest) {
        // ADHOC
        if (obj instanceof NumDataSet) {
            byte[] ba = NumericDataRW.binarize((NumDataSet)obj);
            FileUtil.writeBytes(ba, fdest);


        } else {
            String s = Serializer.serialize(obj);
            FileUtil.writeStringToFile(s, fdest);
        }

    }



}
