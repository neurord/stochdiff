package org.textensor.stochdiff.examples.branch;

import org.catacomb.dataview.CCViz;
import org.textensor.stochdiff.StochDiff;

public class BranchTest3 {

    public static void main(String[] argv) throws Exception {
        String snm = BranchTest3.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "BranchModelRad3a.xml"};


        StochDiff.main(args);

        String[] sa = {srt + "BranchModelRad3a.out"};
        CCViz.main(sa);
    }


}
