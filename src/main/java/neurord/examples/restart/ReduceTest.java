package neurord.examples.restart;

import neurord.StochDiff;
import neurord.reduce.Reduce;

public class ReduceTest {

    public static void main(String[] argv) throws Exception {
        String snm = ReduceTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model2.xml", srt + "model2-state-40.nrds"};

        Reduce.main(args);
    }
}
