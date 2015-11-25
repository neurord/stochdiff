package org.textensor.stochdiff.examples.plane;

import org.textensor.stochdiff.StochDiff;

public class PlaneTest {

    public static void main(String[] argv) throws Exception {
        String snm = PlaneTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model.xml"};

        StochDiff.main(args);
    }
}
