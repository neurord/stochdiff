package org.textensor.stochdiff;

import java.io.File;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.ModelReader;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.xml.XMLWriter;

public class StochDiff {

    // The main method - a bit of basic checking and if all is well, create the
    // SDCalc object and run it;

    public static void main(String[] argv) {
        File modelFile = null;
        File outputFile = null;

        if (argv.length == 0) {
            System.out.println("Usage: org.textensor.stochdiff.StochDiff modelFile [outputFile]\n"
                               + " where the modelFile is an XML specification of the model to run. \n "
                               + "The optional outputFile specifies where the results should be stored. If it is \n"
                               + "not supplied, they are written to modelFile.out");

            System.exit(0);

        } else {
            modelFile = new File(argv[0]);
            if (modelFile.exists()) {

            } else {
                System.out.println("ERROR - no such file " + modelFile);
            }

            if (argv.length > 1) {
                outputFile = new File(argv[1]);

            } else {
                String s = argv[0];
                if (s.indexOf(".") > 0) {
                    s = s.substring(0, s.lastIndexOf("."));
                }
                outputFile = new File(s + ".out");
            }




            ResultWriter rw = new ResultWriter(outputFile);
            SDRun sdModel = ModelReader.read(modelFile);
            sdModel.resolve();

            SDCalc sdCalc = new SDCalc(sdModel);
            sdCalc.setResultWriter(rw);
            sdCalc.run();

            E.info("total number of particles at the end: " + sdCalc.getParticleCount());

            E.info("should have written " + outputFile);
        }
    }


    private static void dump(SDRun sdr) {
        String srw = XMLWriter.serialize(sdr);
        E.info("after rewrite: \n" + srw);
    }

}
