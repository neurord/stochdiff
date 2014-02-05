package org.textensor.stochdiff;

import org.catacomb.dataview.CCViz;


public class SDTest1Stoch {


    public static void main(String[] argv) throws Exception {
        String[] args = {"data/test/model1.xml"};
        // String[] args = {"data/test/PKA2model.xml"};


        StochDiff.main(args);

        String[] sa = {"data/test/model1.out"};
        CCViz.main(sa);
    }


}
