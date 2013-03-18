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
import static org.textensor.util.ArrayUtil.xJoined;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ResultWriterHDF5 implements ResultWriter {
    static final Logger log = LogManager.getLogger(ResultWriterHDF5.class);

    final protected File outputFile;
    protected H5File output;
    protected Group model;
    protected Group sim;
    protected H5ScalarDS concs = null;
    protected H5ScalarDS conc_times = null;
    protected H5ScalarDS species = null;
    protected H5ScalarDS reaction_events = null;
    protected H5ScalarDS diffusion_events = null;

    public static final H5Datatype double_t =
        new H5Datatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype int_t =
        new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);

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
        this.model = this.output.createGroup("model", root);
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
    public void writeGrid(VolumeGrid vgrid, double startTime, String fnmsOut[], IGridCalc source)
    {
        try {
            this._writeGrid(vgrid, startTime, fnmsOut, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void _writeGrid(VolumeGrid vgrid, double startTime,
                              String fnmsOut[], IGridCalc source)
        throws Exception
    {
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

        this.output.createCompoundDS("grid", this.model, dims, null, chunks, gzip,
                                     memberNames, memberTypes, null, data);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 "grid", xJoined(dims), "", xJoined(chunks));

        writeArray("neighbors", this.model, vgrid.getPerElementNeighbors(), -1);
        writeArray("couplings", this.model, vgrid.getPerElementCouplingConstants());
    }

    public void writeArray(String name, Group parent, double[][] items)
        throws Exception
    {
        int maxlength = ArrayUtil.maxLength(items);
        long[] dims = {items.length, maxlength};

        double[] flat = ArrayUtil.flatten(items, maxlength);

        this.output.createScalarDS(name, parent,
                                   double_t, dims, null, null,
                                   0, flat);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
    }

    public void writeArray(String name, Group parent, int[][] items, int fill)
        throws Exception
    {
        int maxlength = ArrayUtil.maxLength(items);
        long[] dims = {items.length, maxlength};

        int[] flat = ArrayUtil.flatten(items, maxlength, fill);

        this.output.createScalarDS(name, parent,
                                   int_t, dims, null, null,
                                   0, flat);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
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
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
    }

    protected void writeSpecies(int[] ispecout, IGridCalc source)
        throws Exception
    {
        String[] specieIDs = source.getSpecieIDs();
        String[] outSpecies = new String[ispecout.length];
        for (int i = 0; i < ispecout.length; i++)
            outSpecies[i] = specieIDs[ispecout[i]];

        this.writeVector("species", this.model, outSpecies);
    }

    protected void writeRegionLabels(IGridCalc source)
        throws Exception
    {
        String[] regions = source.getRegionLabels();
        this.writeVector("regions", this.model, regions);
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
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "concentrations", xJoined(dims), xJoined(size), xJoined(chunks));
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
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "times", xJoined(dims), xJoined(size), xJoined(chunks));
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
        try {
            this._writeGridConcs(time, nel, ispecout, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void _writeGridConcs(double time, int nel, int ispecout[], IGridCalc source)
        throws Exception
    {
        log.info("Writing concentrations at time {}", time);

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

        this.writeReactionEvents(time, source);
        this.writeDiffusionEvents(time, source);
    }

    protected void initReactionEvents(int reactions)
        throws Exception
    {
        assert this.reaction_events == null;

        /* times x reactions */
        {
            long[] dims = {1, reactions};
            long[] size = {H5F_UNLIMITED, reactions};
            long[] chunks = {32, reactions};

            this.reaction_events = (H5ScalarDS)
                this.output.createScalarDS("reaction_events", this.sim,
                                           int_t, dims, size, chunks,
                                           9, null);
            this.reaction_events.init();
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "reaction_events", xJoined(dims), xJoined(size), xJoined(chunks));
        }
    }

    protected void writeReactionEvents(double time, IGridCalc source)
        throws Exception
    {
        final int[] events = source.getReactionEvents();
        if (events == null)
            return;

        log.debug("Writing reaction events at time {}", time);

        final long[] dims;
        if (this.reaction_events == null) {
            this.initReactionEvents(events.length);
            dims = this.reaction_events.getDims();
        } else {
            dims = this.reaction_events.getDims();
            dims[0] = dims[0] + 1;
            this.reaction_events.extend(dims);
        }

        {
            long[] selected = this.reaction_events.getSelectedDims();
            long[] start = this.reaction_events.getStartDims();
            selected[0] = 1;
            selected[1] = dims[1];
            start[0] = dims[0] - 1;

            int[] data = (int[]) this.reaction_events.getData();
            System.arraycopy(events, 0, data, 0, events.length);
            this.reaction_events.write(data);
        }
    }

    protected void initDiffusionEvents(int elements, int neighbors, int species)
        throws Exception
    {
        assert this.diffusion_events == null;

        /* times x reactions */
        {
            long[] dims = {1, elements, species, neighbors};
            long[] size = {H5F_UNLIMITED, elements, species, neighbors};
            long[] chunks = {4, elements, species, neighbors};

            this.diffusion_events = (H5ScalarDS)
                this.output.createScalarDS("diffusion_events", this.sim,
                                           int_t, dims, size, chunks,
                                           9, null);
            this.diffusion_events.init();
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "diffusion_events", xJoined(dims), xJoined(size), xJoined(chunks));
        }
    }

    protected void writeDiffusionEvents(double time, IGridCalc source)
        throws Exception
    {
        final int[][][] events = source.getDiffusionEvents();
        if (events == null)
            return;

        log.debug("Writing diffusion events at time {}", time);

        final long[] dims;
        if (this.diffusion_events == null) {
            int maxneighbors = ArrayUtil.maxLength(events);
            this.initDiffusionEvents(events.length, events[0].length,
                                     maxneighbors);
            dims = this.diffusion_events.getDims();
        } else {
            dims = this.diffusion_events.getDims();
            dims[0] = dims[0] + 1;
            this.diffusion_events.extend(dims);
        }

        {
            long[] selected = this.diffusion_events.getSelectedDims();
            long[] start = this.diffusion_events.getStartDims();
            selected[0] = 1;
            selected[1] = dims[1];
            selected[2] = dims[2];
            selected[3] = dims[3];
            start[0] = dims[0] - 1;

            int[] data = (int[]) this.diffusion_events.getData();
            ArrayUtil.flatten(data, events, (int) dims[2], 0);
            this.diffusion_events.write(data);
        }
    }

    @Override
    public void writeGridConcsDumb(int i, double time, int nel, String fnamepart, IGridCalc source) {}

    @Override
    public void saveState(double time, String prefix, String state) {}
}
