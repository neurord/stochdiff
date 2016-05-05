package neurord.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.Manifest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.commons.cli.CommandLine;

public class Settings {
    static final Logger log = LogManager.getLogger();

    final String name;
    final String description;
    final Object fallback;

    public static ArrayList<Settings> all_settings = new ArrayList<>();

    private Settings(String name, String description, Object fallback) {
        this.name = name;
        this.description = description;
        this.fallback = fallback;

        all_settings.add(this);
    }

    static public int getProperty(String name, String description, int fallback) {
        new Settings(name, description, fallback);

        String val = System.getProperty(name);
        if (val != null) {
            int ret = Integer.valueOf(val);
            log.debug("Overriding {}: {} → {}", name, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public boolean getProperty(String name, String description, boolean fallback) {
        new Settings(name, description, fallback);

        String val = System.getProperty(name);
        if (val != null) {
            boolean ret = Boolean.valueOf(val);
            log.debug("Overriding {}: {} → {}", name, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public double getProperty(String name, String description, double fallback) {
        new Settings(name, description, fallback);

        String val = System.getProperty(name);
        if (val != null) {
            double ret = Double.valueOf(val);
            log.debug("Overriding {}: {} → {}", name, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public String getProperty(String name, String description, String fallback) {
        new Settings(name, description, fallback);

        String val = System.getProperty(name);
        if (val != null) {
            log.debug("Overriding {}: {} → {}", name, fallback, val);
            return val;
        } else
            return fallback;
    }

    static public String[] getPropertyList(String name, String description, String... fallback) {
        new Settings(name, description, fallback);

        String val = System.getProperty(name);
        if (val == null)
            return fallback;

        String[] ret = val.split(",");
        if (ret.length == 1 && ret[0].equals(""))
            ret = new String[0];

        log.debug("Overriding {}: {} → {}", name, fallback, ret);
        return ret;
    }

    static void forceLoading() {
        Logger log;
        log = neurord.SDCalc.log;
        log = neurord.numeric.grid.AdaptiveGridCalc.log;
        log = neurord.numeric.grid.GridCalc.log;
        log = neurord.numeric.grid.NextEventQueue.log;
        log = neurord.numeric.grid.ResultWriterHDF5.log;
        log = neurord.numeric.grid.StochasticGridCalc.log;
        log = neurord.xml.ModelReader.log;
    }

    static public String stringify(Object x) {
        if (x instanceof String[])
            return Arrays.toString((String[]) x);
        else
            return x.toString();
    }

    static public void printAvailableSettings(PrintStream out) {
        forceLoading();

        out.println("Recognized properties:");
        for (Settings s: all_settings)
            out.println(String.format("%s (default: %s)\t%s",
                                      s.name,
                                      stringify(s.fallback),
                                      s.description));
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

    static public int getEnvironmentVariable(String name, int fallback) {
        String val = System.getenv(name);
        if (val != null) {
            int ret = Integer.valueOf(val);
            return ret;
        } else
            return fallback;
    }

    static public int getOption(CommandLine cmd, String name, int fallback) {
        String val = cmd.getOptionValue(name);
        if (val != null) {
            int ret = Integer.valueOf(val);
            return ret;
        } else
            return fallback;
    }

    static public boolean getOption(CommandLine cmd, String name, boolean fallback) {
        String val = cmd.getOptionValue(name);
        if (val != null) {
            boolean ret = Boolean.valueOf(val);
            return ret;
        } else
            return fallback;
    }

    static public double getOption(CommandLine cmd, String name, double fallback) {
        String val = cmd.getOptionValue(name);
        if (val != null) {
            double ret = Double.valueOf(val);
            return ret;
        } else
            return fallback;
    }

    static public File getOption(CommandLine cmd, String name, File fallback) {
        String val = cmd.getOptionValue(name);
        if (val != null)
            return new File(val);
        else
            return fallback;
    }
}
