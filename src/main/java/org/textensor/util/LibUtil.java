package org.textensor.util;

import java.util.Arrays;
import java.io.File;
import java.lang.reflect.Field;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class LibUtil {
    static final Logger log = LogManager.getLogger(LibUtil.class);

    /**
     * Add a path to java.library.path
     */
    static public void addLibraryPaths(String... paths) {
        String var = System.getProperty("java.library.path");

        for(String path: paths) {
            if (Arrays.asList(var.split(File.pathSeparator)).contains(path))
                continue;

            var = var.isEmpty() ? path : var + File.pathSeparator + path;

            log.debug("Added {} to java.library.path", path);
        }

        System.setProperty("java.library.path", var);

        /*
         * http://blog.cedarsoft.com/2010/11/setting-java-library-path-programmatically/
         */
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch(Exception e) {
            log.warn("Failed to do the magic thing to sys_paths");
        }
    }
}
