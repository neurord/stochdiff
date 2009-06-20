package org.catacomb.util;



import org.catacomb.interlish.structure.ProgressReport;
import org.catacomb.report.E;

import java.io.*;
import java.net.URL;



public class NetUtil {



    public static byte[] readHeader(URL url, int n) {
        byte[] ret = null;
        try {
            InputStream ins = url.openStream();
            ret = new byte[n];
            int nread = ins.read(ret);
            if (nread != n) {
                E.error("readNBytes wanted " + n + " but got " + nread);
            }
            ins.close();
        } catch (Exception ex) {
            E.error("readNBytes problem " + ex);
        }
        return ret;
    }



    public static byte[] readBytes(URL u) {
        byte[] ret = null;
        try {
            InputStream ins = u.openStream();
            BufferedInputStream bis = new BufferedInputStream(ins);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] bb = new byte[4096];
            int nread = bis.read(bb);
            while (nread > 0) {
                baos.write(bb, 0, nread);
                nread = bis.read(bb);
            }
            ret = baos.toByteArray();
            ins.close();

        } catch (Exception ex) {
            E.error("readNBytes problem " + ex);
        }
        return ret;
    }



    public static String readStringFromURL(URL url) {
        return readStringFromURL(url, null);
    }


    public static String readStringFromURL(String surl, ProgressReport report) {
        String ret = null;
        try {
            URL url = new URL(surl);
            ret = readStringFromURL(url, report);
        } catch (Exception ex) {
            E.error("url conversion failed for " + surl + " " + ex);
        }
        return ret;
    }



    public static String readStringFromURL(URL url, ProgressReport report) {

        StringBuffer sb = new StringBuffer();
        try {
            InputStream in = url.openStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));

            String inline;
            if (report != null) {
                report.setText("reading " + url);
                report.update();
            }
            int nline = 0;

            while ((inline = bis.readLine()) != null) {
                sb.append(inline);
                sb.append("\n");
                nline += 1;

                if (report != null && (nline % 100) == 0) {
                    report.setText("line " + nline);
                    report.update();
                }
            }

            if (report != null) {
                report.setFraction(1.0);
                report.update();
            }

        } catch (Exception ex) {
            E.error("URL read error " + ex);

        }
        return sb.toString();
    }




}
