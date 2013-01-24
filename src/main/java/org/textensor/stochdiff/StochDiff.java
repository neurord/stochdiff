package org.textensor.stochdiff;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.ModelReader;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.xml.XMLWriter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class StochDiff {
    static final Logger log = LogManager.getLogger("stochdiff");

    // The main method - a bit of basic checking and if all is well, create the
    // SDCalc object and run it;

    public static void help_exit(boolean error) {
        String msg = "Usage: org.textensor.stochdiff.StochDiff modelFile [outputFile]\n"
            + " where the modelFile is an XML specification of the model to run. \n "
            + "The optional outputFile specifies where the results should be stored. If it is \n"
            + "not supplied, they are written to modelFile.out";
        if (error) {
            System.err.println(msg);
            System.exit(1);
        } else {
            System.out.println(msg);
            System.exit(0);
        }
    }

    public static void main(String[] argv) {
        File modelFile = null;
        File outputFile = null;

        List<String> args = Arrays.asList(argv);
        boolean help_requested = args.contains("-h") || args.contains("--help");
        if (help_requested || argv.length == 0)
            help_exit(!help_requested);

        modelFile = new File(argv[0]);
        if (!modelFile.exists()) {
            log.fatal("no such file: {}", modelFile);
            System.exit(2);
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

        SDRun sdModel = ModelReader.read(modelFile);
        sdModel.resolve();

        SDCalc sdCalc = new SDCalc(sdModel, outputFile);

        int runret = sdCalc.run();

        if (runret == 0) {
            E.info("total number of particles at the end: " + sdCalc.getParticleCount());
            E.info("should have written " + outputFile);
        }
    }


    private static void dump(SDRun sdr) {
        String srw = XMLWriter.serialize(sdr);
        E.info("after rewrite: \n" + srw);
    }

}
