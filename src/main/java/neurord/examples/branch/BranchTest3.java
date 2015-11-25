package neurord.examples.branch;

import neurord.StochDiff;

public class BranchTest3 {

    public static void main(String[] argv) throws Exception {
        String snm = BranchTest3.class.getPackage().getName();
        System.out.println("pnm is " + snm);
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "BranchModelRad3a.xml"};

        StochDiff.main(args);
    }
}
