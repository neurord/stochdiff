package org.textensor.stochdiff;

import java.io.File;

import org.textensor.report.E;

import java.io.*;

public class ResultWriter {

    File outputFile;

    public final static int TEXT = 1;
    public final static int BINARY = 2;

    OutputStreamWriter writer;

    boolean ready;


    public ResultWriter(File outFile) {
        outputFile = outFile;
        ready = false;
    }


    public void init(String magic, int type) {
        if (type == TEXT) {
            try {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile));
                writer.write(magic + "\n");
                ready = true;
            } catch (Exception ex) {
                E.error("cant create file writer " + ex);
            }
        } else {
            E.error("binary not handled yet...");
        }

    }

    public void writeString(String sdat) {
        if (ready) {
            try {
                writer.write(sdat, 0, sdat.length());
            } catch (Exception ex) {
                E.error("cant write: " + ex);
            }
        }
    }

    public void close() {
        if (ready) {
            try {
                writer.close();
            } catch (Exception ex) {
                E.error("ex " + ex);
            }
        } else {
            E.error("data not written (earlier errors)");
        }

    }

}
