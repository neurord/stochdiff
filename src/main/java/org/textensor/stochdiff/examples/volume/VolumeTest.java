package org.textensor.stochdiff.examples.volume;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class VolumeTest {

    public static void main(String[] argv) {
        String snm = VolumeTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model.xml"};


        StochDiff.main(args);

        String[] sa = {srt + "model.out"};
        CCViz.main(sa);
    }


}
