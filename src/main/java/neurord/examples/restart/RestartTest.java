package neurord.examples.restart;

import neurord.StochDiff;

public class RestartTest {

    public static void main(String[] argv) throws Exception {
        String snm = RestartTest.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model2.xml"};

        StochDiff.main(args);
    }
}
