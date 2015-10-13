package org.textensor.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.jar.Manifest;

public abstract class Settings {
    static public int getProperty(String name, int fallback) {
        String val = System.getProperty(name);
        if (val != null)
            return Integer.valueOf(val);
        else
            return fallback;
    }

    static public boolean getProperty(String name, boolean fallback) {
        String val = System.getProperty(name);
        if (val != null)
            return Boolean.valueOf(val);
        else
            return fallback;
    }

    static public String getProperty(String name, String fallback) {
        String val = System.getProperty(name);
        if (val != null)
            return val;
        else
            return fallback;
    }

    static public String[] getPropertyList(String name, String... fallback) {
        String val = System.getProperty(name);
        if (val == null)
            return fallback;

        String[] spl = val.split(",");
        if (spl.length == 1 && spl[0].equals(""))
            return new String[0];
        else
            return spl;
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
