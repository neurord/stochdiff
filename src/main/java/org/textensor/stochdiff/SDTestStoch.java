package org.textensor.stochdiff;

import org.catacomb.dataview.CCViz;


public class SDTestStoch {


    public static void main(String[] argv) throws Exception {
        // String[] args = {"data/test/model1.xml"};
        String[] args = {"data/test/PKA2model.xml"};


        StochDiff.main(args);

        String[] sa = {"data/test/PKA2model.out"};
        CCViz.main(sa);
    }


}
