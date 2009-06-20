package org.catacomb.druid.manifest;

import java.io.File;

import org.catacomb.report.E;
import org.catacomb.serial.Serializer;
import org.catacomb.util.FileUtil;


public class MultiManifester {

    String srcPattern;
    String prefix;


    public MultiManifester(String spat, String relp) {
        srcPattern = spat;
        prefix = relp;
    }



    public DecManifest buildManifest() {
        DecManifest dm = new DecManifest();
        dm.init();

        for (File fsrc : FileUtil.matchingFiles(srcPattern)) {
            File ftop = new File(fsrc, prefix);
            if (ftop.exists() && ftop.isDirectory()) {
                File fnolist = new File(ftop, "nolist.xml");
                if (fnolist.exists()) {
                    E.info("skipping (nolist file present) " + ftop);
                } else {
                    E.info("adding files from " + ftop + ", using prefix " + prefix);
                    dm.addFilesFrom(ftop, prefix);
                }
            }
        }
        return dm;
    }



    public static void main(String[] argv) {
        String sfpat = argv[0];
        String relpath = argv[1];
        File fdest = new File(new File(argv[2]), "DecManifest.xml");

        E.newLine();
        E.info("Creating manifest in " + fdest.getPath());
        FileUtil.writeStringToFile("", fdest);


        MultiManifester mm = new MultiManifester(sfpat, relpath);

        DecManifest xm = mm.buildManifest();

        String ser = Serializer.serialize(xm);

        FileUtil.writeStringToFile(ser, fdest);
    }


}


