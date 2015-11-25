package neurord.examples.branch;

import neurord.StochDiff;

public class BranchTest {

    public static void main(String[] argv) throws Exception {
        String snm = BranchTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "BranchModelRad1a.xml"};

        StochDiff.main(args);
    }
}
