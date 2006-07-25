package org.textensor.stochdiff;

import org.catcmb.dataview.CCViz;


public class SDTest2 {


    public static void main(String[] argv) {
        String[] args = {"data/test/model1.xml"};

        StochDiff.main(args);

        String[] sa = {"data/test/model1.out"};
        CCViz.main(sa);
    }


}
