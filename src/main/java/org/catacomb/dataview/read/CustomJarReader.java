package org.catacomb.dataview.read;

import org.catacomb.report.E;
import org.catacomb.serial.jar.CustomJar;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class CustomJarReader {

    HashMap<String, Object> itemHM;
    HashMap<String, byte[]> rawHM;

    JarImportContext jarImportContext;



    public CustomJarReader(byte[] ba, JarImportContext jctx) {
        jarImportContext = jctx;
        readAll(ba);
    }




    public void readAll(byte[] bytes) {
        rawHM = new HashMap<String, byte[]>();

        itemHM = new HashMap<String, Object>();

        CustomJar.naturalize(bytes);

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ZipInputStream zis = new ZipInputStream(bais);
            ZipEntry entry = zis.getNextEntry();

            while (entry != null) {
                String name = entry.getName();
                byte[] buffer = readBytes(zis);


                if (CustomJar.isMetaName(name)) {
                    String s = new String(buffer);
                    itemHM.put(name, s);
                } else {
                    rawHM.put(name, buffer);
                }

                entry = zis.getNextEntry();
            }
            zis.close();

        } catch (Exception ex) {
            E.error("zip read exception " + ex);
            ex.printStackTrace();
        }
    }



    public Object getRelative(String name) {
        Object ret = null;
        if (itemHM.containsKey(name)) {
            ret = itemHM.get(name);

        } else if (rawHM.containsKey(name)) {
            byte[] bytes = rawHM.get(name);
            ContentReader cr = Importer.getReader(bytes, jarImportContext);

            ret = cr.getMain();

            itemHM.put(name, ret);
        }
        return ret;
    }






    private byte[] readBytes(InputStream ins) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] bb = new byte[4096];
        int nread = ins.read(bb);

        while (nread > 0) {
            baos.write(bb, 0, nread);
            nread = ins.read(bb);
        }
        return baos.toByteArray();
    }




    public Object getMain() {
        Object ret = null;
        String sn = CustomJar.getMetaMain();

        if (itemHM.containsKey(sn)) {
            Object om = itemHM.get(sn);

            if (om instanceof String) {
                String mainname = (String)om;

                if (hasRelative(mainname)) {
                    ret = getRelative(mainname);
                }
            } else {
                E.error("main name not a string?");
            }

        }
        if (ret == null) {
            E.warning("no main objhect in custom jar - mainname");
        }
        return ret;
    }



    public boolean hasRelative(String name) {
        return (itemHM.containsKey(name) || rawHM.containsKey(name));
    }



}
