package org.catacomb.druid.xtext;

import org.catacomb.druid.xtext.base.Guise;


public class StandardGuises {


    static Guise defaultGuise;


    static {
        defaultGuise = new Guise();
    }



    public static Guise getDefault() {
        return defaultGuise;
    }


    public static Guise getParagraph() {
        Guise g = new Guise();
        return g;
    }



    public static Guise getSentence() {
        Guise g = new Guise();
        return g;

    }



    public static Guise getWord() {
        Guise g = new Guise();
        return g;
    }



    public static Guise getDocument() {
        Guise g = new Guise();
        return g;
    }



}
