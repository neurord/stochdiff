package org.catacomb.util;


import java.io.File;

import java.util.ArrayList;

import org.catacomb.report.E;

import java.io.FileOutputStream;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;

import java.util.HashSet;

public class FileAccumulator {

    File rootDir;

    ArrayList<File> allFiles;


    DirRef rootRef;


    HashSet<File> fileHS;

    public FileAccumulator(File rd) {
        rootDir = rd;
        rootRef = new DirRef("");
        allFiles = new ArrayList<File>();
        fileHS = new HashSet<File>();
    }



    public void addIfNew(File f) {
        if (fileHS.contains(f)) {
            // not new;
        } else {
            add(f);
        }
    }


    public void add(File f) {
        allFiles.add(f);
        fileHS.add(f);



        DirRef pd = rootRef;
        for (String s : FileUtil.getPathElements(rootDir, f)) {
            if (pd.containsDir(s)) {
                pd = pd.getDir(s);
            } else {
                DirRef npd = new DirRef(s);
                pd.add(npd);
                pd = npd;
            }
        }

        FileRef fr = new FileRef(f);
        pd.add(fr);

        // E.info("file accumulator added " + f);
    }



    public void saveJar(File fzin) {
        File fz = fzin;
        if (fz.getName().endsWith(".jar")) {
            // OK
        } else {
            fz = new File(fz.getParentFile(), fz.getName() + ".jar");
        }


        try {
            FileOutputStream fos = new FileOutputStream(fz);
            JarOutputStream zos = new JarOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(zos);
            BufferedWriter bw = new BufferedWriter(osw);

            for (File f : allFiles) {
                String relpath = FileUtil.getRelativeDirectory(f, rootDir) + "/" + f.getName();

                String s = FileUtil.readStringFromFile(f);
                zos.putNextEntry(new JarEntry(relpath));
                bw.write(s, 0, s.length());
                bw.flush();
                zos.closeEntry();
            }

            osw.close();


        } catch (Exception ex) {
            E.error("jar write error " + ex);
        }
    }



    public void addJavaSource(String scrdir, String path) {
        // E.info("adding java source from " + path);

        File fsc = new File(rootDir, scrdir);
        String[] sa = path.split("\\.");

        for (int i = 0; i < sa.length-1; i++) {
            fsc = new File(fsc, sa[i]);
        }

        String sl = sa[sa.length-1];
        if (sl.equals("*")) {
            if (fsc.exists() && fsc.isDirectory()) {
                for (File f : fsc.listFiles()) {
                    addIfNew(f);
                }
            } else {
                E.warning("cant add wildcard imports " + path);
            }


        } else {
            File f = new File(fsc, sl + ".java");
            if (f.exists()) {
                addIfNew(f);
            } else {
                E.warning("missing script file? " + f + " for import " + path);
            }
        }
    }



    public void addJavaSourceSiblings(String scrdir, String pathin) {
        String path = pathin;
        if (path.endsWith(".*")) {
            addJavaSource(scrdir, path);

        } else {
            path = path.substring(0, path.lastIndexOf("."));
            path = path + ".*";
            addJavaSource(scrdir, path);
        }



    }


}
