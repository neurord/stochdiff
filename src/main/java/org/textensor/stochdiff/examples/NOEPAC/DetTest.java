package org.textensor.stochdiff.examples.NOEPAC;

import org.textensor.stochdiff.StochDiff;

public class DetTest {

    public static void main(String[] argv) throws Exception {
        String snm = DetTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String dfnm = "HEK293model_SLIMCELL_NOEPAC_continuous";

        String[] args = {srt + dfnm + ".xml"};

        StochDiff.main(args);
    }
}
