package org.textensor.stochdiff.examples.regions;

import org.textensor.stochdiff.StochDiff;

public class RegionsTest {

    public static void main(String[] argv) throws Exception {
        String snm = RegionsTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model5.xml"};

        StochDiff.main(args);
    }
}
