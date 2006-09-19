package org.textensor.stochdiff;

import org.catcmb.dataview.CCViz;


public class SDTestStoch {


    public static void main(String[] argv) {
        // String[] args = {"data/test/model1.xml"};
        String[] args = {"data/test/PKA1model.xml"};


        StochDiff.main(args);

        String[] sa = {"data/test/PKA1model.out"};
        CCViz.main(sa);
    }


}
