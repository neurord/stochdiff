package org.catacomb.util;


import java.text.SimpleDateFormat;
import java.util.Date;


public class Timestamp {



    public static String timestamp() {
        SimpleDateFormat f;
        f = new SimpleDateFormat("HH:mm:ss  EEE d MMM yyyy");
        return f.format(new Date());
    }



    public static String withinSessionTimestamp() {
        SimpleDateFormat f;
        f = new SimpleDateFormat("HH:mm:ss");
        return f.format(new Date());
    }


    public static String dayTimestamp() {
        SimpleDateFormat f;
        f = new SimpleDateFormat("d MMM yyyy");
        return f.format(new Date());
    }

}
