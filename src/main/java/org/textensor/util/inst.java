package org.textensor.util;

import java.util.Arrays;
import java.util.Vector;

public abstract class inst {
    public static <T> Vector<T> newVector(T... items) {
        return new Vector<>(Arrays.asList(items));
    }
}
