package org.textensor.stochdiff.examples.smoothvolume;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class SmoothVolumeTest {

    public static void main(String[] argv) {
        String snm = SmoothVolumeTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model.xml"};


        StochDiff.main(args);

        // String[] sa = {srt + "model.out"};
        // CCViz.main(sa);
    }


}
