package org.textensor.util;

public abstract class Settings {
    static public int getProperty(String name, int fallback) {
        String val = System.getProperty(name);
        if (val != null)
            return Integer.valueOf(val);
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
        if (val != null)
            return val.split(",");
        else
            return fallback;
    }
}
