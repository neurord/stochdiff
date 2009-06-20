package org.catacomb.serial.jar;

import org.catacomb.interlish.structure.Binariable;
import org.catacomb.report.E;
import org.catacomb.serial.Serializer;


import java.io.*;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class CustomJarWriter {

    HashMap<String, Object> itemHM;


    public CustomJarWriter() {
        itemHM = new HashMap<String, Object>();
    }

    public CustomJarWriter(HashMap<String, Object> hm) {
        itemHM = hm;
    }




    public void addMain(String sdata) {
        add(CustomJar.getMetaMain(), "main");
        add("main", sdata);
    }


    public void addMain(File f) {
        add(CustomJar.getMetaMain(), f.getName());
        add(f);
    }

    public void addMimetype(String mt) {
        add(CustomJar.getMetaMime(), mt);
    }



    public void add(File f) {
        add(f.getName(), f);
    }


    public void add(String name, Object value) {
        itemHM.put(name, value);
    }




    public void write(File fout) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (String name : itemHM.keySet()) {
                Object value = itemHM.get(name);
                zos.putNextEntry(new ZipEntry(name));

                if (value instanceof File) {
                    FileInputStream fis  = new FileInputStream((File)value);
                    byte[] buf = new byte[4096];
                    int nread = 0;
                    while ((nread = fis.read(buf)) > 0) {
                        zos.write(buf, 0, nread);
                    }
                    fis.close();


                } else if (value instanceof Binariable) {
                    E.error("data jar binarizable object but code is missing ");

                } else {
                    String sdata = "";
                    if (value instanceof String) {
                        sdata = (String)value;

                    } else {
                        sdata = Serializer.serialize(value);
                    }

                    OutputStreamWriter osw = new OutputStreamWriter(zos);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.write(sdata, 0, sdata.length());
                    bw.flush();
                }

                zos.closeEntry();
            }

            zos.flush();
            zos.close();

            byte[] ba= baos.toByteArray();


            // EXTEND - could hack magic number 	 CustomJar.customize();

            writeByteArrayToFile(ba, fout);
        } catch (Exception ex) {
            E.error("custom jar writing error " + ex);
            ex.printStackTrace();
        }
    }




    private void writeByteArrayToFile(byte[] ba, File fout) throws IOException {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(fout));
        os.write(ba);
        os.flush();
        os.close();
    }



}
