package org.textensor.stochdiff.examples.regions;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class RegionsTest {

    public static void main(String[] argv) {
        String snm = RegionsTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model5.xml"};


        StochDiff.main(args);

        String[] sa = {srt + "model5.out"};
        CCViz.main(sa);
    }


}
