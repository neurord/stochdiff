package org.textensor.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class LibUtil {
    static final Logger log = LogManager.getLogger(LibUtil.class);

    static boolean tryLoad(String filename) {
        log.debug("Trying to load {}", filename);
        try {
            System.load(filename);
            log.info("Successfully loaded {}", filename);
        } catch (Exception e) {
            return false;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }

        return true;
    }

    /**
     * Load a native library
     */
    static public boolean loadLibrary(String name) {
        String os = System.getProperty("os.name");
        if (os.equals("Linux")) {
            // Check for 64-bit library availability
            // prior to 32-bit library availability.
            if (tryLoad("/usr/lib64/" + name + "/lib" + name + ".so"))
                return true;

            if (tryLoad("/usr/lib/" + name + "/lib" + name + ".so"))
                return true;
        } else {
            try {
                System.loadLibrary(name);
                log.info("Successfully loaded {}", name);
                return true;
            } catch (Throwable t) {
                // This is bad news, the program is doomed at this point
                t.printStackTrace();
            }
        }

        log.error("Failed to load lib{}", name);
        return false;
    }
}
