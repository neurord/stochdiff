package neurord.util;

import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.appender.ConsoleAppender;

import neurord.util.CustomFileAppender;

public abstract class Logging {
    static final Logger log = LogManager.getLogger();

    /* Like INFO, but we want to show it by default. */
    public static final Level NOTICE = Level.forName("NOTICE", 350);

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

    public static void configureLogging(String logfile) {
        if (System.console() != null) {
            Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
            org.apache.logging.log4j.core.Logger coreLogger
                = (org.apache.logging.log4j.core.Logger) logger;
            LoggerContext context = (LoggerContext) coreLogger.getContext();
            Configuration configuration = context.getConfiguration();
            coreLogger.addAppender(configuration.getAppender("Console"));
        }

        if (!logfile.equals("no"))
            CustomFileAppender.addFileAppender(logfile);

        setLogLevels();
    }
}
