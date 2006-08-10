package org.textensor.stochdiff;

import java.io.File;

import org.textensor.report.E;

import java.io.*;

import java.util.HashMap;

public class ResultWriter {

    File outputFile;

    public final static int TEXT = 1;
    public final static int BINARY = 2;

    OutputStreamWriter writer;

    boolean ready;

    HashMap<String, ResultWriter> siblings;


    public ResultWriter(File outFile) {
        outputFile = outFile;
        ready = false;
    }


    public void init(String magic, int type) {
        if (type == TEXT) {
            try {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile));
                if (magic != null) {
                    writer.write(magic + "\n");
                }
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

        if (siblings != null) {
            for (ResultWriter rw : siblings.values()) {
                rw.close();
            }
        }
    }


    private ResultWriter getSibling(int type, String extn, String magic) {
        ResultWriter ret = null;
        if (siblings == null) {
            siblings = new HashMap<String, ResultWriter>();
        }
        if (siblings.containsKey(extn)) {
            ret = siblings.get(extn);

        } else {
            String fnm = outputFile.getName() + extn;
            File f = new File(outputFile.getParentFile(), fnm);
            ret = new ResultWriter(f);
            siblings.put(extn, ret);
            ret.init(magic, type);
        }

        return ret;
    }



    public void writeToSiblingFile(String txt, String extn) {
        writeToSiblingFile(txt, extn, null);
    }

    public void writeToSiblingFile(String txt, String extn, String magic) {
        ResultWriter rw = getSibling(TEXT, extn, magic);
        rw.writeString(txt);
    }


}
