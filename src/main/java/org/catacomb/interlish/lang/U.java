package org.catacomb.interlish.lang;


public class U {



    public static boolean isBlank(String s) {
        return (s == null || s.length() == 0);
    }

    public static boolean eitherIsBlank(String s1, String s2) {
        return (isBlank(s1) || isBlank(s2));
    }


    public static boolean differ(Object s1, Object s2) {
        return !same(s1, s2);
    }

    public static boolean same(Object s1, Object s2) {
        return ((s1 == null && s2 == null) || s1.equals(s2));
    }



}
