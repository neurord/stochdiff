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

import org.textensor.util.CustomFileAppender;

public class StochDiff {
    static final Logger log = LogManager.getLogger("stochdiff");

    // The main method - a bit of basic checking and if all is well, create the
    // SDCalc object and run it;

    public static void help_exit(boolean error) {
        String msg = "Usage: org.textensor.stochdiff.StochDiff <model> [<output>]\n"
            + " where the <model> is an XML specification of the model to run. \n "
            + "The optional <output> specifies where the results should be stored (w/o extension).\n"
            + "If it is not supplied, they are written to <model> but with .out extension.";
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
        final File outputFile;

        List<String> args = Arrays.asList(argv);
        boolean help_requested = args.contains("-h") || args.contains("--help");
        if (help_requested || argv.length == 0)
            help_exit(!help_requested);

        modelFile = new File(argv[0]);
        if (!modelFile.exists()) {
            log.fatal("no such file: {}", modelFile);
            System.exit(2);
        }

        if (argv.length > 1)
            outputFile = new File(argv[1]);
        else {
            String s = argv[0];
            if (s.indexOf(".") > 0)
                s = s.substring(0, s.lastIndexOf("."));
            outputFile = new File(s);
        }

        final String logfile = outputFile + ".log";
        CustomFileAppender.addFileAppender(logfile);

        SDRun sdModel = ModelReader.read(modelFile);
        sdModel.resolve();

        SDCalc sdCalc = new SDCalc(sdModel, outputFile);
        sdCalc.run();
    }
}
