package org.textensor.stochdiff.examples.branch;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class BranchTest {

    public static void main(String[] argv) {
        String snm = BranchTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "BranchModelRad1a.xml"};


        StochDiff.main(args);

        String[] sa = {srt + "BranchModelRad1a.out"};
        CCViz.main(sa);
    }


}
