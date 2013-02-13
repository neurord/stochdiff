package org.textensor.util;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttr;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.LogEvent;

import java.util.List;

@Plugin(name="CustomFile", type="Core", elementType="appender", printObject=true)
public final class CustomFileAppender extends AbstractAppender {

    final private List<Appender> appenders = inst.newArrayList();

    static private CustomFileAppender instance = null;
    static public CustomFileAppender getInstance() {
        // TODO: support for multiple instances
        return instance;
    }

    private CustomFileAppender(String name, Filter filter, Layout layout,
                               boolean handleException) {
        super(name, filter, layout, handleException);
    }

    public void append(LogEvent e) {
        for (Appender appender: this.appenders) {
            appender.append(e);
        }
    }

    @PluginFactory
    public static CustomFileAppender createAppender
        (@PluginAttr("name") String name,
         @PluginAttr("suppressExceptions") String suppress,
         @PluginElement("layout") Layout layout,
         @PluginElement("filters") Filter filter) {

        final boolean handleExceptions = suppress == null ? true : Boolean.valueOf(suppress);

        if (name == null) {
            LOGGER.error("No name provided for CustomFileAppender");
            return null;
        }

        final CustomFileAppender instance =
            new CustomFileAppender(name, filter, layout, handleExceptions);

        if (CustomFileAppender.instance != null) {
            LOGGER.error("No support for multiple CustomFileAppenders");
            return null;
        }

        CustomFileAppender.instance = instance;
        return instance;
    }

    public static void addFileAppender(String filename) {
        final CustomFileAppender instance = getInstance();
        if (instance == null) {
            LOGGER.error("CustomFileAppender hasn't been initalized, ignoring output "
                         + filename);
            return;
        }

        final FileAppender appender =
            FileAppender.createAppender(filename, "false", "false", filename,
                                        "true", "false", "true",
                                        instance.getLayout(),
                                        instance.getFilter());
        LOGGER.info("registering custom logfile '{}'", appender);
        instance.appenders.add(appender);
    }
}
