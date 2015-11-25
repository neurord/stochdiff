package neurord.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.jar.Manifest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class Settings {
    static final Logger log = LogManager.getLogger();

    static public int getProperty(String name, int fallback) {
        String val = System.getProperty(name);
        if (val != null) {
            int ret = Integer.valueOf(val);
            log.debug("Overriding {}: {} → {}", name, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public boolean getProperty(String name, boolean fallback) {
        String val = System.getProperty(name);
        if (val != null) {
            boolean ret = Boolean.valueOf(val);
            log.debug("Overriding {}: {} → {}", name, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public String getProperty(String name, String fallback) {
        String val = System.getProperty(name);
        if (val != null) {
            log.debug("Overriding {}: {} → {}", name, fallback, val);
            return val;
        } else
            return fallback;
    }

    static public String[] getPropertyList(String name, String... fallback) {
        String val = System.getProperty(name);
        if (val == null)
            return fallback;

        String[] ret = val.split(",");
        if (ret.length == 1 && ret[0].equals(""))
            ret = new String[0];

        log.debug("Overriding {}: {} → {}", name, fallback, ret);
        return ret;
    }

    public static Manifest getManifest() throws IOException {
        InputStream stream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
        return new Manifest(stream);
    }

    public static String getProgramVersion() {
        Manifest manifest;
        String value = null;
        try {
            manifest = getManifest();
            value = manifest.getMainAttributes().getValue("git-version");
        } catch(IOException e) {
        }

        return "NeuroRD " + (value != null ? value : "(unknown version)");
    }
}
