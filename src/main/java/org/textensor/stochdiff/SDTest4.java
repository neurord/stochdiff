package org.textensor.stochdiff;

import org.catacomb.dataview.CCViz;


public class SDTest4 {


    public static void main(String[] argv) throws Exception {
        String root = "data/jan30/Test4A_model0";
        String[] args = {root + ".xml"};

        StochDiff.main(args);

        String[] sa = {root + ".out"};
        CCViz.main(sa);
    }


}
