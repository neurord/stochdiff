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
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.LogEvent;

@Plugin(name="CustomFile", type="Core", elementType="appender", printObject=true)
public final class CustomFileAppender extends AbstractAppender {

    final private FileAppender appender;

    private CustomFileAppender(String name, Filter filter, Layout layout,
                               boolean handleException, FileAppender appender) {
        super(name, filter, layout, handleException);
        this.appender = appender;
    }

    public void append(LogEvent e) {
        this.appender.append(e);
    }

    @PluginFactory
    public static CustomFileAppender createAppender
        (@PluginAttr("name") String name,
         @PluginAttr("suppressExceptions") String suppress,
         @PluginElement("layout") Layout layout,
         @PluginElement("filters") Filter filter) {

        boolean handleExceptions = suppress == null ? true : Boolean.valueOf(suppress);

        if (name == null) {
            LOGGER.error("No name provided for CustomFileAppender");
            return null;
        }

        FileAppender appender = FileAppender.createAppender("log.file",
                                                            "false", "false", name,
                                                            "true", "false", "true",
                                                            layout, filter);
        return new CustomFileAppender(name, filter, layout, handleExceptions, appender);
    }
}
