package org.textensor.stochdiff;

import org.catacomb.dataview.CCViz;


public class SDTestDet {


    public static void main(String[] argv) {
        String[] args = {"data/test/model1-det.xml"};

        StochDiff.main(args);

        String[] sa = {"data/test/model1-det.out"};
        CCViz.main(sa);
    }


}
