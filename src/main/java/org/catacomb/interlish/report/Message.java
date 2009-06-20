package org.catacomb.interlish.report;

import java.text.SimpleDateFormat;
import java.util.Date;



public class Message {


    String summary;
    String[] body;
    String timestamp;



    public Message(String s) {
        this(s, null);
    }

    public Message(String sum, String[] bdy) {
        summary = sum;
        body = bdy;
        timestamp = makeTimestamp();
    }


    public String getText() {
        return summary;
    }


    public String toString() {
        return summary;
    }


    public boolean sameSubject(Message msg) {
        return summary.equals(msg.getSummary());
    }

    public String getSummary() {
        return summary;
    }


    public String[] getBody() {
        return body;
    }




    public static String makeTimestamp() {
        SimpleDateFormat f;
        f = new SimpleDateFormat("HH:mm  d MMM yyyy");
        return f.format(new Date());
    }



}
