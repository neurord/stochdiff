package org.textensor.stochdiff.examples.morphsurf;

import org.textensor.stochdiff.StochDiff;

public class MSTest {

    public static void main(String[] argv) throws Exception {
        String snm = MSTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "Model_nosurfaceLayers.xml"};

        StochDiff.main(args);
    }
}
