package org.textensor.stochdiff;

import java.io.File;

import org.catacomb.util.FileUtil;
import org.textensor.report.E;

import java.io.*;

import java.util.HashMap;
import java.util.StringTokenizer;

public class ResultWriter {

    File outputFile;

    public final static int TEXT = 1;
    public final static int BINARY = 2;

    OutputStreamWriter writer;
    String fnroot = "";

    boolean ready;
    boolean closed = false;
    boolean continuation = false;

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

    public boolean isContinuation() {
        return continuation && outputFile.exists();
    }

    public void init(String magic, int type) {
        if (type == TEXT) {
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

            if (siblings != null) {
                for (ResultWriter rw : siblings.values()) {
                    rw.close();
                }
            }
            closed = true;
        }
    }

    public ResultWriter getSibling(int type, String extn, String magic) {
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


    public ResultWriter getRawSibling(int type, String extn, String magic) {
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

    public void writeToFinalSiblingFile(String txt, String extn) {
        writeToFinalSiblingFile(txt, extn, null);
    }

    public void writeToSiblingFile(String txt, String extn, String magic) {
        ResultWriter rw = getSibling(TEXT, extn, magic);
        rw.writeString(txt);
    }

    public void writeToFinalSiblingFile(String txt, String extn, String magic) {
        ResultWriter rw = getSibling(TEXT, extn, magic);
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



    public void pruneFrom(String match, int idx, double value) {
        // this expects each record to begin with max, and then have a time field as the idx'th element of the
        // line. It keeps records as long as their time is < value but discards the rest
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
            E.error("cant prune data file: " + ex);
        }
    }







    public void pruneFromCount(int n, double value) {
        // this form expects lines of n items with labels for the first and the time
        // as the first item of the rest
        continuation = true;
        File fcopy = outputFile.getAbsoluteFile();
        File fwk = new File(fcopy.getParentFile(), "tmp.wk");



        E.info("pruning by count " + n + fcopy + " " + fwk);
        if (fwk.exists()) {
            fwk.delete();
        }
        boolean suc = fcopy.renameTo(fwk);

        E.info("rename result: " + suc);

        System.exit(0);

        try {
            Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(fwk)));
            StreamTokenizer st = new StreamTokenizer(r);
            BufferedWriter bw = new BufferedWriter(new FileWriter(fcopy));

            int ntok = st.nextToken();
            int nrec = 0;


            while (true) {
                if (ntok == StreamTokenizer.TT_EOF) {
                    E.info("end of file..");
                }
                if (nrec > 0) {
                    double d = Double.parseDouble(st.sval);
                    if (d > value - 1.e-9) {
                        E.info("finished reading " + d);
                        break;
                    }
                    E.info("continuing - read " + d);
                }

                int nr = 0;
                while (ntok != StreamTokenizer.TT_EOF && nr < n) {
                    bw.write(st.sval);
                    bw.write(" ");
                    ntok = st.nextToken();
                    nr += 1;
                }
                bw.write("\n");
                nrec += 1;
            }

            bw.close();
            r.close();
            E.info("exiting...");

            System.exit(0);
            // fwk.delete();

        } catch (Exception ex) {
            E.error("cant prune data file: " + ex);
        }
    }

}
