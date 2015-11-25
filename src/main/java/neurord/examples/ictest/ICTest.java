package neurord.examples.ictest;

import neurord.StochDiff;

public class ICTest {

    public static void main(String[] argv) throws Exception {
        String snm = ICTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model2.xml"};

        StochDiff.main(args);
    }
}
