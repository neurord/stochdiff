package org.catacomb.interlish.interact;


public class Clipboard {

    static Object content;
    static String label;
    static String contentType;

    public static void setContent(Object obj, String lab, String typ) {
        content = obj;
        label = lab;
        contentType = typ;

    }


    public static boolean hasContent(String s) {
        return contentType != null && contentType.equals(s);
    }

    public static Object getContent() {
        return content;
    }


    public static String getLabel() {
        return label;
    }

}
