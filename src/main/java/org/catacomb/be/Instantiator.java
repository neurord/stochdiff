package org.catacomb.be;

import java.lang.reflect.InvocationTargetException;


public interface Instantiator {

    Object newInstance(String s) throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        NoSuchMethodException;

    Object newInstance(String s, Object arg) throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        NoSuchMethodException,
        IllegalArgumentException,
        InvocationTargetException;

    Class<?> forName(String scl);

    Class<?> forName(String scl, boolean require);

    ClassLoader getLoader();

    void reset();

}
