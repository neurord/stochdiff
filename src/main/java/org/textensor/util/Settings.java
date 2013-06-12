package org.textensor.util;

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
}
