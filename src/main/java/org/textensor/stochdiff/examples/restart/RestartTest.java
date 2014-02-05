package org.textensor.stochdiff.examples.restart;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class RestartTest {

    public static void main(String[] argv) throws Exception {
        String snm = RestartTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model2.xml"};


        StochDiff.main(args);

        String[] sa = {srt + "model2.out"};
        CCViz.main(sa);
    }


}
