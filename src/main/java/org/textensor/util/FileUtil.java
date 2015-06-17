package org.textensor.util;


import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.util.ArrayList;

public abstract class FileUtil {

    public static String readStringFromFile(File f) {
        String sdat = "null";
        if (f != null)
            try {
                boolean dogz = (f.getName().endsWith(".gz"));
                InputStream ins = new FileInputStream(f);
                if (dogz) {
                    ins = new GZIPInputStream(ins);
                }
                InputStreamReader insr = new InputStreamReader(ins);
                BufferedReader fr = new BufferedReader(insr);

                StringBuffer sb = new StringBuffer();
                while (fr.ready()) {
                    sb.append(fr.readLine());
                    sb.append("\n");
                }
                fr.close();
                sdat = sb.toString();

            } catch (IOException ex) {
                throw new RuntimeException("file read error", ex);
            }

        return sdat;
    }

    public static boolean writeStringToFile(String sdat, File f) {
        String fnm = f.getName();
        boolean ok = false;
        if (f != null) {
            boolean dogz = (fnm.endsWith(".gz"));
            try {
                OutputStream fos = new FileOutputStream(f);
                if (dogz) {
                    fos = new GZIPOutputStream(fos);
                }
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                osw.write(sdat, 0, sdat.length());
                osw.close();
                ok = true;

            } catch (IOException ex) {
                throw new RuntimeException("File write error, when writing " + fnm);
            }
        }
        return ok;
    }

    public static String getRootName(File f) {
        String fnm = f.getName();
        int index = fnm.lastIndexOf(".");
        return fnm.substring(0, index == -1 ? fnm.length() : index);
    }
}
