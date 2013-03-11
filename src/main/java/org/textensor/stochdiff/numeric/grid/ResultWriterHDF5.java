package org.textensor.stochdiff.numeric.grid;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;

import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_UNLIMITED;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Datatype;
import ncsa.hdf.object.h5.H5ScalarDS;

import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.util.FileUtil;
import org.textensor.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ResultWriterHDF5 implements ResultWriter {
    static final Logger log = LogManager.getLogger(ResultWriterHDF5.class);

    final protected File outputFile;
    protected H5File output;
    protected Group sim;
    protected H5ScalarDS concs = null;
    protected H5ScalarDS conc_times = null;
    protected H5ScalarDS species = null;

    public static final H5Datatype double_t =
        new H5Datatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, Datatype.NATIVE);

    public ResultWriterHDF5(File outFile) {
        this.outputFile = new File(FileUtil.getRootName(outFile) + ".h5");
        log.info("Writing HDF5 to {}", this.outputFile);
    }

    @Override
    public void init(String magic) {
        try {
            this._init(magic);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void _init(String magic)
        throws Exception
    {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        assert fileFormat != null;
        log.info("Opening output file {}", this.outputFile);
        this.output = (H5File) fileFormat.create(this.outputFile.toString());
        assert this.output != null;

        try {
            this.output.open();
        } catch(Exception e) {
            log.error("Failed to open results file {}", this.outputFile);
            throw e;
        }

        Group root = (Group)((DefaultMutableTreeNode) this.output.getRootNode()).getUserObject();
        this.sim = this.output.createGroup("simulation", root);
    }

    @Override
    public void close() {
        log.info("Closing output file {}", this.outputFile);
        try {
            this.output.close();
        } catch(Exception e) {
            log.error("Failed to close results file {}", outputFile, e);
        }
    }

    @Override
    public void writeGrid(VolumeGrid vgrid, double startTime, String fnmsOut[], IGridCalc source) {
        log.info("Writing grid at time {}", startTime);
        int n = vgrid.getNElements();
        long[]
            dims = {n,},
            chunks = {n,};
        int gzip = 6;
        String[] memberNames = {"x0", "y0", "z0",
                                "x1", "y1", "z1",
                                "x2", "y2", "z2",
                                "x3", "y3", "z3",
                                "volume", "deltaZ"};

        Datatype[] memberTypes = new Datatype[memberNames.length];
        Arrays.fill(memberTypes, double_t);

        Vector<Object> data = vgrid.gridData();

        Dataset grid;
        try {
            grid = this.output.createCompoundDS("grid", this.sim, dims, null, chunks, gzip,
                                                memberNames, memberTypes, null, data);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeVector(String name, Group parent, String[] items)
        throws Exception
    {
        int maxlength = ArrayUtil.maxLength(items);
        long[] dims = {items.length};

        H5Datatype string_t = new H5Datatype(Datatype.CLASS_STRING, maxlength,
                                             Datatype.NATIVE, Datatype.NATIVE);

        this.output.createScalarDS(name, parent,
                                   string_t, dims, null, null,
                                   0, items);
    }

    protected void writeSpecies(int[] ispecout, IGridCalc source)
        throws Exception
    {
        String[] specieIDs = source.getSpecieIDs();
        String[] outSpecies = new String[ispecout.length];
        for (int i = 0; i < ispecout.length; i++)
            outSpecies[i] = specieIDs[ispecout[i]];

        this.writeVector("species", this.sim, outSpecies);
    }

    protected void writeRegionLabels(IGridCalc source)
        throws Exception
    {
        String[] regions = source.getRegionLabels();
        this.writeVector("regions", this.sim, regions);
    }

    protected boolean initConcs(int nel, int[] ispecout, IGridCalc source)
        throws Exception
    {
        assert this.concs == null;
        assert this.conc_times == null;

        this.writeSpecies(ispecout, source);
        this.writeRegionLabels(source);

        int nspecout = ispecout.length;
        if (nspecout == 0)
            return false;

        /* times x nel x nspecout, but we write only for only time 'time' at one time */
        {
            long[] dims = {1, nel, nspecout};
            long[] size = {H5F_UNLIMITED, nel, nspecout};
            long[] chunks = {32, nel, nspecout};

            this.concs = (H5ScalarDS)
                this.output.createScalarDS("concentrations", this.sim,
                                           double_t, dims, size, chunks,
                                           9, null);
            this.concs.init();
        }

        {
            long[] dims = {1};
            long[] size = {H5F_UNLIMITED};
            long[] chunks = {1024};
            double[] times = {0.0};

            this.conc_times = (H5ScalarDS)
                this.output.createScalarDS("times", this.sim,
                                           double_t, dims, size, chunks,
                                           9, times);
            this.conc_times.init();
        }
        return true;
    }

    public void getGridNumbers(double[] dst,
                             int nel, int ispecout[], IGridCalc source) {
        assert dst.length == nel * ispecout.length;
        int pos = 0;
        for (int i = 0; i < nel; i++)
            for (int j = 0; j < ispecout.length; j++)
                dst[pos++] = source.getGridPartNumb(i, ispecout[j]);
    }

    @Override
    public void writeGridConcs(double time, int nel, int ispecout[], IGridCalc source) {
        log.info("Writing grid concentrations at time {}", time);
        try {
            this._writeGridConcs(time, nel, ispecout, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void _writeGridConcs(double time, int nel, int ispecout[], IGridCalc source) 
        throws Exception
    {
        final long[] dims;
        if (this.concs == null) {
            if (!this.initConcs(nel, ispecout, source))
                return;
            dims = this.concs.getDims();;
        } else {
            dims = this.concs.getDims();
            dims[0] = dims[0] + 1;
            this.concs.extend(dims);

            long[] dims2 = this.conc_times.getDims();
            dims2[0] = dims2[0] + 1;
            this.conc_times.extend(dims);
        }

        {
            long[] selected = this.concs.getSelectedDims();
            long[] start = this.concs.getStartDims();
            selected[0] = 1;
            selected[1] = dims[1];
            selected[2] = dims[2];
            start[0] = dims[0] - 1;

            double[] data = (double[]) this.concs.getData();
            this.getGridNumbers(data, nel, ispecout, source);
            this.concs.write(data);
        }

        {
            long[] selected = this.conc_times.getSelectedDims();
            long[] start = this.conc_times.getStartDims();
            selected[0] = 1;
            start[0] = dims[0] - 1;
            double[] times = (double[]) this.conc_times.getData();
            times[0] = time;
            this.conc_times.write(times);
        }
    }

    @Override
    public void writeGridConcsDumb(int i, double time, int nel, String fnamepart, IGridCalc source) {}

    @Override
    public void saveState(double time, String prefix, String state) {}
}
