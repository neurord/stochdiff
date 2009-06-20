package org.catacomb.interlish.service;




public class Env {


    public final static int APPLICATION = 0;
    public final static int APPLET = 1;

    public static int context = APPLICATION;



    public static void setContext(int ic) {
        context = ic;
    }


    public static void setContextApplet() {
        context = APPLET;
    }


    public static boolean isApplet() {
        return (context == APPLET);
    }

    public static boolean isApplication() {
        return (context == APPLICATION);
    }



}
