package org.textensor.stochdiff;

import java.io.File;

import org.catacomb.util.FileUtil;
import org.textensor.report.E;

import java.io.*;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.textensor.util.inst;

public class ResultWriter {

    File outputFile;

    OutputStreamWriter writer;
    String fnroot = "";

    boolean ready;
    boolean closed = false;
    boolean continuation = false;

    final protected HashMap<String, ResultWriter> siblings = inst.newHashMap();

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

    public boolean isContinuation() {
        return continuation && outputFile.exists();
    }

    public void init(String magic) {
        try {
            if (isContinuation()) {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile, true));

            } else {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile));
                if (magic != null) {
                    writer.write(magic + "\n");
                }
            }
            ready = true;
        } catch (Exception ex) {
            E.error("cannot create file writer " + ex);
        }
    }

    public void writeString(String sdat) {
        if (ready) {
            try {
                writer.write(sdat, 0, sdat.length());
            } catch (Exception ex) {
                E.error("cannot write: " + ex);
            }
        }
    }

    public void close() {
        if (!closed) {

            if (ready) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    E.error("ex " + ex);
                }
            } else {
                E.error("data not written (earlier errors)");
            }

            for (ResultWriter rw : siblings.values())
                rw.close();

            closed = true;
        }
    }

    public ResultWriter getSibling(String extn, String magic) {
        ResultWriter ret = getRawSibling(extn);
        ret.init(magic);
        return ret;
    }

    public ResultWriter getRawSibling(String extn) {
        ResultWriter ret = siblings.get(extn);

        if (ret == null) {
            String fnm = fnroot + extn;
            File f = new File(outputFile.getParentFile(), fnm);
            ret = new ResultWriter(f);
            siblings.put(extn, ret);
        }

        return ret;
    }



    public File getSiblingFile(String extn) {
        ResultWriter rw = getSibling(extn, null);
        return rw.getFile();
    }


    private File getFile() {
        return outputFile;
    }

    public void writeToSiblingFileAndClose(String txt, String extn) {
        ResultWriter rw = getSibling(extn, null);
        rw.writeString(txt);
        rw.close();
    }

    public void writeToSiblingFile(String txt, String extn) {
        writeToSiblingFile(txt, extn, null);
    }

    public void writeToFinalSiblingFile(String txt, String extn) {
        writeToFinalSiblingFile(txt, extn, null);
    }

    public void writeToSiblingFile(String txt, String extn, String magic) {
        ResultWriter rw = getSibling(extn, magic);
        rw.writeString(txt);
    }

    public void writeToFinalSiblingFile(String txt, String extn, String magic) {
        ResultWriter rw = getSibling(extn, magic);
        rw.writeString(txt);
        rw.close();
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

    /**
     * this expects each record to begin with max, and then have a time
     * field as the idx'th element of the line. It keeps records as long
     * as their time is &lt; value but discards the rest
     */
    public void pruneFrom(String match, int idx, double value) {
        continuation = true;
        File fcopy = outputFile.getAbsoluteFile();
        File fwk = new File(fcopy.getParentFile(), fcopy.getName()+".wk");

        fcopy.renameTo(fwk);
        try {
            BufferedReader br = new BufferedReader(new FileReader(fwk));
            BufferedWriter bw = new BufferedWriter(new FileWriter(fcopy));

            while (br.ready()) {
                String sl = br.readLine();
                if (match == null || match.length() == 0 || sl.startsWith(match)) {
                    StringTokenizer st = new StringTokenizer(sl, " ");
                    if (st.countTokens() > idx) {
                        String stok = "";
                        for (int i = 0; i <= idx; i++) {
                            stok = st.nextToken();
                        }
                        try {
                            double d = Double.parseDouble(stok);
                            if (d >= value - 1.e-9) {
                                break;
                            }
                        } catch (NumberFormatException ex) {
                            // its ok - probably a header rather than a number
                        }
                    }
                }
                bw.write(sl);
                bw.write("\n");
            }

            bw.close();
            br.close();

            fwk.delete();

        } catch (Exception ex) {
            E.error("cannot prune data file: " + ex);
        }
    }

}
