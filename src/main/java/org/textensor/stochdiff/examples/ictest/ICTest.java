package org.textensor.stochdiff.examples.ictest;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class ICTest {

    public static void main(String[] argv) {
        String snm = ICTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model2.xml"};


        StochDiff.main(args);

        String[] sa = {srt + "model2.out"};
        CCViz.main(sa);
    }


}
