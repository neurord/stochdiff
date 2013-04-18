package org.textensor.stochdiff.numeric.grid;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;

import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_UNLIMITED;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Datatype;
import ncsa.hdf.object.h5.H5ScalarDS;

import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
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
    protected H5ScalarDS times = null;
    protected H5ScalarDS species = null;
    protected H5ScalarDS stimulation_events = null;
    protected H5ScalarDS diffusion_events = null;
    protected H5ScalarDS reaction_events = null;
    protected Dataset saved_state = null;

    public static final H5Datatype double_t =
        new H5Datatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype int_t =
        new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype short_str_t =
        new H5Datatype(Datatype.CLASS_STRING, 100, Datatype.NATIVE, Datatype.NATIVE);

    public ResultWriterHDF5(File outFile) {
        this.outputFile = new File(FileUtil.getRootName(outFile) + ".h5");
        log.debug("Writing HDF5 to {}", this.outputFile);
    }

    @Override
    public void init(String magic) {
        try {
            this._init();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void _init()
        throws Exception
    {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        assert fileFormat != null;
        log.debug("Opening output file {}", this.outputFile);
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
        setAttribute(this.model, "TITLE", "model parameters");
        this.sim = this.output.createGroup("simulation", root);
        setAttribute(this.sim, "TITLE", "results of the simulation");
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
        log.debug("Writing grid at time {}", startTime);
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

        Dataset grid =
            this.output.createCompoundDS("grid", this.model, dims, null, chunks, gzip,
                                         memberNames, memberTypes, null, data);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 "grid", xJoined(dims), "", xJoined(chunks));
        setAttribute(grid, "TITLE", "voxels");
        setAttribute(grid, "LAYOUT", "[nel x {xyz, xyz, xyz, xyz, volume, deltaZ}]");

        {
            Dataset ds =
                writeArray("neighbors", this.model, vgrid.getPerElementNeighbors(), -1);
            setAttribute(ds, "TITLE", "adjacency mapping between voxels");
            setAttribute(ds, "LAYOUT", "[nel x neighbors*]");
            setAttribute(ds, "UNITS", "indexes");
        }
        {
            Dataset ds =
                writeArray("couplings", this.model, vgrid.getPerElementCouplingConstants());
            setAttribute(ds, "TITLE", "coupling rate between voxels");
            setAttribute(ds, "LAYOUT", "[nel x neighbors*]");
            setAttribute(ds, "UNITS", "nm^2 / nm ?");
        }

        this.writeStimulationData(source);
        this.writeReactionData(source);
    }

    protected void setAttribute(HObject obj, String name, String value)
        throws Exception
    {
        Attribute attr = new Attribute(name, short_str_t,
                                       new long[] {}, new String[] {value});
        obj.writeMetadata(attr);
        log.debug("Wrote metadata on {} {}={}", obj, name, value);
    }

    public Dataset writeArray(String name, Group parent, double[][] items)
        throws Exception
    {
        int maxlength = ArrayUtil.maxLength(items);
        long[] dims = {items.length, maxlength};

        double[] flat = ArrayUtil.flatten(items, maxlength);

        Dataset ds = this.output.createScalarDS(name, parent,
                                                double_t, dims, null, null,
                                                0, flat);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
        return ds;
    }

    public Dataset writeArray(String name, Group parent, int[][] items, int fill)
        throws Exception
    {
        int maxlength = ArrayUtil.maxLength(items);
        long[] dims = {items.length, maxlength};

        int[] flat = ArrayUtil.flatten(items, maxlength, fill);

        Dataset ds = this.output.createScalarDS(name, parent,
                                                int_t, dims, null, null,
                                                0, flat);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
        return ds;
    }

    public Dataset writeVector(String name, Group parent, String[] items)
        throws Exception
    {
        int maxlength = ArrayUtil.maxLength(items);
        long[] dims = {items.length};

        H5Datatype string_t = new H5Datatype(Datatype.CLASS_STRING, maxlength,
                                             Datatype.NATIVE, Datatype.NATIVE);

        Dataset ds = this.output.createScalarDS(name, parent,
                                                string_t, dims, null, null,
                                                0, items);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
        return ds;
    }

    public Dataset writeVector(String name, Group parent, double[] items)
        throws Exception
    {
        long[] dims = {items.length};

        Dataset ds = this.output.createScalarDS(name, parent,
                                                double_t, dims, null, null,
                                                0, items);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
        return ds;
    }

    protected void writeSpecies(int[] ispecout, IGridCalc source)
        throws Exception
    {
        String[] specieIDs = source.getSpecieIDs();
        String[] outSpecies = new String[ispecout.length];
        for (int i = 0; i < ispecout.length; i++)
            outSpecies[i] = specieIDs[ispecout[i]];

        Dataset ds = this.writeVector("species", this.model, outSpecies);
        setAttribute(ds, "TITLE", "names of saved species");
        setAttribute(ds, "LAYOUT", "[nspecies]");
        setAttribute(ds, "UNITS", "text");
    }

    protected void writeRegionLabels(IGridCalc source)
        throws Exception
    {
        String[] regions = source.getRegionLabels();
        Dataset ds = this.writeVector("regions", this.model, regions);
        setAttribute(ds, "TITLE", "names of regions");
        setAttribute(ds, "LAYOUT", "[nregions]");
        setAttribute(ds, "UNITS", "text");
    }

    protected void writeStimulationData(IGridCalc source)
        throws Exception
    {
        StimulationTable table = source.getStimulationTable();

        Group group = this.output.createGroup("stimulation", this.model);
        setAttribute(group, "TITLE", "stimulation parameters");

        {
            String[] targets = table.getTargetIDs();
            if (targets.length == 0) {
                log.info("not writing stimulation data (empty targets)");
                return;
            }

            Dataset ds = this.writeVector("target_names", group, targets);
            setAttribute(ds, "TITLE", "names of stimulation targets");
            setAttribute(ds, "LAYOUT", "[nstimulations]");
            setAttribute(ds, "UNITS", "text");
        }

        {
            int[][] targets = source.getStimulationTargets();
            Dataset ds = this.writeArray("targets", group, targets, -1);
            setAttribute(ds, "TITLE", "stimulated voxels");
            setAttribute(ds, "LAYOUT", "[??? x ???]");
            setAttribute(ds, "UNITS", "indexes");
        }
    }

    protected void writeReactionData(IGridCalc source)
        throws Exception
    {
        ReactionTable table = source.getReactionTable();

        Group group = this.output.createGroup("reactions", this.model);
        setAttribute(group, "TITLE", "reaction scheme");

        {
            int[][] indices = table.getReactantIndices();
            Dataset ds = this.writeArray("reactants", group, indices, -1);
            setAttribute(ds, "TITLE", "reactant indices");
            setAttribute(ds, "LAYOUT", "[nreact x nreactants*]");
            setAttribute(ds, "UNITS", "indexes");
        }
        {
            int[][] indices = table.getProductIndices();
            Dataset ds = this.writeArray("products", group, indices, -1);
            setAttribute(ds, "TITLE", "product indices");
            setAttribute(ds, "LAYOUT", "[nreact x nproducts*]");
            setAttribute(ds, "UNITS", "indexes");
        }
        {
            int[][] stochio = table.getReactantStochiometry();
            Dataset ds = this.writeArray("reactant_stochiometry", group, stochio, -1);
            setAttribute(ds, "TITLE", "reactant stochiometry");
            setAttribute(ds, "LAYOUT", "[nreact x nreactants*]");
            setAttribute(ds, "UNITS", "indexes");
        }
        {
            int[][] stochio = table.getProductStochiometry();
            Dataset ds = this.writeArray("product_stochiometry", group, stochio, -1);
            setAttribute(ds, "TITLE", "product stochiometry");
            setAttribute(ds, "LAYOUT", "[nreact x nproducts*]");
            setAttribute(ds, "UNITS", "indexes");
        }

        {
            double[] rates = table.getRates();
            Dataset ds = this.writeVector("rates", group, rates);
            setAttribute(ds, "TITLE", "reaction rates");
            setAttribute(ds, "LAYOUT", "[nreact]");
            setAttribute(ds, "UNITS", "transitions/ms");
        }
    }


    protected boolean initConcs(int nel, int[] ispecout, IGridCalc source)
        throws Exception
    {
        assert this.concs == null;
        assert this.times == null;

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
            setAttribute(this.concs, "TITLE", "concentrations of species in voxels over time");
            setAttribute(this.concs, "LAYOUT", "[snapshot x nel x nspecout]");
            setAttribute(this.concs, "UNITS", "count");
        }

        {
            long[] dims = {1};
            long[] size = {H5F_UNLIMITED};
            long[] chunks = {1024};
            double[] times = {0.0};

            this.times = (H5ScalarDS)
                this.output.createScalarDS("times", this.sim,
                                           double_t, dims, size, chunks,
                                           9, times);
            this.times.init();
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "times", xJoined(dims), xJoined(size), xJoined(chunks));
            setAttribute(this.times, "TITLE", "times when snapshots were written");
            setAttribute(this.times, "LAYOUT", "[times]");
            setAttribute(this.times, "UNITS", "ms");
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
        log.debug("Writing concentrations at time {}", time);

        final long[] dims;
        if (this.concs == null) {
            if (!this.initConcs(nel, ispecout, source))
                return;
            dims = this.concs.getDims();;
        } else {
            dims = this.concs.getDims();
            dims[0] = dims[0] + 1;
            this.concs.extend(dims);

            long[] dims2 = this.times.getDims();
            dims2[0] = dims2[0] + 1;
            this.times.extend(dims);
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
            long[] selected = this.times.getSelectedDims();
            long[] start = this.times.getStartDims();
            selected[0] = 1;
            start[0] = dims[0] - 1;
            double[] times = (double[]) this.times.getData();
            times[0] = time;
            this.times.write(times);
        }

        this.writeStimulationEvents(time, source);
        this.writeDiffusionEvents(time, source);
        this.writeReactionEvents(time, source);
    }

    protected void initStimulationEvents(int elements, int species)
        throws Exception
    {
        assert this.stimulation_events == null;

        /* times x elements x species */
        {
            long[] dims = {1, elements, species};
            long[] size = {H5F_UNLIMITED, elements, species};
            long[] chunks = {32, elements, species};

            H5ScalarDS ds =  (H5ScalarDS)
                this.output.createScalarDS("stimulation_events", this.sim,
                                           int_t, dims, size, chunks,
                                           9, null);
            ds.init();
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "stimulation_events",
                     xJoined(dims), xJoined(size), xJoined(chunks));
            setAttribute(ds, "TITLE", "actual stimulation counts since last snapshot");
            setAttribute(ds, "LAYOUT", "[times x elements x species]");
            setAttribute(ds, "UNITS", "count");
            this.stimulation_events = ds;
        }
    }

    protected void writeStimulationEvents(double time, IGridCalc source)
        throws Exception
    {
        final int[][] events = source.getStimulationEvents();
        if (events == null)
            return;

        final long[] dims;
        if (this.stimulation_events == null) {
            this.initStimulationEvents(events.length, events[0].length);
            dims = this.stimulation_events.getDims();
        } else {
            dims = this.stimulation_events.getDims();
            dims[0] = dims[0] + 1;
            this.stimulation_events.extend(dims);
        }

        log.debug("Writing stimulation events at time {}", time);

        {
            long[] selected = this.stimulation_events.getSelectedDims();
            long[] start = this.stimulation_events.getStartDims();
            selected[0] = 1;
            selected[1] = dims[1];
            selected[2] = dims[2];
            start[0] = dims[0] - 1;

            int[] data = (int[]) this.stimulation_events.getData();
            ArrayUtil._flatten(data, events, dims[2], 0);
            this.stimulation_events.write(data);
        }
    }

    private boolean initDiffusionEvents_warning = false;

    protected boolean initDiffusionEvents(int elements, int species, int neighbors)
        throws Exception
    {
        assert this.diffusion_events == null;

        if (elements == 0 || species == 0 || neighbors == 0) {
            if (!initDiffusionEvents_warning) {
                log.info("Diffusion events are {}x{}x{}", elements, species, neighbors);
                log.warn("No diffusion events, not writing anything");
                initDiffusionEvents_warning = true;
            }
            return false;
        }

        /* times x reactions */
        {
            long[] dims = {1, elements, species, neighbors};
            long[] size = {H5F_UNLIMITED, elements, species, neighbors};
            long[] chunks = {4, elements, species, neighbors};

            H5ScalarDS ds = (H5ScalarDS)
                this.output.createScalarDS("diffusion_events", this.sim,
                                           int_t, dims, size, chunks,
                                           9, null);
            ds.init();
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "diffusion_events", xJoined(dims), xJoined(size), xJoined(chunks));
            setAttribute(ds, "TITLE", "actual diffusion counts since last snapshot");
            setAttribute(ds, "LAYOUT", "[times x nel x species x neighbors]");
            setAttribute(ds, "UNITS", "count");
            this.diffusion_events = ds;
        }

        return true;
    }

    protected void writeDiffusionEvents(double time, IGridCalc source)
        throws Exception
    {
        final int[][][] events = source.getDiffusionEvents();
        if (events == null)
            return;

        final long[] dims;
        if (this.diffusion_events == null) {
            int maxneighbors = ArrayUtil.maxLength(events);
            boolean have = this.initDiffusionEvents(events.length, events[0].length,
                                                    maxneighbors);
            if (!have)
                return;
            dims = this.diffusion_events.getDims();
        } else {
            dims = this.diffusion_events.getDims();
            dims[0] = dims[0] + 1;
            this.diffusion_events.extend(dims);
        }

        log.debug("Writing diffusion events at time {}", time);

        {
            long[] selected = this.diffusion_events.getSelectedDims();
            long[] start = this.diffusion_events.getStartDims();
            selected[0] = 1;
            selected[1] = dims[1];
            selected[2] = dims[2];
            selected[3] = dims[3];
            start[0] = dims[0] - 1;

            int[] data = (int[]) this.diffusion_events.getData();
            ArrayUtil._flatten(data, events, dims[3], 0);
            this.diffusion_events.write(data);
        }
    }

    private boolean initReactionEvents_warning = false;

    protected boolean initReactionEvents(int elements, int reactions)
        throws Exception
    {
        assert this.reaction_events == null;

        if (elements == 0 || reactions == 0) {
            if (!initReactionEvents_warning) {
                log.warn("No reaction events, not writing anything");
                initReactionEvents_warning = true;
            }
            return false;
        }

        /* times x reactions */
        {
            long[] dims = {1, elements, reactions};
            long[] size = {H5F_UNLIMITED, elements, reactions};
            long[] chunks = {32, elements, reactions};

            H5ScalarDS ds =  (H5ScalarDS)
                this.output.createScalarDS("reaction_events", this.sim,
                                           int_t, dims, size, chunks,
                                           9, null);
            ds.init();
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "reaction_events", xJoined(dims), xJoined(size), xJoined(chunks));
            setAttribute(ds, "TITLE", "actual reaction counts since last snapshot");
            setAttribute(ds, "LAYOUT", "[times x elements x reactions]");
            setAttribute(ds, "UNITS", "count");
            this.reaction_events = ds;
        }

        return true;
    }

    protected void writeReactionEvents(double time, IGridCalc source)
        throws Exception
    {
        final int[][] events = source.getReactionEvents();
        if (events == null)
            return;

        final long[] dims;
        if (this.reaction_events == null) {
            boolean have = this.initReactionEvents(events.length, events[0].length);
            if (!have)
                return;
            dims = this.reaction_events.getDims();
        } else {
            dims = this.reaction_events.getDims();
            dims[0] = dims[0] + 1;
            this.reaction_events.extend(dims);
        }

        log.debug("Writing reaction events at time {}", time);

        {
            long[] selected = this.reaction_events.getSelectedDims();
            long[] start = this.reaction_events.getStartDims();
            selected[0] = 1;
            selected[1] = dims[1];
            selected[2] = dims[2];
            start[0] = dims[0] - 1;

            int[] data = (int[]) this.reaction_events.getData();
            ArrayUtil._flatten(data, events, dims[2], 0);
            this.reaction_events.write(data);
        }
    }


    @Override
    public void writeGridConcsDumb(int i, double time, int nel, String fnamepart, IGridCalc source) {}

    protected void writeSavedStateI(int nel, int nspecie, IGridCalc source)
        throws Exception
    {
        int[][] state = {};
        if (this.saved_state == null) {
            Dataset ds = this.writeArray("state", this.sim, state, -1);
            setAttribute(ds, "TITLE", "saved state");
            setAttribute(ds, "LAYOUT", "[nelements x nspecies]");
            setAttribute(ds, "UNITS", "count");

            this.saved_state = ds;
        } else {
            int[] data = (int[]) this.saved_state.getData();
            int columns = state[0].length; /* should all be the same */
            ArrayUtil._flatten(data, state, columns, -1);
            this.saved_state.write(data);
        }
    }

    protected void writeSavedStateD(int nel, int nspecie, IGridCalc source)
        throws Exception
    {
        double[][] state = {};
        if (this.saved_state == null) {
            Dataset ds = this.writeArray("state", this.sim, state);
            setAttribute(ds, "TITLE", "saved state");
            setAttribute(ds, "LAYOUT", "[nelements x nspecies]");
            setAttribute(ds, "UNITS", "nm/l ?");

            this.saved_state = ds;
        } else {
            double[] data = (double[]) this.saved_state.getData();
            int columns = state[0].length; /* should all be the same */
            ArrayUtil._flatten(data, state, columns);
            this.saved_state.write(data);
        }
    }

    @Override
    public void saveState(double time, String prefix, IGridCalc source) {
        log.debug("state saved at t={} ms", time);
        try {
            int nel = source.getNumberElements();
            int nspecie = source.getSpecieIDs().length;
            if (source.preferConcs())
                this.writeSavedStateD(nel, nspecie, source);
            else
                this.writeSavedStateI(nel, nspecie, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object _loadState(String filename, IGridCalc source)
        throws Exception
    {
        // FIXME: This is totally not going to work, because we delete
        // the file on creation...
        Dataset obj = (Dataset) this.output.get("/simulation/state");
        int nel = source.getNumberElements();
        int nspecie = source.getSpecieIDs().length;
        if (obj == null)
            throw new RuntimeException("state hasn't been saved");
        if (source.preferConcs()) {
            double[] data = (double[]) obj.getData();
            return ArrayUtil.shape(data, nel, nspecie);
        } else {
            int[] data = (int[]) obj.getData();
            return ArrayUtil.shape(data, nel, nspecie);
        }
    }

    @Override
    public Object loadState(String filename, IGridCalc source) {
        try {
            return _loadState(filename, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
