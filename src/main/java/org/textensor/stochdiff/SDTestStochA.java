package org.textensor.stochdiff;

import org.catcmb.dataview.CCViz;


public class SDTestStochA {


    public static void main(String[] argv) {
        // String[] args = {"data/test/model1.xml"};
        String[] args = {"data/test/PKA3model.xml"};


        StochDiff.main(args);

        String[] sa = {"data/test/PKA3model.out"};
        CCViz.main(sa);
    }


}
