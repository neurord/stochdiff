package org.textensor.util;

public abstract class Settings {
    static public int getProperty(String name, int fallback) {
        String val = System.getProperty(name);
        if (name != null)
            return Integer.valueOf(val);
        else
            return fallback;
    }
}
