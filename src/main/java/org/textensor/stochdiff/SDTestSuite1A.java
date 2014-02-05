package org.textensor.stochdiff;

import org.catacomb.dataview.CCViz;


public class SDTestSuite1A {


    public static void main(String[] argv) throws Exception {
        String root = "data/jan11/TestSuite/Test1A/Test1A_model";
        String[] args = {root + ".xml"};

        StochDiff.main(args);

        String[] sa = {root + ".out"};
        CCViz.main(sa);
    }


}
