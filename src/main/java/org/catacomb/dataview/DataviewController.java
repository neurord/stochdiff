package org.catacomb.dataview;


import org.catacomb.dataview.build.DataSource;
import org.catacomb.dataview.build.Dataview;
import org.catacomb.dataview.read.ContentReader;
import org.catacomb.dataview.read.Exporter;
import org.catacomb.dataview.read.FUImportContext;
import org.catacomb.druid.gui.base.DruFrame;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.service.Env;
import org.catacomb.interlish.structure.*;
import org.catacomb.numeric.data.DataExtractor;
import org.catacomb.numeric.data.NumDataSet;
import org.catacomb.report.E;
import org.catacomb.serial.jar.CustomJarWriter;
import org.catacomb.util.AWTUtil;
import org.catacomb.util.ImageUtil;


import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;


public class DataviewController implements Controller {

    Dataview dataview;

    int npainter;
    DataPlotPainter[] painters;

    int ndisplay;
    GraphicsView[] displays;

    FrameController frameController;

    DruFrame mainFrame;
    DruPanel mainPanel;


    DataExtractor dataExtractor;

    ContentReader contentReader;

    String datsrc = "";


    public DataviewController(Dataview dv) {
        super();
        dataview = dv;
    }



    public void initData(ContentReader dsr) {
        contentReader = dsr;

        dataExtractor = new DataExtractor();
        initDataSource();

        exportData();

        if (Env.isApplication()) {
            cacheBinary();
        }
    }


    public void setMainFrame(DruFrame df) {
        mainFrame = df;
    }


    public void exit() {
        mainFrame.dispose();
    }



    public void attached() {
    }



    public void cacheBinary() {

        FUImportContext ctxt = contentReader.getContext();

        File fsrc = ctxt.getSourceFile();

        String cbin = ctxt.getExtensionRelativeName("-cache.bnd");
        File fbin = ctxt.getRelativeFile(cbin);

        String cjar = ctxt.getExtensionRelativeName("sdj");
        File fjar = ctxt.getRelativeFile(cjar);



        if (fbin != null && !(fbin.exists())) {
            DataSource ds = dataview.getDataSource();
            String datname = ds.name;
            NumDataSet omin = getMinimalDataSet(datname);
            Exporter.export(omin, fbin);
        }

        // POSERR
        if (fsrc != null && fsrc.getName().endsWith("xml")) {
            CustomJarWriter jarw = new CustomJarWriter();
            jarw.addMain(fsrc);
            jarw.add(fbin);
            jarw.write(fjar);
        }

    }

    public void cacheData(File fcache, String cnm) {

    }



    private void initDataSource() {
        DataSource ds = dataview.getDataSource();

        String fnm = ds.file;

        Object dataobj = null;

        FUImportContext ctxt = contentReader.getContext();

        String cca = ctxt.getExtensionRelativeName("-cache.bnd");
        if (cca != null && ctxt.hasRelative(cca)) {
            dataobj = ctxt.getRelative(cca);

        } else if (fnm != null && ctxt.hasRelative(fnm)) {
            dataobj = ctxt.getRelative(fnm);
        }

        if (dataobj instanceof NumDataSet) {
            NumDataSet dataSet = (NumDataSet)dataobj;
            dataSet.setName(ds.name);
            dataExtractor.addDataSet(dataSet);

        } else {
            E.error("Dataview controller - wrong data type " + dataobj);
        }

    }



    // ADHOC use standard druid methods for all this!!!!!!!
    public void setDisplays(ArrayList<Object> arl) {
        displays = new GraphicsView[10];
        ndisplay = 0;
        painters = new DataPlotPainter[10];
        npainter = 0;


        for (Object obj : arl) {
            if (obj instanceof GraphicsView) {
                displays[ndisplay++] = (GraphicsView)obj;

            } else if (obj instanceof DataPlotPainter) {
                painters[npainter++] = (DataPlotPainter)obj;

            } else if (obj instanceof FrameController) {
                frameController = (FrameController)obj;
                frameController.setDataviewController(this);


            } else if (obj instanceof ModeSetter) {
                E.missing();
//            ((ModeSetter)obj).setModeSettable(this);

            } else if (obj instanceof DruPanel && ((DruPanel)obj).getID().equals("main")) {
                // OK - handled later;


            } else {
                E.error("dataview controller cant handle display item " + obj);
            }

            if (obj instanceof DruPanel && ((DruPanel)obj).getID().equals("main")) {
                mainPanel = (DruPanel)obj;
            }

        }
    }



    private void setPaintWidthFactor(int ithick) {
        for (int i = 0; i < npainter; i++) {
            painters[i].setPaintWidthFactor(ithick);
        }
    }


    private void exportData() {

        for (int i = 0; i < npainter; i++) {
            painters[i].setDataSource(dataExtractor);
        }

        if (frameController != null) {
            frameController.setDataSource(dataExtractor);
        }

        updateDisplays();
    }



    private void markNeeded() {

        for (int i = 0; i < npainter; i++) {
            painters[i].markNeeded();
        }

        if (frameController != null) {
            frameController.markNeeded();
        }

    }


    public void setMode(String dom, String mod) {
        E.missing("cant set mode from here");
        for (int i = 0; i < ndisplay; i++) {
            // displays[i].setMode(dom, mod);
        }
    }


    public void showFrame(int iframe) {
        for (int i = 0; i < npainter; i++) {
            painters[i].showFrame(iframe);
        }
        updateDisplays();
    }



    private void updateDisplays() {
        for (int i = 0; i < ndisplay; i++) {
            displays[i].viewChanged();
        }
    }



    public void requestClose() {
        E.missing();
        requestExit();
    }


    public void requestExit() {
        E.missing();
        System.exit(0);
    }



    public BufferedImage getBufferedImage(int ithick) {

        setPaintWidthFactor(ithick);

        BufferedImage ret = AWTUtil.getBufferedImage(mainPanel);

        setPaintWidthFactor(1);

        return ret;
    }



    private NumDataSet getMinimalDataSet(String datname) {
        markNeeded();

        NumDataSet nds = dataExtractor.getDataSet(datname);

        NumDataSet ret = nds.copyMarked();

        return ret;

    }


    // / ***REFACTOR move much of htis to ImageUtil



    public void saveImage(File file, int ifr) {
        showFrame(ifr);
        BufferedImage bim = getBufferedImage(1);
        ImageUtil.writePNG(bim, file);
    }


    public void saveThumbnailImage(File file, int ifr) {
        showFrame(ifr);

        BufferedImage bim = getBufferedImage(1);

        int wscl = 140;
        int hscl = 140;

        int wf = bim.getWidth();
        int hf = bim.getHeight();
        double fo = ((float)wscl) / hscl;
        double ff = ((float)wf) / hf;
        if (ff > fo) {
            hscl = (int)(wscl / ff);
        } else {
            wscl = (int)(hscl * ff);
        }
        int ithick = ((wf + wscl / 2) / wscl);
        bim = getBufferedImage(ithick);
        BufferedImage bufim = AWTUtil.getScaledBufferedImage(bim, wscl, hscl);
        ImageUtil.writePNG(bufim, file);
    }



    public void makeMovie(File f) {
        if (frameController != null) {
            frameController.makeMovie(f);
        } else {
            E.error("no frame controller - cant make movie");
        }
    }


    public void makeThumbnailMovie(File f) {
        if (frameController != null) {
            frameController.makeThumbnailMovie(f);
        } else {
            E.error("no frame controller - cant make movie");
        }
    }

}
