package neurord.reduce;

import java.io.File;

import neurord.inter.SDState;
import neurord.inter.StateReader;
import neurord.model.SDRun;
import neurord.util.FileUtil;
import neurord.xml.ModelReader;

public class Reduce {

    // The main method - a bit of basic checking and if all is well, create the
    // SDCalc object and run it;

    static final ModelReader<SDRun> loader = new ModelReader(SDRun.class);

    public static void main(String[] argv) throws Exception {
        File modelFile;
        File stateFile = null;

        if (argv.length < 2) {
            System.out.println("Usage: neurord.Reduce modelFile targetStateFile\n"
                               + " where the modelFile is an XML specification of the model to run. \n "
                               + "The targetStateFile is a saved state corresponding to a model in the same grid. \n"
                               + "The initial conditions file from the model is used a a template for a new initial " +
                               "conditions file.");
            System.exit(1);

        } else {
            modelFile = new File(argv[0]);
            if (!modelFile.exists()) {
                System.out.println("ERROR - no such file " + modelFile);
            }

            if (argv.length > 1) {
                stateFile = new File(argv[1]);
                if (!stateFile.exists()) {
                    System.out.println("ERROR - no such file " + stateFile);
                }
            }

            SDRun sdModel = loader.unmarshall(modelFile, null);

            String stxt = FileUtil.readStringFromFile(stateFile);
            SDState sdState = StateReader.readStateString(stxt);

            Reducer rdr = new Reducer(sdModel, sdState);
            rdr.reduce();
        }
    }

    private static void dump(SDRun sdr) throws Exception {
        loader.marshall(sdr, System.out);
    }
}
