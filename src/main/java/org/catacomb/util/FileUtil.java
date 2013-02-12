package org.catacomb.util;

import org.catacomb.report.E;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.util.ArrayList;


public abstract class FileUtil {



    public static byte[] readHeader(File f, int n) {
        byte[] ret = null;
        try {
            FileInputStream ins = new FileInputStream(f);
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



    public static byte[] readBytes(File f) {
        byte[] ret = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] bb = new byte[4096];
            int nread = bis.read(bb);
            while (nread > 0) {
                baos.write(bb, 0, nread);
                nread = bis.read(bb);
            }
            ret = baos.toByteArray();

        } catch (Exception ex) {
            E.error("readNBytes problem " + ex);
        }
        return ret;
    }



    public static String readStringFromFile(File f) {
        String sdat = "null";
        if (f != null) {
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
                E.error("Cant read file " + f);
            }
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
                E.error("file writing error, trying to write file " + fnm);
                ex.printStackTrace();
            }
        }
        return ok;
    }



    public static String getRootName(File f) {
        String fnm = f.getName();
        String root = fnm.substring(0, fnm.lastIndexOf("."));
        return root;
    }



    public static void writeBytes(byte[] ba, File f) {
        writeByteArrayToFile(ba, f);
    }


    public static void writeByteArrayToFile(byte[] ba, File f) {
        if (f == null) {
            return;
        }
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
            os.write(ba);
            os.flush();
        } catch (Exception e) {
            E.error("cannot write byte array " + ba + " to " + f);
        }
    }



    public static void copyFile(File fsrc, File fdest) {
        if (fsrc.exists()) {
            try {
                InputStream in = new FileInputStream(fsrc);
                OutputStream out = new FileOutputStream(fdest);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (Exception ex) {
                E.error("file copy exception");
            }

        } else {
            E.warning("copy - missing file " + fsrc);
        }
    }



    public static String findPath(File f, String name) {
        String ret = null;

        for (File fs : f.listFiles()) {
            if (fs.getName().equals(name)) {
                ret = "";
                break;
            }
        }

        if (ret == null) {
            for (File fd : f.listFiles()) {
                if (fd.isDirectory()) {
                    String s = findPath(fd, name);
                    if (s != null) {
                        if (s.equals("")) {
                            ret = fd.getName();
                        } else {
                            ret = fd.getName() + "/" + s;
                        }
                        break;
                    }
                }
            }
        }
        return ret;
    }



    public static String readFirstLine(File f) {

        String ret = null;
        if (f != null) {
            try {
                InputStream ins = new FileInputStream(f);
                InputStreamReader insr = new InputStreamReader(ins);
                BufferedReader fr = new BufferedReader(insr);
                ret = fr.readLine();
                fr.close();

            } catch (IOException ex) {
                E.error("file read error ");
                ex.printStackTrace();
            }
        }
        return ret;
    }



    public static String getRelativeDirectory(File ftgt, File rtFolder) {
        File fpar = ftgt.getParentFile();
        int ns = 0;

        String sret = null;

        while (fpar != null && !(fpar.equals(rtFolder))) {
            if (sret == null) {
                sret = fpar.getName();
            } else {
                sret = fpar.getName() + "/" + sret;
            }
            fpar = fpar.getParentFile();

            ns += 1;
            if (ns > 8) {
                E.error("too many steps trying to get relative files ? " + ftgt.getAbsolutePath() + " "
                        + rtFolder.getAbsolutePath());
                break;
            }
        }

        return sret;
    }


    // TODO make this smarter (or use GlobFileFilter from jakarta ORO ?)
    public static ArrayList<File> matchingFiles(String srcPattern) {
        ArrayList<File> ret = new ArrayList<File>();
        if (srcPattern.indexOf("*") < 0) {
            File fd = new File(srcPattern);
            if (fd.exists() && fd.isDirectory()) {
                for (File f : fd.listFiles()) {
                    ret.add(f);
                }
            }

        } else {
            int istar = srcPattern.indexOf("*");
            String sa = srcPattern.substring(0, istar);
            String sb = srcPattern.substring(istar + 1, srcPattern.length());
            File ftop = new File(sa);
            for (File fg : ftop.listFiles()) {
                File fp = new File(fg, sb);
                if (fp.exists()) {
                    ret.add(fp);
                }
            }
        }
        return ret;
    }



    public static void deleteDir(File fdir) {
        for (File f : fdir.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        fdir.delete();
    }



    public static File getTempFolder() {
        String s = System.getProperty("java.io.tmpdir");
        File fsystmp = new File(s);
        long l = System.currentTimeMillis();
        File ftmp = new File(fsystmp, "ccmb" + l);
        ftmp.mkdir();
        return ftmp;
    }



    public static void clearCache(File fparam) {
        String fnm = fparam.getName();

        int ild = fnm.lastIndexOf(".");
        if (ild > 0) {
            File fcls = new File(fparam.getParent(), fnm.substring(0, ild) + ".class");
            if (fcls.exists()) {
                fcls.delete();
            } else {
                //  E.warning("no such class file " + fcls);
            }
        }

    }



    public static File getSiblingFile(File fme, String ext) {
        String fnm = fme.getName();
        int ild = fnm.lastIndexOf(".");
        if (ild > 1) {
            fnm = fnm.substring(0, ild);
        }
        File fret = new File(fme.getParentFile(), fnm + ext);
        return fret;

    }



    public static ArrayList<String> getPathElements(File rootDir, File ftgt) {

        ArrayList<String> elts = new ArrayList<String>();
        File fpar = ftgt.getParentFile();
        int ns = 0;

        while (fpar != null && !(fpar.equals(rootDir))) {
            elts.add(0, fpar.getName());
            fpar = fpar.getParentFile();
            ns += 1;
            if (ns > 8) {
                E.error("too many steps trying to get relative files ? " + ftgt.getAbsolutePath() + " "
                        + rootDir.getAbsolutePath());
                break;
            }
        }

        return elts;
    }





}
