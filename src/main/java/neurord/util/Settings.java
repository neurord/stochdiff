package neurord.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
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

    static public String getProperty(String name) {
        return getProperties().getProperty(name);
    }

    static public int getProperty(String name, String description, int fallback) {
        new Settings(name, description, fallback);

        String val = getProperty(name);
        if (val != null) {
            int ret = Integer.valueOf(val);
            log.debug("Overriding {} with \"{}\": {} → {}", name, val, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public boolean getProperty(String name, String description, boolean fallback) {
        new Settings(name, description, fallback);

        String val = getProperty(name);
        if (val != null) {
            boolean ret = parseBool(val);
            log.debug("Overriding {} with \"{}\": {} → {}", name, val, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public double getProperty(String name, String description, double fallback) {
        new Settings(name, description, fallback);

        String val = getProperty(name);
        if (val != null) {
            double ret = Double.valueOf(val);
            log.debug("Overriding {} with \"{}\": {} → {}", name, val, fallback, ret);
            return ret;
        } else
            return fallback;
    }

    static public String getProperty(String name, String description, String fallback) {
        new Settings(name, description, fallback);

        String val = getProperty(name);
        if (val != null) {
            log.debug("Overriding {}: {} → {}", name, fallback, val);
            return val;
        } else
            return fallback;
    }

    static public String[] getPropertyList(String name, String description, String... fallback) {
        new Settings(name, description, fallback);

        String val = getProperty(name);
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
        log = neurord.model.Specie.log;
        log = neurord.model.Reaction.log;
        log = neurord.model.InjectionStim.log;

        Integer dummy = neurord.util.CustomFileAppender.dummy;
    }

    static public String stringify(Object x) {
        if (x instanceof String[])
            return Arrays.toString((String[]) x);
        else
            return x.toString();
    }

    static String repeat(String s, int n) {
        String ans = "";
        while (n-- > 0)
            ans += s;
        return ans;
    }

    static public void printAvailableSettings(PrintStream out) {
        forceLoading();

        int n = 0;
        for (Settings s: all_settings) {
            String lhs = String.format("%s (default: %s)",
                                       s.name,
                                       stringify(s.fallback));
            n = Math.max(n, lhs.length());
        }

        out.println("Recognized properties (use as -D<property>=<value>):");
        for (Settings s: all_settings) {
            String lhs = String.format("%s (default: %s)",
                                       s.name,
                                       stringify(s.fallback));
            out.println(String.format("%s%s %s",
                                      lhs,
                                      repeat(" ", n - lhs.length()),
                                      s.description));
        }

        out.println();
        out.println("Manifest main attributes:");
        try {
            Manifest manifest = getManifest();
            for (Object key: manifest.getMainAttributes().keySet())
                out.println("" + key + " = " + manifest.getMainAttributes().get(key));
        } catch(IOException e) {}
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

    static public int optionCount(CommandLine cmd, String argv[], String longopt, String shortopt) {
        int count = 0;
        if (cmd.hasOption(longopt)) {
            for (String arg: argv)
                if (arg.equals("--" + longopt) || arg.equals("-" + shortopt))
                    count ++;
            assert count > 0: count;
        }
        return count;
    }

    public static final String FALLBACK = "java -jar neurord.jar";
    static public String javaExecutable(Class cls) {
        String path;

        /* Allow overriding, which is useful when a wrapper script is provided.
         * Use Systemd.getProperty because this is supposed to be overriden
         * from the outside, too early to parse options. */
        path = System.getProperty("neurord.executable_name");
        if (path != null)
            return path;

        try {
            path = cls.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch(java.net.URISyntaxException e) {
            return FALLBACK;
        }

        if (path == null)
            return FALLBACK;

        if (path.endsWith(".jar"))
            return "java -jar " + path;
        else
            return "java " + cls.getName();
    }

    public static void augmentProperties(Properties overrides) {
        properties = (Properties) System.getProperties().clone();
        properties.putAll(overrides);
    }

    private static Properties properties = null;
    public static Properties getProperties() {
        if (properties == null)
            return System.getProperties();
        return properties;
    }

    private static boolean parseBool(String value) {
        switch(value.toLowerCase()) {
        case "1":
        case "yes":
        case "true":
            return true;
        case "0":
        case "no":
        case "false":
            return false;
        }
        throw new RuntimeException("Cannot parse boolean \"" + value + "\"");
    }
}
