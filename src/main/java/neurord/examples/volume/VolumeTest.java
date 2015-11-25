package neurord.examples.volume;

import neurord.StochDiff;

public class VolumeTest {

    public static void main(String[] argv) throws Exception {
        String snm = VolumeTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model.xml"};

        StochDiff.main(args);
    }
}
