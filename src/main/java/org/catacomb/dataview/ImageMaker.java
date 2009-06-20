package org.catacomb.dataview;


import org.catacomb.report.E;

import java.io.File;


public class ImageMaker {




    public static void main(String[] argv) {
        String conf = argv[0];

        File fconf = new File(conf);

        DataViewer dv = new DataViewer(fconf);

        DataviewController dvc = dv.getController();


        String fnm = fconf.getAbsolutePath();
        int ild = fnm.lastIndexOf(".");
        fnm = fnm.substring(0, ild);


        File fim = new File(fnm + ".png");
        dvc.saveImage(fim, 0);
        File fimtn = new File(fnm + "-tn.png");
        dvc.saveThumbnailImage(fimtn, 0);

        if (argv.length > 1) {
            for (int i = 1; i < argv.length; i++) {
                int ifr = Integer.parseInt(argv[i]);
                File ffr = new File(fnm + "_" + ifr + ".png");
                dvc.saveImage(ffr, ifr);

                File ffrtn = new File(fnm + "_" + ifr + "-tn.png");
                dvc.saveThumbnailImage(ffrtn, ifr);
            }
        }



        File ffull = new File(fnm + ".gif");
        File fthumb = new File(fnm + "-tn.gif");

        E.info("making full movie " + ffull);
        //      dvc.makeMovie(ffull);

        E.info("making thumbnail movie " + ffull);
        dvc.makeThumbnailMovie(fthumb);


        System.exit(0);
    }

}
