package org.textensor.stochdiff;

import java.io.File;

import org.catacomb.util.FileUtil;
import org.textensor.report.E;

import java.io.*;

import java.util.HashMap;

public class ResultWriter {

    File outputFile;

    public final static int TEXT = 1;
    public final static int BINARY = 2;

    OutputStreamWriter writer;
    String fnroot = "";

    boolean ready;

    HashMap<String, ResultWriter> siblings;


    public ResultWriter(File outFile) {
        outputFile = outFile;
        ready = false;

        String fnm = outFile.getName();
        int idot = fnm.lastIndexOf(".");
        if (idot > 0) {
            fnroot = fnm.substring(0, idot);
        } else {
            fnroot = fnm;
        }


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
            String fnm = fnroot + extn;
            File f = new File(outputFile.getParentFile(), fnm);
            ret = new ResultWriter(f);
            siblings.put(extn, ret);
            ret.init(magic, type);
        }

        return ret;
    }

    public void writeToSiblingFileAndClose(String txt, String extn) {
        ResultWriter rw = getSibling(TEXT, extn, null);
        rw.writeString(txt);
        rw.close();
    }


    public void writeToSiblingFile(String txt, String extn) {
        writeToSiblingFile(txt, extn, null);
    }

    public void writeToSiblingFile(String txt, String extn, String magic) {
        ResultWriter rw = getSibling(TEXT, extn, magic);
        rw.writeString(txt);
    }


    public String readSibling(String fnm) {
        String ret = null;
        File fin = new File(outputFile.getParentFile(), fnm);
        if (fin.exists()) {
            ret = FileUtil.readStringFromFile(fin);
        } else {
            E.error("No such file " + fin.getAbsolutePath());
        }
        return ret;
    }

}
