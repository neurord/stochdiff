package org.textensor.stochdiff.examples.morphtest;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class MorphTest {

    public static void main(String[] argv) {
        String snm = MorphTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String mfnm = "model_morphml_1";

        String[] args = {srt + mfnm + ".xml"};


        StochDiff.main(args);

        String[] sa = {srt + mfnm + ".out"};
        CCViz.main(sa);
    }


}
