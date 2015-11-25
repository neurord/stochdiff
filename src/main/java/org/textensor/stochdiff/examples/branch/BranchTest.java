package org.textensor.stochdiff.examples.branch;

import org.textensor.stochdiff.StochDiff;

public class BranchTest {

    public static void main(String[] argv) throws Exception {
        String snm = BranchTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "BranchModelRad1a.xml"};

        StochDiff.main(args);
    }
}
