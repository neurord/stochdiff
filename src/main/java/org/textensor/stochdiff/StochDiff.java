package org.textensor.stochdiff;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.textensor.xml.ModelReader;
import org.textensor.stochdiff.model.SDRun;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import org.textensor.util.CustomFileAppender;

public class StochDiff {
    static final Logger log = LogManager.getLogger("stochdiff");

    static final ModelReader<SDRun> loader = new ModelReader(SDRun.class);

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

    /**
     * Set log4j2 log levels based on properties:
     * log.&lt;logger-name&gt;=info|debug|warning|...
     */
    public static void setLogLevels() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        Properties props = System.getProperties();
        for (String key : props .stringPropertyNames())
            if (key.startsWith("log.")) {
                String logger = "org.textensor." + key.substring(4);
                String value = props.getProperty(key);
                Level level = Level.getLevel(value.toUpperCase());
                if (level == null) {
                    log.warn("Unrecognized level \"{}\"", value);
                    continue;
                }

                // Force the logger to exist
                LogManager.getLogger(logger);

                LoggerConfig loggerConfig = config.getLoggerConfig(logger);
                if (!loggerConfig.getName().equals(logger)) {
                    log.warn("Failed to find logger \"{}\"", logger);
                    continue;
                }

                log.debug("Logging level {}={}", logger, level);
                loggerConfig.setLevel(level);
            }
    }

    public static void main(String[] argv) throws Exception {
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

        setLogLevels();

        final String logfile = outputFile + ".log";
        CustomFileAppender.addFileAppender(logfile);

        SDRun sdModel = loader.unmarshall(modelFile);

        SDCalc sdCalc = new SDCalc(sdModel, outputFile);
        sdCalc.run();
    }
}
