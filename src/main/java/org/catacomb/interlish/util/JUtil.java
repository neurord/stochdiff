package org.catacomb.interlish.util;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.catacomb.Root;
import org.catacomb.interlish.report.Logger;
import org.catacomb.interlish.report.PrintLogger;
import org.catacomb.report.E;


public class JUtil {

    static Class rootClass;

    static String fileSep;
    static String rootPath;

    static {
        rootClass = (new Root()).getClass();
        fileSep = "/"; // System.getProperty("file.separator");
        rootPath = "org" + fileSep + "catacomb";
    }



    public static String getRelativeResource(Object obj, String path) {
        return getRelativeResource(null, obj.getClass(), path);
    }


    public static String getRelativeResource(String s) {
        return getRelativeResource(null, rootClass, s);
    }

    public static String getRelativeResource(Logger logger, Class cls, String path) {
        String sret = null;
        Logger myLogger = logger;
        if (myLogger == null) {
            myLogger = new PrintLogger();
        }
        try {
            if (cls != null) {
                InputStream fis = cls.getResourceAsStream(path);
                sret = readInputStream(myLogger, fis);
            }	 else {
                InputStream fis = ClassLoader.getSystemResourceAsStream(path);
                sret = readInputStream(myLogger, fis);
            }

        } catch (Exception ex) {
            myLogger.log("ResourceAccess - cant get " + path + " " + ex);
            ex.printStackTrace();
        }
        return sret;
    }



    public static String getXMLResource(String path) {
        String sp = null;
        if (path.endsWith(".xml") || path.indexOf(".") < 0) {
            E.warning("getXMLReousrce should have a dot path, not " + path);
            sp = path;
        } else {
            //    E.info("replacing dots in " + path + " with " + fileSep);

            sp = path.replaceAll("\\.", fileSep) + ".xml";
        }
        return getResource(sp);
    }


    public static String getFileResource(String path, String fnm) {
        String sp = path.replaceAll("\\.", fileSep) + fileSep + fnm;
        return getResource(sp);
    }


    public static String getResource(String pathin) {
        String path = pathin;
        String sret = null;

        Logger logger = new PrintLogger();

        try {
            if (path.startsWith(rootPath)) {
                path = path.substring(rootPath.length()+1, path.length());

                //   E.info("seeking stream rel to root class " + path + " " + rootClass.getName());
                InputStream fis = rootClass.getResourceAsStream(path);
                sret = readInputStream(logger, fis);

            } else {
                // E.info("about to read " + path);
                InputStream fis = ClassLoader.getSystemResourceAsStream(path);
                // E.info("fis is " + fis);
                sret = readInputStream(logger, fis);
            }

        } catch (Exception ex) {
            E.error("ResourceAccess - cant get " + path + " " + ex);
            ex.printStackTrace();
        }
        return sret;
    }


    private static String readInputStream(Logger logger, InputStream fis)
    throws NullPointerException, IOException {
        String sret = null;

        InputStreamReader insr = new InputStreamReader(fis);
        BufferedReader fr = new BufferedReader(insr);

        StringBuffer sb = new StringBuffer();
        while (fr.ready()) {
            sb.append(fr.readLine());
            sb.append("\n");
        }
        fr.close();
        sret = sb.toString();

        return sret;
    }



    public static void copyBinaryResource(Logger loggerin, String respathin, File dest) {
        Logger logger = loggerin;
        String respath = respathin;
        if (dest.exists()) {
            //   E.info("destination file already exists - not copying " + dest);
            return;
        }

        if (logger == null) {
            logger = new PrintLogger();
        }


        // E.info("installing " + dest);


        try {
            if (respath.startsWith(rootPath)) {
                respath = respath.substring(rootPath.length()+1,respath.length());
            }
            InputStream in = rootClass.getResourceAsStream(respath);

            OutputStream out = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception ex) {
            E.warning("ResourceAccess - cant get " + respath + " " + ex);
            ex.printStackTrace();
        }
    }






    public static void extractResources(Logger logger, String pathin, File dest) {
        String path = pathin;
        path = path.replaceAll("\\.", fileSep);
        String sl = getFileResource(path, "_files.txt");
        StringTokenizer st = new StringTokenizer(sl, " \n\r\t");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken().trim();
            if (tok.length() > 0) {

                String respath = path + "/" + tok;
                File destfile = new File(dest, tok);
                copyBinaryResource(logger, respath, destfile);
            }
        }


        String sld = getFileResource(path, "_directories.txt");
        StringTokenizer std = new StringTokenizer(sld, " \n\r\t");
        while (std.hasMoreTokens()) {
            String tok = std.nextToken().trim();
            if (tok.length() > 0) {
                File fsub = new File(dest, tok);
                fsub.mkdir();
                extractResources(logger, path + fileSep + tok, fsub);
            }
        }
    }



    public static Object newInstance(String sin) {
        String s = sin;
        Object ret = null;

        if (s.startsWith("org.")) {
            // OK;
        } else {
            s = "org.catacomb." + s; // ADHOC
        }

        try {
            Class<?> c = Class.forName(s);
            ret = c.newInstance();
        } catch (Exception ex) {
            E.error("cant instantiate " + s + " " + ex);
            ex.printStackTrace();
        }
        return ret;
    }



    public static String shortClassName(Object ov) {
        String cnm = ov.getClass().getName();
        cnm = cnm.substring(cnm.lastIndexOf(".") + 1, cnm.length());
        return cnm;
    }


    public static void extractMissingResources(String path, File dir) {
        extractResources(new PrintLogger(), path, dir);
    }





    public static void unpackJar(File fjar, File fout) {
        try {
            JarFile jf = new JarFile(fjar);
            Enumeration en = jf.entries();

            while (en.hasMoreElements()) {
                JarEntry je = (JarEntry) en.nextElement();
                java.io.File f = new File(fout,  je.getName());
                if (je.isDirectory()) {
                    f.mkdirs();
                    continue;

                } else {
                    // f.getParentFile().mkdirs();

                    if (f.getPath().indexOf("META-INF") >= 0) {
                        // skip it
                    } else {
                        f.getParentFile().mkdirs();
                        java.io.InputStream is = jf.getInputStream(je);
                        java.io.FileOutputStream fos = new FileOutputStream(f);

                        // EFF - buffering, file channels??
                        while (is.available() > 0) {
                            fos.write(is.read());
                        }
                        fos.close();
                        is.close();
                    }
                }
            }



            //  E.info("unpacked jar to " + fout);

        } catch (Exception ex) {
            E.error("cant unpack " + fjar + " : " + ex);
        }
    }





    public static void extractJarResources(Object base, String jarName, File destDirectory) {
        try {
            InputStream ins = base.getClass().getResourceAsStream(jarName);
            JarInputStream jins = new JarInputStream(ins);
            while (jins.available() > 0) {
                JarEntry je = jins.getNextJarEntry();
                if (je != null) {
                    // E.info("extracting har entry " + je.getName());
                    File f = new File(destDirectory,  je.getName());
                    if (je.isDirectory()) {
                        f.mkdirs();

                    } else if (f.getPath().indexOf("META-INF") >= 0) {
                        // skip it

                    } else {
                        f.getParentFile().mkdirs();


                        int nb = (int)(je.getSize());

                        byte[] ba = new byte[nb];
                        int nread = 0;
                        while (nread < nb) {
                            nread += jins.read(ba, nread, nb-nread);
                        }
                        OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
                        os.write(ba);
                        os.flush();
                    }
                }
            }


        } catch (Exception ex) {
            E.error("cant extract resources - " + ex);
            ex.printStackTrace();
        }
    }



}
