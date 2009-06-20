package org.catacomb.dataview;



import org.catacomb.interlish.structure.Element;
import org.catacomb.report.E;
import org.catacomb.serial.ElementImporter;
import org.catacomb.util.FileUtil;

import java.io.File;


public class BatchImageMaker {


    File fsrc;
    File fviewdir;
    File fdestdir;


    public static void main(String[] argv) {
        BatchImageMaker bim = new BatchImageMaker(argv);
        bim.process();
        //      Element srcelt = ElementImporter.getElement(new File(argv[0]));
    }



    public BatchImageMaker(String[] argv) {
        fsrc = new File(argv[0]); // xml list of figures;

        fviewdir = new File(argv[1]);
        fdestdir = new File(argv[2]);
    }



    public void process() {

        E.info("about to read " + fsrc);


        Element srcelt = ElementImporter.getElement(fsrc);

        Element[] elta = srcelt.getElementArray();

        for (int i = 0; i < elta.length; i++) {
            Element elt = elta[i];

            if (elt.getName().equals("figure")) {
                makeFigure(elt);

            } else if (elt.getName().equals("movie")) {
                makeMovie(elt);

            } else {
                E.warning("unknown element type in figure list " + elt);
            }
        }
    }



    public void makeFigure(Element elt) {
        if (elt.hasAttribute("view")) {
            // OK;
        } else {
            E.error("not processing element (no view attribute) " + elt);
            return;
        }
        String sconf = elt.getAttribute("view");

        int ifr = -1;
        if (elt.hasAttribute("frame")) {
            ifr = Integer.parseInt(elt.getAttribute("frame"));
        }


        String sroot = elt.getAttribute("path") + elt.getAttribute("label");
        if (ifr >= 0) {
            sroot += "_" + ifr;
        }
        File fim = new File(fdestdir, sroot + ".png");
        File ftn = new File(fdestdir, sroot + "-tn.png");
        fim.getParentFile().mkdirs();

        if (fim.exists() && ftn.exists()) {
            // nothing to do;
        } else {


            File fconf = new File(fviewdir, sconf + ".xml");
            DataViewer dv = new DataViewer(fconf);
            DataviewController dvc = dv.getController();

            if (!fim.exists()) {
                dvc.saveImage(fim, ifr);
                E.info("written image " + fim);
            }
            if (!ftn.exists()) {
                dvc.saveThumbnailImage(ftn, ifr);
                E.info("written image " + ftn);
            }

            dvc.exit();
        }
    }




    public void makeMovie(Element elt) {
        if (elt.hasAttribute("view")) {
            // OK;
        } else {
            E.error("not processing element (no view attribute) " + elt);
            return;
        }

        String sconf = elt.getAttribute("view");


        String sroot = elt.getAttribute("path") + elt.getAttribute("label");

        File fmo = new File(fdestdir, sroot + ".gif");
        File ftn = new File(fdestdir, sroot + "-tn.gif");

        File fsubdest = fmo.getParentFile();
        fsubdest.mkdirs();

        if (fmo.exists() && ftn.exists()) {
            // nothing to do;

        } else {

            File fconf = new File(fviewdir, sconf + ".xml");
            DataViewer dv = new DataViewer(fconf);
            DataviewController dvc = dv.getController();

            copyFiles(fconf, fsubdest);

            if (!fmo.exists()) {
                dvc.makeMovie(fmo);
                E.info("written movie " + fmo);
            }
            if (!ftn.exists()) {
                dvc.makeThumbnailMovie(ftn);
                E.info("written movie " + ftn);
            }

            dvc.exit();
        }
    }



    private void copyFiles(File fconf, File fdd) {
        File fpar = fconf.getParentFile();
        String fnm = fconf.getName();
        String sr = fnm.substring(0, fnm.lastIndexOf("."));

        String sr1 = sr + "-cache.bnd";
        String sr2 = sr + ".sdj";
        FileUtil.copyFile(fconf, new File(fdd, fconf.getName()));
        FileUtil.copyFile(new File(fpar, sr1), new File(fdd, sr1));
        FileUtil.copyFile(new File(fpar, sr2), new File(fdd, sr2));

    }


}
