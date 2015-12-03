package neurord;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import neurord.xml.ModelReader;
import neurord.model.SDRun;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import neurord.util.CustomFileAppender;
import neurord.util.Settings;

public class StochDiff {
    static final Logger log = LogManager.getLogger();

    final static boolean log_to_file = Settings.getProperty("neurord.log", true);

    final static int source_trial = Settings.getProperty("neurord.source_trial", 0);
    final static double source_time = Settings.getProperty("neurord.source_time", Double.NaN);

    static final ModelReader<SDRun> loader = new ModelReader(SDRun.class);

    // The main method - a bit of basic checking and if all is well, create the
    // SDCalc object and run it;

    public static void help_exit(boolean error) {
        String msg = "Usage: neurord.StochDiff <model> [<output>]\n"
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
        boolean any = false;

        Properties props = System.getProperties();
        for (String key : props .stringPropertyNames())
            if (key.startsWith("log.")) {
                String logger = key.substring(4);
                String value = props.getProperty(key);
                Level level = Level.getLevel(value.toUpperCase());
                if (level == null) {
                    log.warn("Unrecognized level \"{}\"", value);
                    continue;
                }

                try {
                    Class<?> cls = Class.forName(logger);
                } catch(ClassNotFoundException e) {
                    log.warn("Failed to find logger \"{}\": {}", logger, e);
                    continue;
                }

                LoggerConfig loggerConfig = config.getLoggerConfig(logger);
                if (loggerConfig.getName().equals(logger)) {
                    loggerConfig.setLevel(level);
                    log.debug("Setting logger level {}={}", logger, level);
                } else {
                    log.debug("Creating logger {}={}", logger, level);
                    loggerConfig = new LoggerConfig(logger, level, false);
                    config.addLogger(logger, loggerConfig);
                }

                any = true;
            }

        if (any)
            /* This causes all Loggers to refetch information from their LoggerConfig. */
            ctx.updateLoggers();
    }

    public static void main(String[] argv) throws Exception {
        File modelFile;
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

        setLogLevels();

        if (log_to_file)
            CustomFileAppender.addFileAppender(logfile);

        /* Write out the version, after opening the log file. */
        log.info("{}", Settings.getProgramVersion());

        if (log_to_file)
            log.info("Writing logs to {}", logfile);

        final SDRun model;
        if (modelFile.toString().endsWith(".h5"))
            model = SDRun.loadFromFile(modelFile, source_trial, source_time);
        else
            model = loader.unmarshall(modelFile, null);

        SDCalc calc = new SDCalc(model, outputFile);
        calc.run();
    }
}
