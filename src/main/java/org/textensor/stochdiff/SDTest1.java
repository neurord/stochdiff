package org.textensor.stochdiff;

import org.catcmb.dataview.CCViz;


public class SDTest1 {


    public static void main(String[] argv) {
        String[] args = {"data/test/model1-det.xml"};

        StochDiff.main(args);

        String[] sa = {"data/test/model1-det.out"};
        CCViz.main(sa);
    }


}
