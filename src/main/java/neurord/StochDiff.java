/**
 * NeuroRD — Stochastic reaction-diffusion simulator
 *
 * This software is licensed under the GNU Publice License, version 2
 * or any later, at your option.
 */

package neurord;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import neurord.model.SDRun;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.appender.ConsoleAppender;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;

import neurord.util.CustomFileAppender;
import neurord.util.Settings;

public class StochDiff {
    static final Logger log = LogManager.getLogger();

    /* Like INFO, but we want to show it by default. */
    public static final Level NOTICE = Level.forName("NOTICE", 350);

    // The main method - a bit of basic checking and if all is well, create the
    // SDCalc object and run it;

    public static void help_exit(Options options, boolean error) {
        String header =
            "\nwhere the <model> is an XML specification of the model to run. "
            + "The optional <output> specifies where the results should be stored "
            + "(w/o extension). When not supplied, <output> defaults to <model> "
            + "without the extension.\n\n";

        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(error ? System.err : System.out);
        int columns =
            Math.max(Math.min(Settings.getEnvironmentVariable("COLUMNS", 80),
                              120),
                     20);
        formatter.printHelp(pw,
                            columns,
                            "neurord.StochDiff <model> [<output>]",
                            header,
                            options,
                            HelpFormatter.DEFAULT_LEFT_PAD,
                            HelpFormatter.DEFAULT_DESC_PAD,
                            "");
        pw.flush();
        System.exit(error ? 1 : 0);
    }

    public static boolean setLogLevel(LoggerContext context, String logger, Level level) {
        final LoggerContext ctx;
        if (context != null)
            ctx = context;
        else
            ctx = (LoggerContext) LogManager.getContext(false);

        final Configuration config = ctx.getConfiguration();

        if (!logger.equals(LogManager.ROOT_LOGGER_NAME))
            try {
                Class.forName(logger);
            } catch(ClassNotFoundException e) {
                log.warn("Failed to find logger \"{}\": {}", logger, e);
                return false;
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

        /* Tell logger to refetch configuration */
        if (context == null)
            ctx.updateLoggers();
        return true;
    }

    /**
     * Set log4j2 log levels based on properties:
     * log.&lt;logger-name&gt;=info|debug|warning|...
     */
    public static void setLogLevels() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
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

                if (setLogLevel(ctx, logger, level))
                    any = true;
            }

        if (any)
            /* This causes all Loggers to refetch information from their LoggerConfig. */
            ctx.updateLoggers();
    }

    static Options buildOptions() {
        Options options = new Options();

        options.addOption(null, "version", false, "print version string and exit");

        options.addOption("i", "ic", true, "output file to take the initial conditions from");
        options.addOption(null, "ic-trial", true, "trial to take the seed from (default: 0)");
        options.addOption(null, "ic-time", true, "time to take the ICs from (default: none)");

        options.addOption(null, "log", true, "log file name (\"no\" to disable)");
        options.addOption("v", "verbose", false, "increase log level");

        return options;
    }

    public static void main(String[] argv) throws Exception {
        File modelFile;
        final File outputFile;

        CommandLineParser parser = new DefaultParser();
        Options options = buildOptions();

        List<String> args = Arrays.asList(argv);
        boolean help_requested = args.contains("-h") || args.contains("--help");
        if (help_requested || argv.length == 0)
            help_exit(options, !help_requested);

        CommandLine cmd = parser.parse(options, argv);
        if (cmd.hasOption("version")) {
            System.out.println(Settings.getProgramVersion());
            System.exit(0);
        }
        if (cmd.hasOption("verbose")) {
            int count = 0;
            for (String arg: argv)
                if (arg.equals("-v") || arg.equals("--verbose"))
                    count ++;
            assert count > 0: count;

            Level level = count == 1 ? Level.INFO : Level.DEBUG;
            setLogLevel(null, LogManager.ROOT_LOGGER_NAME, level);
        }

        argv = cmd.getArgs();
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

        if (System.console() != null) {
            Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
            org.apache.logging.log4j.core.Logger coreLogger
                = (org.apache.logging.log4j.core.Logger) logger;
            LoggerContext context = (LoggerContext) coreLogger.getContext();
            Configuration configuration = context.getConfiguration();
            coreLogger.addAppender(configuration.getAppender("Console"));
        }

        final String logfile = cmd.getOptionValue("log", outputFile + ".log");
        boolean log_to_file = !logfile.equals("no");
        if (log_to_file)
            CustomFileAppender.addFileAppender(logfile);

        /* Write out the version, after creating the log appenders. */
        log.info("{}", Settings.getProgramVersion());

        if (log_to_file)
            log.info("Writing logs to {}", logfile);

        final File ic_file = Settings.getOption(cmd, "ic", null);
        final int ic_trial = Settings.getOption(cmd, "ic-trial", 0);
        final double ic_time = Settings.getOption(cmd, "ic-time", Double.NaN);

        final SDRun model = SDRun.loadFromFile(modelFile, ic_file, ic_trial, ic_time);

        SDCalc calc = new SDCalc(model, outputFile);
        calc.run();
    }
}
