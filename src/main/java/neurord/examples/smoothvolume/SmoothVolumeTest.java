package neurord.examples.smoothvolume;

import neurord.StochDiff;

public class SmoothVolumeTest {

    public static void main(String[] argv) throws Exception {
        String snm = SmoothVolumeTest.class.getPackage().getName();
        String srt = "src/" + snm.replaceAll("\\.", "/") + "/";

        String[] args = {srt + "model.xml"};

        StochDiff.main(args);
    }
}
