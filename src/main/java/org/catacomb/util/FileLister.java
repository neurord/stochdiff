package org.catacomb.util;

import java.io.File;

import org.catacomb.report.E;


public class FileLister {




    public static void main(String[] argv) {
        File f = new File(argv[0]);
        E.info("listinig files and folders under " + f.getAbsolutePath());

        listResources(f);
    }





    public static void listResources(String rtpath) {
        listResources(new File(rtpath));
    }

    public static void listResources(File fpar) {
        StringBuffer sbf = new StringBuffer();
        StringBuffer sbd = new StringBuffer();
        for (File f : fpar.listFiles()) {
            if (f.getName().startsWith("_")) {
                // skip it;
            } else {
                if (f.isDirectory()) {
                    listResources(f);
                    sbd.append(f.getName());
                    sbd.append("\n");
                } else if (f.isFile()) {
                    sbf.append(f.getName());
                    sbf.append("\n");
                }
            }

        }
        FileUtil.writeStringToFile(sbf.toString(), new File(fpar, "_files.txt"));
        FileUtil.writeStringToFile(sbd.toString(), new File(fpar, "_directories.txt"));

        //    E.info("written files " + sbf.toString());
        //    E.info("written folders " + sbd.toString());
    }



}
