package org.textensor.stochdiff;

import org.catcmb.dataview.CCViz;


public class SDTestSuite2A {


    public static void main(String[] argv) {
        String root = "data/jan11/TestSuite/Test2A/Test2A_model";
        String[] args = {root + ".xml"};

        StochDiff.main(args);

        String[] sa = {root + ".out"};
        CCViz.main(sa);
    }


}
