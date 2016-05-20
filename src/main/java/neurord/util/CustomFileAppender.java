package neurord.util;

import java.io.Serializable;

import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import java.util.List;
import java.util.ArrayList;

@Plugin(name="CustomFile", category="core", elementType="appender", printObject=true)
public final class CustomFileAppender extends AbstractAppender {
    public static final Integer dummy = 0;

    final static boolean mmap_appender = Settings.getProperty("neurord.mmap_appender",
                                                              "User MemoryMappedFileAppender",
                                                              true);

    final private List<AbstractOutputStreamAppender<? extends OutputStreamManager>> appenders
        = new ArrayList<>();

    static private CustomFileAppender instance = null;

    private CustomFileAppender(String name, Filter filter, Layout layout,
                               boolean handleException) {
        super(name, filter, layout, handleException);
    }

    public void append(LogEvent e) {
        for (Appender appender: this.appenders)
            appender.append(e);
    }

    @PluginFactory
    public static CustomFileAppender createAppender
        (@PluginAttribute(value="fileName")
         String fileName,
         @PluginAttribute(value="append")
         String append,
         @PluginAttribute(value="locking")
         String locking,
         @PluginAttribute(value="name")
         String name,
         @PluginAttribute(value="immediateFlush")
         String immediateFlush,
         @PluginAttribute(value="ignoreExceptions")
         String ignore,
         @PluginAttribute(value="bufferedIo")
         String bufferedIo,
         @PluginAttribute(value="bufferSize")
         String bufferSizeStr,
         @PluginElement(value="Layout")
         Layout<? extends Serializable> layout,
         @PluginElement(value="Filter")
         Filter filter,
         @PluginAttribute(value="advertise")
         String advertise,
         @PluginAttribute(value="advertiseUri")
         String advertiseUri,
         @PluginConfiguration
         Configuration config) {

        if (name == null) {
            LOGGER.error("No name provided for CustomFileAppender");
            return null;
        }

        final CustomFileAppender instance = new CustomFileAppender(name, filter, layout, false);

        if (CustomFileAppender.instance != null) {
            LOGGER.error("No support for multiple CustomFileAppenders");
            return null;
        }

        CustomFileAppender.instance = instance;
        return instance;
    }

    public static void addFileAppender(String filename) {
        if (instance == null) {
            LOGGER.error("CustomFileAppender hasn't been initalized, ignoring output "
                         + filename);
            return;
        }

        final AbstractOutputStreamAppender<? extends OutputStreamManager> appender;
        if (mmap_appender)
            appender = MemoryMappedFileAppender.createAppender(filename,
                                                               "false", filename,
                                                               "false", "8192", "false",
                                                               instance.getLayout(),
                                                               instance.getFilter(),
                                                               "false", "false",
                                                               new DefaultConfiguration());
        else
            appender = FileAppender.createAppender(filename,
                                                   "false", "false",
                                                   filename,
                                                   "false", "false",
                                                   "true", "8192",
                                                   instance.getLayout(),
                                                   instance.getFilter(),
                                                   "false", "false",
                                                   new DefaultConfiguration());

        LOGGER.info("registering custom logfile '{}'", appender);
        instance.appenders.add(appender);
    }

    public static void close() {
        if (instance == null)
            return;

        for (Appender appender: instance.appenders)
            appender.stop();
    }
}
