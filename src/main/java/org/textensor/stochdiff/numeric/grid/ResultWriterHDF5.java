package org.textensor.stochdiff.numeric.grid;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.Vector;
import java.util.Collection;
import java.util.Map;
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
import org.textensor.util.ArrayUtil;
import static org.textensor.util.ArrayUtil.xJoined;
import org.textensor.util.LibUtil;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ResultWriterHDF5 implements ResultWriter {
    static final Logger log = LogManager.getLogger(ResultWriterHDF5.class);

    static {
        LibUtil.addLibraryPaths("/usr/lib64/jhdf",
                                "/usr/lib64/jhdf5",
                                "/usr/lib/jhdf",
                                "/usr/lib/jhdf5");
    }

    final protected File outputFile;
    protected H5File output;
    protected Group root;
    final protected Map<Integer, Trial> trials = inst.newHashMap();

    public static final H5Datatype double_t =
        new H5Datatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype int_t =
        new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype short_str_t =
        new H5Datatype(Datatype.CLASS_STRING, 100, Datatype.NATIVE, Datatype.NATIVE);

    static final int CACHE_SIZE1 = 1024;
    static final int CACHE_SIZE2 = 1024*1024;

    public ResultWriterHDF5(File output) {
        this.outputFile = new File(output + ".h5");
        log.debug("Writing HDF5 to {}", this.outputFile);
    }

    private int users = 0;

    @Override
    synchronized public void init(String magic) {
        if (users++ > 0)
            return;

        try {
            this._init();
        } catch(UnsatisfiedLinkError e) {
            log.warn("java.library.path: {}", System.getProperty("java.library.path"));
            throw new RuntimeException(e);
        } catch(Exception e) {
            log.warn("java.library.path: {}", System.getProperty("java.library.path"));
            throw new RuntimeException(e);
        }
    }

    protected void _init()
        throws Exception
    {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        if (fileFormat == null)
            throw new UnsatisfiedLinkError("hdf5");

        log.debug("Opening output file {}", this.outputFile);
        this.output = (H5File) fileFormat.create(this.outputFile.toString());
        assert this.output != null;

        try {
            this.output.open();
        } catch(Exception e) {
            log.error("Failed to open results file {}", this.outputFile);
            throw e;
        }

        this.root = (Group)((DefaultMutableTreeNode) this.output.getRootNode()).getUserObject();
    }

    @Override
    synchronized public void close() {
        if (--users > 0)
            return;

        log.info("Closing output file {}", this.outputFile);
        try {
            this.output.close();
        } catch(Exception e) {
            log.error("Failed to close results file {}", outputFile, e);
        }
    }

    @Override
    public File outputFile() {
        return this.outputFile;
    }

    protected Trial getTrial(int trial)
        throws Exception
    {
        Trial t = this.trials.get(trial);
        if (t == null) {
            t = this.createTrial(trial);
            Trial old = this.trials.put(trial, t);
            assert old == null;
        }
        return t;
    }

    protected Trial createTrial(int trial)
        throws Exception
    {
        String name = "trial" + trial;
        Group group = this.output.createGroup(name, this.root);
        setAttribute(group, "TITLE", "trial " + trial);
        return new Trial(group);
    }

    @Override
    synchronized public void writeGrid(VolumeGrid vgrid, double startTime, String fnmsOut[],
                                       IGridCalc source)
    {
        try {
            Trial t = this.getTrial(source.trial());
            t._writeGrid(vgrid, startTime, fnmsOut, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    synchronized public void writeGridConcs(double time, int nel, int ispecout[], IGridCalc source) {
        try {
            Trial t = this.getTrial(source.trial());
            t._writeGridConcs(time, nel, ispecout, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    synchronized public void writeGridConcsDumb(int i, double time, int nel, String fnamepart,
                                                IGridCalc source) {}

    @Override
    synchronized public void saveState(double time, String prefix, IGridCalc source) {
        try {
            Trial t = this.getTrial(source.trial());
            t._saveState(time, prefix, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    synchronized public Object loadState(String filename, IGridCalc source) {
        try {
            Trial t = this.getTrial(source.trial());
            return t._loadState(filename, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected class Trial {
        protected final Group group;
        protected final Group model;
        protected final Group sim;
        protected H5ScalarDS concs;
        protected double[] concs_data;
        protected H5ScalarDS times;
        protected double[] times_data;
        protected int concs_times_count;
        protected H5ScalarDS species;
        protected H5ScalarDS stimulation_events;
        protected H5ScalarDS diffusion_events;
        protected H5ScalarDS reaction_events;
        protected Group events;
        protected List<IGridCalc.Event> events_cache;
        protected H5ScalarDS
            events_event, events_type, events_kind,
            events_extent, events_time, events_waited;
        protected Dataset saved_state = null;

        public Trial(Group group)
            throws Exception
        {
            this.group = group;

            this.model = output.createGroup("model", group);
            setAttribute(this.model, "TITLE", "model parameters");

            this.sim = output.createGroup("simulation", group);
            setAttribute(this.sim, "TITLE", "results of the simulation");
        }

        protected void _writeGrid(VolumeGrid vgrid, double startTime,
                                  String fnmsOut[], IGridCalc source)
            throws Exception
        {
            log.debug("Writing grid at time {} for trial {}", startTime, source.trial());
            int n = vgrid.getNElements();
            long[]
                dims = {n,},
                chunks = {n,};
                int gzip = 6;
                String[] memberNames = { "x0", "y0", "z0",
                                         "x1", "y1", "z1",
                                         "x2", "y2", "z2",
                                         "x3", "y3", "z3",
                                         "volume", "deltaZ",
                                         "label",
                                         "region", "type", "group" };

                Datatype[] memberTypes = new Datatype[memberNames.length];
                Arrays.fill(memberTypes, double_t);
                memberTypes[14] = short_str_t;
                memberTypes[15] = int_t;
                memberTypes[16] = short_str_t;
                memberTypes[17] = short_str_t;
                assert memberTypes.length == 18;

                Vector<Object> data = vgrid.gridData();

                {
                    String[] labels = new String[n];
                    for (int i = 0; i < n; i++) {
                        labels[i] = vgrid.getLabel(i);
                        if (labels[i] == null)
                            labels[i] = "element" + i;
                    }
                    data.add(labels);
                }

                {
                    int[] indexes = vgrid.getRegionIndexes();
                    assert indexes.length == n;
                    data.add(indexes);
                }

                {
                    boolean[] membranes = vgrid.getSubmembranes();
                    assert membranes.length == n;
                    String[] types = new String[n];
                    for (int i = 0; i < n; i++)
                        types[i] = membranes[i] ? "submembrane" : "cytosol";
                    data.add(types);
                }

                {
                    String[] labels = new String[n];
                    for (int i = 0; i < n; i++) {
                        labels[i] = vgrid.getGroupID(i);
                        if (labels[i] == null)
                            labels[i] = "";
                    }
                    data.add(labels);
                }

                Dataset grid =
                    output.createCompoundDS("grid", this.model, dims, null, chunks, gzip,
                                            memberNames, memberTypes, null, data);
                log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                         "grid", xJoined(dims), "", xJoined(chunks));
                setAttribute(grid, "TITLE", "voxels");
                setAttribute(grid, "LAYOUT",
                             "[nel × {x,y,z, x,y,z, x,y,z, x,y,z, volume, deltaZ, label, region#, type, group}]");

                {
                    Dataset ds =
                        writeArray("neighbors", this.model, vgrid.getPerElementNeighbors(), -1);
                    setAttribute(ds, "TITLE", "adjacency mapping between voxels");
                    setAttribute(ds, "LAYOUT", "[nel × neighbors*]");
                    setAttribute(ds, "UNITS", "indexes");
                }
                {
                    Dataset ds =
                        writeArray("couplings", this.model, vgrid.getPerElementCouplingConstants());
                    setAttribute(ds, "TITLE", "coupling rate between voxels");
                    setAttribute(ds, "LAYOUT", "[nel × neighbors*]");
                    setAttribute(ds, "UNITS", "nm^2 / nm ?");
                }

                this.writeStimulationData(source);
                this.writeReactionData(source);
        }

        protected void writeSpecies(int[] ispecout, IGridCalc source)
            throws Exception
        {
            String[] specieIDs = source.getSpecieIDs();
            String[] outSpecies = new String[ispecout.length];
            for (int i = 0; i < ispecout.length; i++)
                outSpecies[i] = specieIDs[ispecout[i]];

            Dataset ds = writeVector("species", this.model, outSpecies);
            setAttribute(ds, "TITLE", "names of saved species");
            setAttribute(ds, "LAYOUT", "[nspecies]");
            setAttribute(ds, "UNITS", "text");
        }

        protected void writeRegionLabels(IGridCalc source)
            throws Exception
        {
            String[] regions = source.getVolumeGrid().getRegionLabels();
            Dataset ds = writeVector("regions", this.model, regions);
            setAttribute(ds, "TITLE", "names of regions");
            setAttribute(ds, "LAYOUT", "[nregions]");
            setAttribute(ds, "UNITS", "text");
        }

        protected void writeStimulationData(IGridCalc source)
            throws Exception
        {
            StimulationTable table = source.getStimulationTable();

            Group group = output.createGroup("stimulation", this.model);
            setAttribute(group, "TITLE", "stimulation parameters");

            {
                String[] targets = table.getTargetIDs();
                if (targets.length == 0) {
                    log.info("not writing stimulation data (empty targets)");
                    return;
                }

                Dataset ds = writeVector("target_names", group, targets);
                setAttribute(ds, "TITLE", "names of stimulation targets");
                setAttribute(ds, "LAYOUT", "[nstimulations]");
                setAttribute(ds, "UNITS", "text");
            }

            {
                int[][] targets = source.getStimulationTargets();
                Dataset ds = writeArray("targets", group, targets, -1);
                setAttribute(ds, "TITLE", "stimulated voxels");
                setAttribute(ds, "LAYOUT", "[??? × ???]");
                setAttribute(ds, "UNITS", "indexes");
            }
        }

        protected void writeReactionData(IGridCalc source)
            throws Exception
        {
            ReactionTable table = source.getReactionTable();

            Group group = output.createGroup("reactions", this.model);
            setAttribute(group, "TITLE", "reaction scheme");

            {
                int[][] indices = table.getReactantIndices();
                Dataset ds = writeArray("reactants", group, indices, -1);
                setAttribute(ds, "TITLE", "reactant indices");
                setAttribute(ds, "LAYOUT", "[nreact × nreactants*]");
                setAttribute(ds, "UNITS", "indexes");
            }
            {
                int[][] indices = table.getProductIndices();
                Dataset ds = writeArray("products", group, indices, -1);
                setAttribute(ds, "TITLE", "product indices");
                setAttribute(ds, "LAYOUT", "[nreact × nproducts*]");
                setAttribute(ds, "UNITS", "indexes");
            }
            {
                int[][] stochio = table.getReactantStochiometry();
                Dataset ds = writeArray("reactant_stochiometry", group, stochio, -1);
                setAttribute(ds, "TITLE", "reactant stochiometry");
                setAttribute(ds, "LAYOUT", "[nreact × nreactants*]");
                setAttribute(ds, "UNITS", "indexes");
            }
            {
                int[][] stochio = table.getProductStochiometry();
                Dataset ds = writeArray("product_stochiometry", group, stochio, -1);
                setAttribute(ds, "TITLE", "product stochiometry");
                setAttribute(ds, "LAYOUT", "[nreact × nproducts*]");
                setAttribute(ds, "UNITS", "indexes");
            }

            {
                double[] rates = table.getRates();
                Dataset ds = writeVector("rates", group, rates);
                setAttribute(ds, "TITLE", "reaction rates");
                setAttribute(ds, "LAYOUT", "[nreact]");
                setAttribute(ds, "UNITS", "transitions/ms");
            }
        }

        public void _writeGridConcs(double time, int nel, int ispecout[], IGridCalc source)
            throws Exception
        {
            log.debug("Saving stats at time {} for trial {}", time, source.trial());

            this.writeConcentrations(time, nel, ispecout, source);
            this.writeStimulationEvents(time, source);
            this.writeDiffusionEvents(time, source);
            this.writeReactionEvents(time, source);
            this.writeEvents(time, source);
        }

        protected boolean initConcentrations(int nel, int[] ispecout, IGridCalc source)
            throws Exception
        {
            assert this.concs == null;
            assert this.times == null;

            this.writeSpecies(ispecout, source);
            this.writeRegionLabels(source);

            int nspecout = ispecout.length;
            if (nspecout == 0)
                return false;

            /* times × nel × nspecout, but we write only for only time 'time' at one time */
            this.concs = createExtensibleArray("concentrations", this.sim, double_t,
                                               "concentrations of species in voxels over time",
                                               "[snapshot × nel × nspecout]",
                                               "count",
                                               CACHE_SIZE1, nel, nspecout);

            this.times = createExtensibleArray("times", this.sim, double_t,
                                               "times when snapshots were written",
                                               "[times]",
                                               "ms",
                                               CACHE_SIZE1);
            this.resetTimesConcs();
            return true;
        }

        protected void resetTimesConcs()
            throws Exception
        {
            extendExtensibleArray(this.concs, CACHE_SIZE1);
            extendExtensibleArray(this.times, CACHE_SIZE1);
            this.concs_data = (double[]) this.concs.getData();
            this.times_data = (double[]) this.times.getData();
            this.concs_times_count = 0;
        }

        protected void writeConcentrations(double time, int nel, int ispecout[], IGridCalc source)
            throws Exception
        {
            if (this.concs == null)
                if (!this.initConcentrations(nel, ispecout, source))
                    return;

            getGridNumbers(this.concs_data, this.concs_times_count, nel, ispecout, source);
            this.times_data[this.concs_times_count] = time;

            if (this.concs_times_count++ == this.times_data.length) {
                log.debug("Writing stats at time {} for trial {}", time, source.trial());
                this.concs.write(this.concs_data);
                this.times.write(times);
                this.resetTimesConcs();
            }
        }

        protected void initStimulationEvents(int elements, int species)
            throws Exception
        {
            assert this.stimulation_events == null;

            /* times × elements × species */
            this.stimulation_events =
                createExtensibleArray("stimulation_events", this.sim, int_t,
                                      "actual stimulation counts since last snapshot",
                                      "[times × elements × species]",
                                      "count",
                                      CACHE_SIZE1, elements, species);
        }

        protected void writeStimulationEvents(double time, IGridCalc source)
            throws Exception
        {
            final int[][] events = source.getStimulationEvents();
            if (events == null)
                return;

            if (this.stimulation_events == null)
                this.initStimulationEvents(events.length, events[0].length);

            log.debug("Writing stimulation events at time {}", time);
            {
                extendExtensibleArray(this.stimulation_events, 1);
                long[] dims = this.stimulation_events.getDims();
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
                    log.info("Diffusion events are {}×{}×{}", elements, species, neighbors);
                    log.warn("No diffusion events, not writing anything");
                    initDiffusionEvents_warning = true;
                }
                return false;
            }

            /* times × reactions */
            this.diffusion_events =
                createExtensibleArray("diffusion_events", this.sim, int_t,
                                      "actual diffusion counts since last snapshot",
                                      "[times × nel × species × neighbors]",
                                      "count",
                                      CACHE_SIZE1, elements, species, neighbors);
            return true;
        }

        protected void writeDiffusionEvents(double time, IGridCalc source)
            throws Exception
        {
            final int[][][] events = source.getDiffusionEvents();
            if (events == null) {
                boolean have = this.initDiffusionEvents(0, 0, 0);
                assert !have;
                return;
            }

            if (this.diffusion_events == null) {
                int maxneighbors = ArrayUtil.maxLength(events);
                boolean have = this.initDiffusionEvents(events.length, events[0].length,
                                                        maxneighbors);
                if (!have)
                    return;
            }

            log.debug("Writing diffusion events at time {}", time);

            {
                extendExtensibleArray(this.diffusion_events, 1);
                long dims[] = this.diffusion_events.getDims();
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

            /* times × reactions */
            this.reaction_events =
                createExtensibleArray("reaction_events", this.sim, int_t,
                                      "actual reaction counts since last snapshot",
                                      "[times × elements × reactions]",
                                      "count",
                                      CACHE_SIZE1, elements, reactions);

            return true;
        }

        protected void writeReactionEvents(double time, IGridCalc source)
            throws Exception
        {
            final int[][] events = source.getReactionEvents();
            if (events == null)
                return;

            if (this.reaction_events == null) {
                boolean have = this.initReactionEvents(events.length, events[0].length);
                if (!have)
                    return;
            }

            log.debug("Writing reaction events at time {}", time);
            {
                extendExtensibleArray(this.reaction_events, 1);
                long[] dims = this.reaction_events.getDims();
                int[] data = (int[]) this.reaction_events.getData();
                ArrayUtil._flatten(data, events, dims[2], 0);
                this.reaction_events.write(data);
            }
        }

        protected void initEvents()
            throws Exception
        {
            assert this.events == null;

            this.events = output.createGroup("events", this.sim);

            this.events_event = createExtensibleArray("events", this.events, int_t,
                                                      "index of the event that happened",
                                                      "[event#]",
                                                      "",
                                                      CACHE_SIZE2);

            this.events_type = createExtensibleArray("types", this.events, int_t,
                                                     "type of the event that happened",
                                                     "[type]",
                                                     "",
                                                     CACHE_SIZE2);

            this.events_kind = createExtensibleArray("kinds", this.events, int_t,
                                                     "mechanism of the event that happened",
                                                     "[kind]",
                                                     "",
                                                     CACHE_SIZE2);

            this.events_extent = createExtensibleArray("extents", this.events, int_t,
                                                       "the extent of the reaction or event",
                                                       "[extent]",
                                                       "count",
                                                       CACHE_SIZE2);
            this.events_time = createExtensibleArray("times", this.events, double_t,
                                                     "at what time the event happened",
                                                     "[extent]",
                                                     "ms",
                                                     CACHE_SIZE2);
            this.events_waited = createExtensibleArray("waited", this.events, double_t,
                                                       "time since the previous instance of this event",
                                                       "[waited]",
                                                       "ms",
                                                       CACHE_SIZE2);

            long chunk_size = this.events_event.getChunkSize()[0];
            this.events_cache = inst.newArrayList((int)chunk_size);
        }

        private boolean initEvents_warning = false;

        protected void writeEvents(double time, IGridCalc source)
            throws Exception
        {
            final Collection<IGridCalc.Event> events = source.getEvents();
            if (events == null) {
                if (!initEvents_warning) {
                    log.warn("No events, not writing anything");
                    initEvents_warning = true;
                }
                return;
            }

            if (this.events == null)
                this.initEvents();

            this.events_cache.addAll(events);

            int n = this.events_cache.size();
            n -= n % this.events_event.getChunkSize()[0];

            log.debug("Got {} events at time {}, writing {}", events.size(), time, n);
            if (n == 0)
                return;

            {
                extendExtensibleArray(this.events_event, n);
                int[] data = (int[]) this.events_event.getData();
                for (int i = 0; i < n; i++)
                    data[i] = this.events_cache.get(i).hashCode();
                this.events_event.write(data);
            }

            {
                extendExtensibleArray(this.events_type, n);
                int[] data = (int[]) this.events_type.getData();
                for (int i = 0; i < n; i++) {
                    data[i] = this.events_cache.get(i).type().ordinal();
                }
                this.events_type.write(data);
            }

            {
                extendExtensibleArray(this.events_kind, n);
                int[] data = (int[]) this.events_kind.getData();
                for (int i = 0; i < n; i++)
                    data[i] = this.events_cache.get(i).kind().ordinal();
                this.events_kind.write(data);
            }

            {
                extendExtensibleArray(this.events_extent, n);
                int[] data = (int[]) this.events_extent.getData();
                for (int i = 0; i < n; i++)
                    data[i] = this.events_cache.get(i).extent();
                this.events_extent.write(data);
            }

            {
                extendExtensibleArray(this.events_time, n);
                double[] data = (double[]) this.events_time.getData();
                for (int i = 0; i < n; i++)
                    data[i] = this.events_cache.get(i).time();
                this.events_time.write(data);
            }

            {
                extendExtensibleArray(this.events_waited, n);
                double[] data = (double[]) this.events_waited.getData();
                for (int i = 0; i < n; i++)
                    data[i] = this.events_cache.get(i).waited();
                this.events_waited.write(data);
            }

            this.events_cache = this.events_cache.subList(n, this.events_cache.size());
        }

        protected void writeSavedStateI(int nel, int nspecie, IGridCalc source)
            throws Exception
        {
            int[][] state = {};
            if (this.saved_state == null) {
                Dataset ds = writeArray("state", this.sim, state, -1);
                setAttribute(ds, "TITLE", "saved state");
                setAttribute(ds, "LAYOUT", "[nelements × nspecies]");
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
                Dataset ds = writeArray("state", this.sim, state);
                setAttribute(ds, "TITLE", "saved state");
                setAttribute(ds, "LAYOUT", "[nelements × nspecies]");
                setAttribute(ds, "UNITS", "nm/l ?");

                this.saved_state = ds;
            } else {
                double[] data = (double[]) this.saved_state.getData();
                int columns = state[0].length; /* should all be the same */
                ArrayUtil._flatten(data, state, columns);
                this.saved_state.write(data);
            }
        }

        public void _saveState(double time, String prefix, IGridCalc source)
            throws Exception
        {
            log.debug("state saved at t={} ms for trial {}", time, source.trial());
            int nel = source.getNumberElements();
            int nspecie = source.getSpecieIDs().length;
            if (source.preferConcs())
                this.writeSavedStateD(nel, nspecie, source);
            else
                this.writeSavedStateI(nel, nspecie, source);
        }

        public Object _loadState(String filename, IGridCalc source)
            throws Exception
        {
            // FIXME: This is totally not going to work, because we delete
            // the file on creation...
            Dataset obj = (Dataset) output.get("/simulation/state");
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
    }

    /***********************************************************************
     ***************           Utility functions          ******************
     ***********************************************************************/

    protected static void setAttribute(HObject obj, String name, String value)
        throws Exception
    {
        Attribute attr = new Attribute(name, short_str_t,
                                       new long[] {}, new String[] {value});
        obj.writeMetadata(attr);
        log.debug("Wrote metadata on {} {}={}", obj, name, value);
    }

    protected Dataset writeArray(String name, Group parent, double[][] items)
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

    protected Dataset writeArray(String name, Group parent, int[][] items, int fill)
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

    protected Dataset writeVector(String name, Group parent, String[] items)
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

    protected Dataset writeVector(String name, Group parent, double[] items)
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

    protected H5ScalarDS createExtensibleArray(String name, Group parent, Datatype type,
                                               String TITLE, String LAYOUT, String UNITS,
                                               long... dims)
        throws Exception
    {
        long[] size = dims.clone();
        size[0] = H5F_UNLIMITED;
        long[] chunks = dims.clone();
        chunks[0] = dims[0];

        H5ScalarDS ds = (H5ScalarDS)
            this.output.createScalarDS(name, parent, type,
                                       dims, size, chunks,
                                       6, null);
        ds.init();
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), xJoined(size), xJoined(chunks));

        setAttribute(ds, "TITLE", TITLE);
        setAttribute(ds, "LAYOUT", LAYOUT);
        setAttribute(ds, "UNITS", UNITS);

        return ds;
    }

    protected static void extendExtensibleArray(H5ScalarDS ds, long howmuch)
        throws Exception
    {
        final long[] start = ds.getStartDims();
        final long[] dims = ds.getDims();
        final long[] selected = ds.getSelectedDims();
        start[0] = dims[0];
        dims[0] = dims[0] + howmuch;
        ds.extend(dims);

        selected[0] = howmuch;
        System.arraycopy(dims, 1, selected, 1, dims.length - 1);

        Object data = ds.getData();
        int length = 0;
        if (data instanceof int[])
            length = ((int[])data).length;
        else if (data instanceof double[])
            length = ((double[])data).length;
        else
            assert false;
        if (length != ArrayUtil.product(selected) && false) {
            log.error("howmuch={} start={} dims={} selected={}" +
                      " getSelected→{} getStride={} getDims={} getStartDims={} getMaxDims={} getChunkSize={} {}↔{}",
                      howmuch, start, dims, selected,
                      ds.getSelectedDims(), ds.getStride(), ds.getDims(), ds.getStartDims(),
                      ds.getMaxDims(), ds.getChunkSize(),
                      length, ArrayUtil.product(selected));
        }
    }

    protected static void getGridNumbers(double[] dst, int row,
                                         int nel, int ispecout[], IGridCalc source) {
        final int rowsize = nel * ispecout.length;
        assert dst.length % rowsize == 0;
        assert row < dst.length / rowsize;

        int pos = row * rowsize;
        for (int i = 0; i < nel; i++)
            for (int j = 0; j < ispecout.length; j++)
                dst[pos++] = source.getGridPartNumb(i, ispecout[j]);
    }
}
