package neurord;

public class SDTestSuite2A {

    public static void main(String[] argv) throws Exception {
        String root = "data/jan11/TestSuite/Test2A/Test2A_model";
        String[] args = {root + ".xml"};

        StochDiff.main(args);
    }
}
