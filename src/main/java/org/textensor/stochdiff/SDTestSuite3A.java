package org.textensor.stochdiff;

public class SDTestSuite3A {

    public static void main(String[] argv) throws Exception {
        String root = "data/apr25/New_EPAC_S_272/HEK293model";
        String[] args = {root + ".xml"};

        StochDiff.main(args);
    }
}
