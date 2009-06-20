package org.catacomb.interlish.report;

import org.catacomb.interlish.structure.QuietWriter;

import java.io.StringWriter;



public class StringQuietWriter implements QuietWriter {


    StringWriter sw;

    public StringQuietWriter() {
        sw = new StringWriter();
    }


    public void write(String s) {
        sw.write(s);
    }



    public String getString() {
        return sw.toString();
    }


}
