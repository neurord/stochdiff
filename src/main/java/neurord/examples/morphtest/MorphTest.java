package neurord.examples.morphtest;

import neurord.StochDiff;

public class MorphTest {

    public static void main(String[] argv) throws Exception {
        String snm = MorphTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String mfnm = "model_morphml_1";

        String[] args = {srt + mfnm + ".xml"};

        StochDiff.main(args);
    }
}
