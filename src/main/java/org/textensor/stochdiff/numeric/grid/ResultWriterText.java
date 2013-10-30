package org.textensor.stochdiff.numeric.grid;

import java.io.File;

import org.catacomb.util.FileUtil;
import org.textensor.report.E;

import java.io.*;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.inter.SDState;
import org.textensor.stochdiff.inter.StateReader;
import org.textensor.util.inst;
import org.textensor.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class ResultWriterText implements ResultWriter {
    static final Logger log = LogManager.getLogger(ResultWriterText.class);

    final File outputFile;

    OutputStreamWriter writer;

    boolean closed = false;
    boolean continuation = false;

    final boolean writeConcentration;

    final protected HashMap<String, ResultWriterText> siblings = inst.newHashMap();

    public ResultWriterText(File output, boolean writeConcentration) {
        this.writeConcentration = writeConcentration;

        outputFile = new File(output + ".out");
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
            E.error("cannot create file writer " + ex);
        }
    }

    public void writeString(String sdat) {
        if (writer != null) {
            try {
                writer.write(sdat, 0, sdat.length());
            } catch (Exception ex) {
                E.error("cannot write: " + ex);
            }
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
                    E.error("ex " + ex);
                }
                writer = null;
            } else
                E.error("data not written (earlier errors)");

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
            ret = new ResultWriterText(f, this.writeConcentration);
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
            E.error("No such file " + fin.getAbsolutePath());

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
            E.error("cannot prune data file: " + ex);
        }
    }

    @Override
    public void writeGrid(VolumeGrid vgrid, double startTime, String[] fnmsOut, IGridCalc source) {
        assert vgrid.isCurved() || vgrid.isCuboid();

        if (!this.isContinuation())
            this.writeString(vgrid.getAsText());

        this.writeToSiblingFileAndClose(vgrid.getAsTableText(), "-mesh.txt");
            
        if (vgrid.isCurved())
                this.writeToSiblingFileAndClose(vgrid.getAsElementsText(), "-elements.tri");

        E.info("Written elements mesh file");

        for (int i = 0; i < fnmsOut.length; i++) {
            String sibsuf = "-" + fnmsOut[i] + "-conc.txt";
            String shead = getGridConcsHeadings_dumb(i, vgrid, source);
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

    private String getGridConcsText(double time, int nel, int[] ispecout, IGridCalc source) {
        final String[] specieIDs = source.getSource().getSpecieIDs();
        StringBuffer sb = new StringBuffer();
        // TODO tag specific to integer quantities;
        int nspecout = ispecout.length;
        if (nspecout == 0)
            return "";

        sb.append("gridConcentrations " + nel + " " + nspecout + " " + time + " ");
        for (int i = 0; i < nspecout; i++)
            sb.append(specieIDs[ispecout[i]] + " ");
        sb.append("\n");

        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < nspecout; j++)
                sb.append(this.formatNumber(i, ispecout[j], source));
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void writeGridConcs(double time, int nel, int ispecout[], IGridCalc source) {
        String concs = this.getGridConcsText(time, nel, ispecout, source);
        this.writeString(concs);
    }

    private String getGridConcsPlainText_dumb(int filenum, double time, int nel, IGridCalc source) {
        final int[][] specIndexesOut = source.getSource().getSpecIndexesOut();
        final String[] regionLabels = source.getSource().getVolumeGrid().getRegionLabels();
        final int[] eltRegions = source.getSource().getVolumeGrid().getRegionIndexes();
        final String[] regionsOut = source.getSource().getRegionsOut();

        StringBuffer sb = new StringBuffer();
        sb.append(stringd(time));

        for (int j = 0; j < specIndexesOut[filenum].length; j++)
            for (int i = 0; i < nel; i++)
                if (regionsOut[filenum].equals("default") || regionsOut[filenum].equals(regionLabels[eltRegions[i]]))
                    sb.append(this.formatNumber(i, specIndexesOut[filenum][j], source));

        sb.append("\n");
        return sb.toString();
    }

    private String getGridConcsHeadings_dumb(int filenum, VolumeGrid vgrid, IGridCalc source) {
        final int[][] specIndexesOut = source.getSource().getSpecIndexesOut();
        final String[] regionsOut = source.getSource().getRegionsOut();
        final String[] regionLabels = vgrid.getRegionLabels();
        final String[] specieIDs = source.getSource().getSpecieIDs();
        final boolean[] submembranes = vgrid.getSubmembranes();
        final int[] eltRegions = source.getSource().getVolumeGrid().getRegionIndexes();

        StringBuffer sb = new StringBuffer();
        sb.append("time");

        for (int j = 0; j < specIndexesOut[filenum].length; j++) {
            for (int i = 0; i < vgrid.getNElements(); i++) {

                // WK 6 17 2007
                if (regionsOut[filenum].equals("default") || regionsOut[filenum].equals(regionLabels[eltRegions[i]])) {
                    sb.append(" Vol_" + i);
                    sb.append("_" + regionLabels[eltRegions[i]]);

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
                    // WK

                    sb.append("_Spc_" + specieIDs[specIndexesOut[filenum][j]]);
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }


    @Override
    public void writeGridConcsDumb(int i, double time, int nel, String fnamepart, IGridCalc source) {
        log.debug("writeGridConcsDumb: i={} time={} nel={} fnamepart={}", i, time, nel, fnamepart);
        String text = getGridConcsPlainText_dumb(i, time, nel, source);
        this.writeToSiblingFile(text, "-" + fnamepart + "-conc.txt");
    }

    @Override
    public void saveState(double time, String prefix, IGridCalc source) {
        String state = getStateText(source);
        this.writeToFinalSiblingFile(state, prefix + Math.round(time) + ".nrds");
    }

    protected String getStateText(IGridCalc source) {
        String[] specieIDs = source.getSource().getSpecieIDs();
        int nel = source.getNumberElements();

        StringBuffer sb = new StringBuffer();
        sb.append("nrds " + nel + " " + specieIDs.length + "\n");
        for (int i = 0; i < specieIDs.length; i++) {
            sb.append(specieIDs[i] + " ");
        }
        sb.append("\n");
        for (int i = 0; i < nel; i++) {
            for (int j = 0; j < specieIDs.length; j++) {
                if (source.preferConcs())
                    sb.append(stringd(source.getGridPartConc(i, j)));
                else
                    sb.append(stringi(source.getGridPartNumb(i, j)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

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

    public double[][] _readInitialState(String fnm, int nel, int nspec, String[] specids) {
        String sdata = this.readSibling(fnm);
        SDState sds = StateReader.readStateString(sdata);

        double[][] ret = null;
        if (sds.nel == nel && sds.nspec == nspec) {
            if (ArrayUtil.arraysMatch(sds.specids, specids)) {
                ret = sds.getData();
            } else {
                E.error("initial conditions species mismatch ");
                for (int i = 0; i < specids.length; i++) {
                    E.info("species " + i + " " + specids[i] + " " + sds.specids[i]);
                }
            }
        } else {
            throw new RuntimeException("initial conditions file does not match model: elements "
                                       + nel + ", " + sds.nel +
                                       "  species: " + nspec + ", " + sds.nspec);
        }

        return ret;
    }

    public Object loadState(String fnm, IGridCalc source) {
            int nel = source.getNumberElements();
            String[] species = source.getSource().getSpecieIDs();
            double[][] state = this._readInitialState(fnm, nel, species.length, species);
            if (source.preferConcs())
                return state;
            else {
                int[][] nums = new int[nel][species.length];
                for (int i = 0; i < nums.length; i++)
                    for (int j = 0; j < nums[0].length; j++)
                        nums[i][j] = (int) Math.round(state[i][j]);
                return nums;
            }
    }
}
