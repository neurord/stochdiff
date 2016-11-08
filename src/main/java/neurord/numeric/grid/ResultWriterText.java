package neurord.numeric.grid;

import java.io.File;

import java.io.*;

import java.util.List;
import java.util.HashMap;
import java.util.StringTokenizer;

import neurord.numeric.morph.VolumeGrid;
import neurord.inter.SDState;
import neurord.inter.StateReader;
import neurord.model.IOutputSet;
import neurord.util.ArrayUtil;
import neurord.util.FileUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class ResultWriterText implements ResultWriter {
    static final Logger log = LogManager.getLogger();

    final File outputFile;

    OutputStreamWriter writer;

    boolean closed = false;
    boolean continuation = false;

    final boolean writeConcentration;

    final protected HashMap<String, ResultWriterText> siblings = new HashMap<>();

    final String[] species;
    final int[] ispecout;
    final int nel;
    final IOutputSet outputSet;
    final List<? extends IOutputSet> outputSets;
    final VolumeGrid grid;

    public ResultWriterText(File output,
                            IOutputSet primary,
                            List<? extends IOutputSet> outputSets,
                            String[] species,
                            VolumeGrid grid,
                            boolean writeConcentration) {

        this.writeConcentration = writeConcentration;
        this.outputFile = new File(output + ".out");

        this.species = species;
        this.grid = grid;
        if (primary != null) {
            this.ispecout = primary.getIndicesOfOutputSpecies(species);
            this.outputSet = primary;
        } else {
            this.ispecout = null;
            this.outputSet = null;
        }
        this.outputSets = outputSets;
        this.nel = grid.size();
    }

    public boolean isContinuation() {
        return continuation && outputFile.exists();
    }

    @Override
    public File outputFile() {
        return this.outputFile;
    }

    @Override
    public void init(String magic) {
        try {
            if (isContinuation()) {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile, true));
            } else {
                writer = new OutputStreamWriter(new FileOutputStream(outputFile));
                if (magic != null)
                    writer.write(magic + "\n");
            }
        } catch (Exception ex) {
            log.error("cannot create file writer", ex);
            throw new RuntimeException(ex);
        }
    }

    public void writeString(String sdat) {
        if (writer != null)
            try {
                writer.write(sdat, 0, sdat.length());
            } catch (Exception ex) {
                log.error("cannot write:", ex);
                throw new RuntimeException(ex);
            }
    }

    @Override
    public void close() {
        if (!closed) {
            log.info("Closing output file {}", this.outputFile);

            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    log.error("Closing failed", ex);
                }
                writer = null;
            } else
                log.error("data not written (earlier errors)");

            for (ResultWriterText rw : siblings.values())
                rw.close();

            closed = true;
        }
    }

    public ResultWriterText getSibling(String extn, String magic) {
        ResultWriterText ret = getRawSibling(extn);
        ret.init(magic);
        return ret;
    }

    public ResultWriterText getRawSibling(String extn) {
        ResultWriterText ret = siblings.get(extn);
        log.debug("getRawSibling {} → {}", extn, ret);

        if (ret == null) {
            String fnm = FileUtil.getRootName(this.outputFile) + extn;
            File f = new File(outputFile.getParentFile(), fnm);
            ret = new ResultWriterText(f, null, null, this.species, this.grid, this.writeConcentration);
            ret.init(null);
            siblings.put(extn, ret);
        }

        return ret;
    }



    public File getSiblingFile(String extn) {
        ResultWriterText rw = getSibling(extn, null);
        return rw.getFile();
    }


    private File getFile() {
        return outputFile;
    }

    public void writeToSiblingFileAndClose(String txt, String extn) {
        ResultWriterText rw = getSibling(extn, null);
        rw.writeString(txt);
        rw.close();
    }

    public void writeToSiblingFile(String txt, String extn) {
        writeToSiblingFile(txt, extn, null);
    }

    public void writeToFinalSiblingFile(String txt, String extn) {
        writeToFinalSiblingFile(txt, extn, null);
    }

    public void writeToSiblingFile(String txt, String extn, String magic) {
        ResultWriterText rw = getRawSibling(extn);
        rw.writeString(txt);
    }

    public void writeToFinalSiblingFile(String txt, String extn, String magic) {
        ResultWriterText rw = getSibling(extn, magic);
        rw.writeString(txt);
        rw.close();
    }

    public String readSibling(String fnm) {
        String ret = null;
        File fin = new File(outputFile.getParentFile(), fnm);
        if (fin.exists())
            ret = FileUtil.readStringFromFile(fin);
        else
            log.error("No such file \"{}\"", fin.getAbsolutePath());

        return ret;
    }

    /**
     * this expects each record to begin with max, and then have a time
     * field as the idx'th element of the line. It keeps records as long
     * as their time is &lt; value but discards the rest
     */
    public void pruneFrom(String match, int idx, double value) {
        continuation = true;
        File fcopy = outputFile.getAbsoluteFile();
        File fwk = new File(fcopy.getParentFile(), fcopy.getName()+".wk");

        fcopy.renameTo(fwk);
        try {
            BufferedReader br = new BufferedReader(new FileReader(fwk));
            BufferedWriter bw = new BufferedWriter(new FileWriter(fcopy));

            while (br.ready()) {
                String sl = br.readLine();
                if (match == null || match.length() == 0 || sl.startsWith(match)) {
                    StringTokenizer st = new StringTokenizer(sl, " ");
                    if (st.countTokens() > idx) {
                        String stok = "";
                        for (int i = 0; i <= idx; i++)
                            stok = st.nextToken();
                        try {
                            double d = Double.parseDouble(stok);
                            if (d >= value - 1.e-9)
                                break;
                        } catch (NumberFormatException ex) {
                            // its ok - probably a header rather than a number
                        }
                    }
                }
                bw.write(sl);
                bw.write("\n");
            }

            bw.close();
            br.close();

            fwk.delete();

        } catch (Exception ex) {
            log.error("Cannot prune data file", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void writeGrid(VolumeGrid vgrid, double startTime, IGridCalc source) {
        assert vgrid.isCurved() || vgrid.isCuboid();

        if (!this.isContinuation())
            this.writeString(vgrid.getAsText());

        this.writeToSiblingFileAndClose(vgrid.getAsTableText(), "-mesh.txt");

        if (vgrid.isCurved())
                this.writeToSiblingFileAndClose(vgrid.getAsElementsText(), "-elements.tri");

        log.info("Written elements mesh file");

        if (this.outputSets != null)
            for (IOutputSet out: this.outputSets) {
                String sibsuf = "-" + out.getIdentifier() + "-conc.txt";
                String shead = getGridConcsHeadings_dumb(out, vgrid, source);
                StringTokenizer st = new StringTokenizer(shead);
                int nt = st.countTokens();

                if (this.isContinuation()) {
                    ResultWriterText sibrw = this.getRawSibling(sibsuf);
                    sibrw.pruneFrom("", 0, startTime);
                    sibrw.init(null);
                } else {
                    this.writeToSiblingFile(shead, sibsuf);
                }
            }
    }

    private String formatNumber(int i, int outj, IGridCalc source) {
        if (writeConcentration) {
            double conc =  source.getGridPartConc(i, outj);
            return stringd(conc);
        } else {
            int numb = source.getGridPartNumb(i, outj);
            return stringi(numb);
        }
    }

    private String getGridConcsText(double time, int[] ispecout, IGridCalc source) {
        final String[] species = source.getSource().getSpecies();
        StringBuffer sb = new StringBuffer();
        // TODO tag specific to integer quantities;
        int nspecout = ispecout.length;
        if (nspecout == 0)
            return "";

        sb.append("gridConcentrations " + this.nel + " " + nspecout + " " + time + " ");
        for (int i = 0; i < nspecout; i++)
            sb.append(species[ispecout[i]] + " ");
        sb.append("\n");

        for (int i = 0; i < this.nel; i++) {
            for (int j = 0; j < nspecout; j++)
                sb.append(this.formatNumber(i, ispecout[j], source));
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void writeOutputInterval(double time, IGridCalc source) {
        String concs = this.getGridConcsText(time, this.ispecout, source);
        this.writeString(concs);
    }

    private String getGridConcsPlainText_dumb(IOutputSet output, double time, IGridCalc source) {
        final String[] species = source.getSource().getSpecies();
        final VolumeGrid grid = source.getSource().getVolumeGrid();

        final String region = output.getRegion();
        final int[] indices = output.getIndicesOfOutputSpecies(species);

        StringBuffer sb = new StringBuffer();
        sb.append(stringd(time));

        for (int specie: indices)
            for (int i = 0; i < this.nel; i++)
                if (region == null || region.equals(grid.getElementRegion(i)))
                    sb.append(this.formatNumber(i, specie, source));

        sb.append("\n");
        return sb.toString();
    }

    private String getGridConcsHeadings_dumb(IOutputSet output, VolumeGrid vgrid, IGridCalc source) {
        final String[] species = source.getSource().getSpecies();
        final boolean[] submembranes = vgrid.getSubmembranes();
        final String[] regionLabels = vgrid.getRegionLabels();
        final VolumeGrid grid = source.getSource().getVolumeGrid();

        StringBuffer sb = new StringBuffer();
        sb.append("time");

        final int[] indices = output.getIndicesOfOutputSpecies(species);
        final String region = output.getRegion();

        for (int specie: indices)
            for (int i = 0; i < this.nel; i++)
                if (region == null || region.equals(grid.getElementRegion(i))) {
                    sb.append(" Vol_" + i);
                    sb.append("_" + grid.getElementRegion(i));

                    String tempLabel = vgrid.getLabel(i);

                    if (vgrid.getGroupID(i) != null) {
                        sb.append("." + vgrid.getGroupID(i));
                    } else if (tempLabel != null) {
                        if (tempLabel.indexOf(".") > 0)
                            sb.append("." + tempLabel.substring(0, tempLabel.indexOf(".")));
                    }
                    if (submembranes[i])
                        sb.append("_submembrane");
                    else
                        sb.append("_cytosol");

                    if (tempLabel != null) {
                        if (tempLabel.indexOf(".") > 0)
                            sb.append("_" + tempLabel.substring(tempLabel.indexOf(".") + 1,
                                                                tempLabel.length()));
                        else
                            sb.append("_" + vgrid.getLabel(i));
                    }

                    sb.append("_Spc_" + species[specie]);
                }

        sb.append("\n");
        return sb.toString();
    }


    @Override
    public void writeOutputScheme(int i, double time, IGridCalc source) {
        IOutputSet output = this.outputSets.get(i);
        String fnamepart = output.getIdentifier();
        log.debug("writeOutputScheme: i={} time={} nel={} fnamepart={}", i, time, this.nel, fnamepart);
        String text = getGridConcsPlainText_dumb(output, time, source);
        this.writeToSiblingFile(text, "-" + fnamepart + "-conc.txt");
    }

    @Override
    public void writeEventStatistics(double time, IGridCalc source) {}

    // let's park those two here for now
    public static String stringd(double d) {
        if (d == 0.0)
            return "0.0 ";
        else
            return String.format("%.5g ", d);
    }

    // <--RO 7 02 2008
    // Saves as integers; used to save particles instead of concentrations
    public static String stringi(int id) {
        if (id == 0)
            return "00 ";
        else
            return String.format("%d ", id);
    }

    @Override
    public void closeTrial(IGridCalc source) {};
}
