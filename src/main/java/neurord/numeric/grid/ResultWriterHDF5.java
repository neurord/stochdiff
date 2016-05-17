package neurord.numeric.grid;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.jar.Manifest;
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

import neurord.numeric.morph.VolumeGrid;
import neurord.numeric.chem.StimulationTable;
import neurord.numeric.chem.ReactionTable;
import neurord.model.IOutputSet;
import neurord.util.ArrayUtil;
import neurord.util.Settings;
import static neurord.util.ArrayUtil.xJoined;
import neurord.util.LibUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;

public class ResultWriterHDF5 implements ResultWriter {
    public static final Logger log = LogManager.getLogger();

    static {
        LibUtil.addLibraryPaths("/usr/lib64/jhdf",
                                "/usr/lib64/jhdf5",
                                "/usr/lib/jhdf",
                                "/usr/lib/jhdf5");
    }

    final static int compression_level = Settings.getProperty("neurord.compression",
                                                              "Compression level in HDF5 output",
                                                              1);

    final protected File outputFile;
    protected H5File output;
    protected Group root;
    final protected Map<Integer, Trial> trials = new HashMap<>();

    public static final H5Datatype double_t =
        new H5Datatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype int_t =
        new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype long_t =
        new H5Datatype(Datatype.CLASS_INTEGER, 8, Datatype.NATIVE, Datatype.NATIVE);
    public static final H5Datatype short_str_t =
        new H5Datatype(Datatype.CLASS_STRING, 100, Datatype.NATIVE, Datatype.NATIVE);

    static final int CACHE_SIZE1 = 1024;
    static final int CACHE_SIZE2 = 8*1024;

    final String[] species;
    final int[] ispecout1;
    final int nel;
    final int[][] ispecout2;
    final int[][] elementsout2;
    final IOutputSet outputSet;
    final List<? extends IOutputSet> outputSets;

    protected Group model;

    public ResultWriterHDF5(File output,
                            IOutputSet primary,
                            List<? extends IOutputSet> outputSets,
                            String[] species,
                            VolumeGrid grid) {

        this.outputFile = new File(output + ".h5");
        log.debug("Writing HDF5 to {}", this.outputFile);

        this.species = species;
        this.outputSet = primary;
        this.outputSets = outputSets;
        this.ispecout1 = primary.getIndicesOfOutputSpecies(species);
        this.nel = grid.size();
        if (this.outputSets != null) {
            this.ispecout2 = new int[outputSets.size()][];
            this.elementsout2 = new int[outputSets.size()][];

            final String[] regionLabels = grid.getRegionLabels();
            final int[] elementRegions = grid.getRegionIndexes();

            for (int i = 0; i < this.ispecout2.length; i++) {
                this.ispecout2[i] = outputSets.get(i).getIndicesOfOutputSpecies(species);

                String region = outputSets.get(i).getRegion();
                if (region != null) {
                    /* Find elements which match specified regions */
                    ArrayList<Integer> list = new ArrayList<>();
                    for (int j = 0; j < elementRegions.length; j++)
                        if (region.equals(regionLabels[elementRegions[j]]))
                            list.add(j);

                    this.elementsout2[i] = ArrayUtil.toIntArray(list);
                } else
                    this.elementsout2[i] = ArrayUtil.iota(this.nel);
            }
        } else {
            this.ispecout2 = null;
            this.elementsout2 = null;
        }
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
        this.writeManifest();
    }

    @Override
    synchronized public void close() {
        if (--users > 0)
            return;

        log.info("Closing output file {}", this.outputFile);

        try {
            for (Map.Entry<Integer, Trial> k_v: this.trials.entrySet())
                this.closeTrial(k_v.getKey(), null);

            this.output.close();
        } catch(Exception e) {
            log.error("Failed to close results file {}", outputFile, e);
        }
    }

    @Override
    public File outputFile() {
        return this.outputFile;
    }

    protected void writeManifest()
        throws Exception
    {
        Manifest manifest = Settings.getManifest();
        writeMap("manifest", this.root, manifest.getMainAttributes().entrySet(), "information about the program");
    }

    protected Group model() throws Exception {
        if (this.model == null) {
            this.model = output.createGroup("model", this.root);
            setAttribute(this.model, "TITLE", "model parameters");
        }

        return this.model;
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

    protected void closeTrial(int trial, IGridCalc source)
        throws Exception
    {
        Trial t = this.trials.get(trial);
        if (t == null)
            return;

        t.close(source);
        this.trials.remove(trial);
    }

    @Override
    public void closeTrial(IGridCalc source) {
        try {
            this.closeTrial(source.trial(), source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    synchronized public void writeGrid(VolumeGrid vgrid, double startTime, IGridCalc source) {
        try {
            this._writeGrid(vgrid, startTime, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void _writeGrid(VolumeGrid vgrid, double startTime, IGridCalc source)
        throws Exception
    {
        Trial t = this.getTrial(source.trial());
        t.writeSimulationData(source);

        /* Only write stuff for the first trial to save time and space */
        if (source.trial() > 0)
            return;

        Group model = this.model();

        t._writeGrid(vgrid, startTime, source);

        writeSpeciesVector("species", "names of all species", model, species, null);

        t.writeRegionLabels(model, source);
        t.writeStimulationData(model, source);
        t.writeReactionData(model, source);
        t.writeEventData(model, source);

        {
            Group output_info = this.output.createGroup("output", model);
            setAttribute(output_info, "TITLE", "output species");
            t.writeOutputInfo(output_info);
        }

        {
            String s = source.getSource().serialize();
            Dataset ds = writeVector("serialized_config", model, s);
            setAttribute(ds, "TITLE", "serialized config");
            setAttribute(ds, "LAYOUT", "XML");
        }
    }

    @Override
    synchronized public void writeOutputInterval(double time, IGridCalc source) {
        writeOutputScheme(-1, time, source);
    }

    @Override
    synchronized public void writeOutputScheme(int i, double time, IGridCalc source) {
        try {
            Trial t = this.getTrial(source.trial());
            t._writeOutput(i + 1, time, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    synchronized public void writeEventStatistics(double time, IGridCalc source) {
        try {
            Trial t = this.getTrial(source.trial());
            t.writeEventStatistics(time, source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected class PopulationOutput {
        final H5ScalarDS concs;
        final int[][][] concs_cache;
        final H5ScalarDS times;
        final double[] times_cache;
        protected int concs_times_count;

        final int[] ispecout;
        final int[] elements;

        public PopulationOutput(Group parent, String name, int[] elements, int[] ispecout)
            throws Exception
        {
            this.ispecout = ispecout;
            this.elements = elements;

            /* times × nel × nspecout, but we write only for only time 'time' at one time */
            this.concs = createExtensibleArray("population", parent, int_t,
                                               "population of species in voxels over time",
                                               "[snapshot × nel × nspecout]",
                                               "count",
                                               CACHE_SIZE1, elements.length, ispecout.length);

            this.times = createExtensibleArray("times", parent, double_t,
                                               "times when snapshots were written",
                                               "[times]",
                                               "ms",
                                               CACHE_SIZE1);

            this.concs_cache = new int[CACHE_SIZE1][elements.length][ispecout.length];
            this.times_cache = new double[CACHE_SIZE1];
        }

        public void writePopulation(double time, IGridCalc source)
            throws Exception
        {
            getGridNumbers(this.concs_cache[this.concs_times_count],
                           this.elements, this.ispecout, source);
            this.times_cache[this.concs_times_count] = time;
            this.concs_times_count++;

            if (this.concs_times_count == this.times_cache.length)
                this.flushPopulation(time);
        }

        public void flushPopulation(double time)
            throws Exception
        {
            if (this.concs_times_count == 0)
                return;
            log.debug("Writing {} stats at time {}", this.concs_times_count, time);

            {
                extendExtensibleArray(this.concs, this.concs_times_count);
                int[] data = (int[]) this.concs.getData();

                int[][][] cache;
                if (this.concs_times_count == this.times_cache.length)
                    cache = this.concs_cache;
                else
                    cache = Arrays.copyOfRange(this.concs_cache, 0, this.concs_times_count);

                ArrayUtil._flatten(data, cache, cache[0][0].length, 0);
                this.concs.write(data);
            }

            {
                extendExtensibleArray(this.times, this.concs_times_count);
                double[] data = (double[]) this.times.getData();
                System.arraycopy(this.times_cache, 0, data, 0, this.concs_times_count);
                this.times.write(data);
            }

            this.concs_times_count = 0;
        }
    }

    protected class Trial {
        protected final Group group;
        protected final Group sim;
        protected List<PopulationOutput> populations = new ArrayList<>();
        protected H5ScalarDS event_statistics;
        protected H5ScalarDS statistics_times;
        protected Group events;
        protected List<IGridCalc.Happening> events_cache;
        protected H5ScalarDS
            events_event, events_kind,
            events_extent, events_time, events_waited, events_original;

        public Trial(Group group)
            throws Exception
        {
            this.group = group;

            this.sim = output.createGroup("output", group);
            setAttribute(this.sim, "TITLE", "results of the simulation");
        }

        protected void close(IGridCalc source)
            throws Exception
        {
            if (this.events_cache != null)
                this.flushEvents(Double.POSITIVE_INFINITY, true);
            for (PopulationOutput output: this.populations)
                output.flushPopulation(Double.POSITIVE_INFINITY);

            if (source != null)
                this.writeFiringsSummary(source);
        }

        protected void _writeGrid(VolumeGrid vgrid, double startTime, IGridCalc source)
            throws Exception
        {
            log.debug("Writing grid at time {} for trial {}", startTime, source.trial());
            assert nel == vgrid.size();
            long[]
                dims = {nel,},
                chunks = {nel,};
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
                String[] labels = new String[nel];
                for (int i = 0; i < nel; i++) {
                    labels[i] = vgrid.getLabel(i);
                    if (labels[i] == null)
                        labels[i] = "element" + i;
                }
                data.add(labels);
            }

            {
                int[] indexes = vgrid.getRegionIndexes();
                assert indexes.length == nel;
                data.add(indexes);
            }

            {
                boolean[] membranes = vgrid.getSubmembranes();
                assert membranes.length == nel;
                String[] types = new String[nel];
                for (int i = 0; i < nel; i++)
                    types[i] = membranes[i] ? "submembrane" : "cytosol";
                data.add(types);
            }

            {
                String[] labels = new String[nel];
                for (int i = 0; i < nel; i++) {
                    labels[i] = vgrid.getGroupID(i);
                    if (labels[i] == null)
                        labels[i] = "";
                }
                data.add(labels);
            }

            Dataset grid =
                output.createCompoundDS("grid", model(), dims, null, chunks, compression_level,
                                        memberNames, memberTypes, null, data);
            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     "grid", xJoined(dims), "", xJoined(chunks));
            setAttribute(grid, "TITLE", "voxels");
            setAttribute(grid, "LAYOUT",
                         "[nel × {x,y,z, x,y,z, x,y,z, x,y,z, volume, deltaZ, label, region#, type, group}]");

            {
                Dataset ds =
                    writeArray("neighbors", model(), vgrid.getPerElementNeighbors(), -1);
                setAttribute(ds, "TITLE", "adjacency mapping between voxels");
                setAttribute(ds, "LAYOUT", "[nel × neighbors*]");
                setAttribute(ds, "UNITS", "indices");
            }
            {
                Dataset ds =
                    writeArray("couplings", model(), vgrid.getPerElementCouplingConstants());
                setAttribute(ds, "TITLE", "coupling rate between voxels");
                setAttribute(ds, "LAYOUT", "[nel × neighbors*]");
                setAttribute(ds, "UNITS", "nm^2 / nm ?");
            }
        }

        protected void writeSimulationData(IGridCalc source)
            throws Exception
        {
            log.debug("Writing simulation configuration for trial {}", source.trial());

            {
                long seed = source.getSimulationSeed();
                Dataset ds = writeVector("simulation_seed", this.group, seed);
                setAttribute(ds, "TITLE", "the calculation seed");
                setAttribute(ds, "LAYOUT", "seed");
                setAttribute(ds, "UNITS", "number");
            }
        }

        protected void writeRegionLabels(Group parent, IGridCalc source)
            throws Exception
        {
            String[] regions = source.getSource().getVolumeGrid().getRegionLabels();
            Dataset ds = writeVector("regions", parent, regions);
            setAttribute(ds, "TITLE", "names of regions");
            setAttribute(ds, "LAYOUT", "[nregions]");
            setAttribute(ds, "UNITS", "text");
        }

        protected void writeStimulationData(Group parent, IGridCalc source)
            throws Exception
        {
            StimulationTable table = source.getSource().getStimulationTable();

            Group group = output.createGroup("stimulation", parent);
            setAttribute(group, "TITLE", "stimulation parameters");

            {
                String[] targets = table.getTargetIDs();
                if (targets.length == 0) {
                    log.debug("Not writing stimulation data (empty targets)");
                    return;
                }

                Dataset ds = writeVector("target_names", group, targets);
                setAttribute(ds, "TITLE", "names of stimulation targets");
                setAttribute(ds, "LAYOUT", "[nstimulations]");
                setAttribute(ds, "UNITS", "text");
            }

            {
                int[][] targets = source.getSource().getStimulationTargets();
                Dataset ds = writeArray("targets", group, targets, -1);
                setAttribute(ds, "TITLE", "stimulated voxels");
                setAttribute(ds, "LAYOUT", "[??? × ???]");
                setAttribute(ds, "UNITS", "indices");
            }
        }

        protected void writeReactionData(Group parent, IGridCalc source)
            throws Exception
        {
            ReactionTable table = source.getSource().getReactionTable();

            Group group = output.createGroup("reactions", parent);
            setAttribute(group, "TITLE", "reaction scheme");

            {
                int[][] indices = table.getReactantIndices();
                Dataset ds = writeArray("reactants", group, indices, -1);
                setAttribute(ds, "TITLE", "reactant indices");
                setAttribute(ds, "LAYOUT", "[nreact × nreactants*]");
                setAttribute(ds, "UNITS", "indices");
            }
            {
                int[][] indices = table.getProductIndices();
                Dataset ds = writeArray("products", group, indices, -1);
                setAttribute(ds, "TITLE", "product indices");
                setAttribute(ds, "LAYOUT", "[nreact × nproducts*]");
                setAttribute(ds, "UNITS", "indices");
            }
            {
                int[][] stoichio = table.getReactantStoichiometry();
                Dataset ds = writeArray("reactant_stoichiometry", group, stoichio, -1);
                setAttribute(ds, "TITLE", "reactant stoichiometry");
                setAttribute(ds, "LAYOUT", "[nreact × nreactants*]");
                setAttribute(ds, "UNITS", "indices");
            }
            {
                int[][] stoichio = table.getProductStoichiometry();
                Dataset ds = writeArray("product_stoichiometry", group, stoichio, -1);
                setAttribute(ds, "TITLE", "product stoichiometry");
                setAttribute(ds, "LAYOUT", "[nreact × nproducts*]");
                setAttribute(ds, "UNITS", "indices");
            }

            {
                double[] rates = table.getRates();
                Dataset ds = writeVector("rates", group, rates);
                setAttribute(ds, "TITLE", "reaction rates");
                setAttribute(ds, "LAYOUT", "[nreact]");
                setAttribute(ds, "UNITS", "transitions/ms");
            }
            {
                int[] pairs = table.getReversiblePairs();
                /* pairs has only one index set per pair. Make it symmetrical. */
                for (int i = 0; i < pairs.length; i++)
                    if (pairs[i] >= 0) {
                        assert pairs[pairs[i]] == -1;
                        pairs[pairs[i]] = i;
                    }

                Dataset ds = writeVector("reversible_pairs", group, pairs);
                setAttribute(ds, "TITLE", "indices of reverse reaction");
                setAttribute(ds, "LAYOUT", "[nreact]");
                setAttribute(ds, "UNITS", "indices");
            }
        }

        protected void writeEventData(Group parent, IGridCalc source)
            throws Exception
        {
            // FIXME: maybe make stoichiometry writing a separate switch?

            Collection<IGridCalc.Event> events = source.getEvents();
            if (events == null) {
                    log.debug("No dependency data, not writing dependency scheme");
                    return;
            }

            Group group = output.createGroup("events", parent);
            setAttribute(group, "TITLE", "description of all event types");

            {
                String[] descriptions = new String[events.size()];
                int[] types = new int[events.size()];
                int[][] elements = new int[events.size()][2];
                int[][] substrates = new int[events.size()][];
                int[][] stoichiometries = new int[events.size()][];
                int[][] dependent = null;

                if (source.getSource().writeDependencies()) {
                    log.debug("Dependency scheme writing enabled");
                    dependent = new int[events.size()][];
                }

                for (IGridCalc.Event event: events) {
                    int i = event.event_number();
                    descriptions[i] = event.description();
                    types[i] = event.event_type().ordinal();
                    elements[i][0] = event.element();
                    elements[i][1] = event.element2();
                    substrates[i] = event.substrates();
                    stoichiometries[i] = event.substrate_stoichiometry();

                    if (dependent != null) {
                        Collection<IGridCalc.Event> dep = event.dependent();
                        dependent[i] = new int[dep.size()];
                        int j = 0;
                        for (IGridCalc.Event child: dep)
                            dependent[i][j++] = child.event_number();
                    }
                }

                {
                    Dataset ds = writeVector("descriptions", group, descriptions);
                    setAttribute(ds, "TITLE", "signatures of reaction channels");
                    setAttribute(ds, "LAYOUT", "[nchannel]");
                    setAttribute(ds, "UNITS", "text");
                }

                {
                    Dataset ds = writeArray("elements", group, elements, -1);
                    setAttribute(ds, "TITLE", "voxel numbers of reaction channels");
                    setAttribute(ds, "LAYOUT", "[nchannel x {source,target}]");
                    setAttribute(ds, "UNITS", "index");
                }

                {
                    Dataset ds = writeVector("types", group, types);
                    setAttribute(ds, "TITLE", "types of reaction channels");
                    setAttribute(ds, "LAYOUT", "[nchannel]");
                    setAttribute(ds, "UNITS", "enumeration");
                }

                {
                    Dataset ds = writeArray("substrates", group, substrates, -1);
                    setAttribute(ds, "TITLE", "event substrates");
                    setAttribute(ds, "LAYOUT", "[nchannel x nspecies*]");
                    setAttribute(ds, "UNITS", "indices");
                }

                {
                    Dataset ds = writeArray("stoichiometries", group, stoichiometries, 0);
                    setAttribute(ds, "TITLE", "substrate stoichiometries");
                    setAttribute(ds, "LAYOUT", "[nchannel x nspecies*]");
                    setAttribute(ds, "UNITS", "indices");
                }

                if (dependent != null) {
                    Dataset ds = writeArray("dependent", group, dependent, -1);
                    setAttribute(ds, "TITLE", "dependent reaction channels");
                    setAttribute(ds, "LAYOUT", "[nchannel x ndependent*]");
                    setAttribute(ds, "UNITS", "indices");
                }
            }
        }

        protected void writeOutputInfo(Group parent, String identifier,
                                        int[] which, int[] elements)
            throws Exception
        {
            Group group = output.createGroup(identifier, parent);

            writeSpeciesVector("species", "names of output species", group, species, which);

            Dataset ds = writeVector("elements", group, elements);
            setAttribute(ds, "TITLE", "indices of output elements");
            setAttribute(ds, "LAYOUT", "[nelements]");
            setAttribute(ds, "UNITS", "indices");
        }

        protected void writeOutputInfo(Group parent)
            throws Exception
        {
            /* We cannot use getNamesOfOutputSpecies() because it has various special
             * rules like support for "all". Instead we use precalulcated lists of species
             * indices. */
            if (ispecout1 != null)
                writeOutputInfo(parent, "__main__", ispecout1, ArrayUtil.iota(nel));

            if (outputSets != null)
                for (int i = 0; i < outputSets.size(); i++) {
                    IOutputSet set = outputSets.get(i);
                    writeOutputInfo(parent,
                                    set.getIdentifier(), ispecout2[i], elementsout2[i]);
                }
        }

        public void _writeOutput(int i, double time, IGridCalc source)
            throws Exception
        {
            this.writePopulation(i, time, source);
            if (i == 0)
                this.writeEvents(time, source);
        }

        protected boolean initPopulation(int i, IGridCalc source)
            throws Exception
        {
            if (i >= this.populations.size() || this.populations.get(i) == null) {
                int elements[], ispecout[];
                String ident;

                if (i == 0) {
                    /* special case */
                    if (ispecout1.length == 0)
                        return false;

                    ident = "__main__";
                    elements = ArrayUtil.iota(nel);
                    ispecout = ispecout1;
                } else {
                    final IOutputSet set = outputSets.get(i - 1);
                    assert set != null;

                    log.debug("elementsout2: {} {}", elementsout2, "");
                    log.debug("i:{} {}", i, elementsout2[i - 1]);

                    elements = elementsout2[i - 1];
                    ispecout = ispecout2[i - 1];

                    ident = set.getIdentifier();
                }

                Group group = output.createGroup(ident, this.sim);
                PopulationOutput conc = new PopulationOutput(group, ident, elements, ispecout);
                this.populations.add(i, conc);
            }

            return true;
        }

        protected void writePopulation(int i, double time, IGridCalc source)
            throws Exception
        {
            if (!this.initPopulation(i, source))
                return;

            this.populations.get(i).writePopulation(time, source);
        }

        protected void initEventStatistics(boolean periodic, int events)
            throws Exception
        {
            assert this.event_statistics == null;

            /* times × events × 2 or times × channels × 2 */
            String type = "events";
            this.event_statistics =
                createExtensibleArray("event_statistics", this.sim, int_t,
                                      "actual stimulation counts since last snapshot",
                                      "[times × " + type + " × species]",
                                      "count",
                                      CACHE_SIZE1, events, 2);

            if (periodic)
                this.statistics_times =
                    createExtensibleArray("statistics_times", this.sim, double_t,
                                          "times when statistics were written",
                                          "[times]",
                                          "ms",
                                          CACHE_SIZE1);
        }

        protected void writeEventStatistics(double time, IGridCalc source)
            throws Exception
        {
            final int[][] stats = source.getEventStatistics();
            if (stats == null)
                return;

            if (this.event_statistics == null)
                this.initEventStatistics(source.getSource().getStatisticsInterval() > 0,
                                         stats.length);

            log.debug("Writing event statistics at time {}", time);
            {
                extendExtensibleArray(this.event_statistics, 1);
                long[] dims = this.event_statistics.getDims();
                int[] data = (int[]) this.event_statistics.getData();
                ArrayUtil._flatten(data, stats, 2, 0);
                this.event_statistics.write(data);
            }

            if (this.statistics_times != null) {
                extendExtensibleArray(this.statistics_times, 1);
                double[] data = (double[]) this.statistics_times.getData();
                data[0] = time;
                this.statistics_times.write(data);
            }
        }

        protected void initEvents()
            throws Exception
        {
            assert this.events == null;

            this.events = output.createGroup("events", this.sim);

            this.events_time = createExtensibleArray("times", this.events, double_t,
                                                     "at what time the event happened",
                                                     "[time]",
                                                     "ms",
                                                     CACHE_SIZE2);
            this.events_waited = createExtensibleArray("waited", this.events, double_t,
                                                       "time since the previous instance of this event",
                                                       "[waited]",
                                                       "ms",
                                                       CACHE_SIZE2);
            this.events_original = createExtensibleArray("original_wait", this.events, double_t,
                                                       "time originally schedule to wait",
                                                       "[original_wait]",
                                                       "ms",
                                                       CACHE_SIZE2);
            this.events_event = createExtensibleArray("events", this.events, int_t,
                                                      "index of the event that happened",
                                                      "[event#]",
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

            long chunk_size = this.events_event.getChunkSize()[0];
            this.events_cache = new ArrayList<>((int)chunk_size);
        }

        private boolean initEvents_warning = false;

        protected void flushEvents(double time, boolean all)
            throws Exception
        {
            int n = this.events_cache.size();
            if (!all)
                n -= n % CACHE_SIZE2;

            int howmuch, m;
            for (m = 0; m < n; m += howmuch) {
                howmuch = Math.min(n - m, CACHE_SIZE2);
                log.debug("Writing {} events at time {}", howmuch, time);

                {
                    extendExtensibleArray(this.events_time, howmuch);
                    double[] data = (double[]) this.events_time.getData();
                    for (int i = 0; i < howmuch; i++)
                        data[i] = this.events_cache.get(m + i).time();
                    this.events_time.write(data);
                }

                {
                    extendExtensibleArray(this.events_waited, howmuch);
                    double[] data = (double[]) this.events_waited.getData();
                    for (int i = 0; i < howmuch; i++)
                        data[i] = this.events_cache.get(m + i).waited();
                    this.events_waited.write(data);
                }

                {
                    extendExtensibleArray(this.events_original, howmuch);
                    double[] data = (double[]) this.events_original.getData();
                    for (int i = 0; i < howmuch; i++)
                        data[i] = this.events_cache.get(m + i).original_wait();
                    this.events_original.write(data);
                }

                {
                    extendExtensibleArray(this.events_event, howmuch);
                    int[] data = (int[]) this.events_event.getData();
                    for (int i = 0; i < howmuch; i++)
                        data[i] = this.events_cache.get(m + i).event_number();
                    this.events_event.write(data);
                }

                {
                    extendExtensibleArray(this.events_kind, howmuch);
                    int[] data = (int[]) this.events_kind.getData();
                    for (int i = 0; i < howmuch; i++)
                        data[i] = this.events_cache.get(m + i).kind().ordinal();
                    this.events_kind.write(data);
                }

                {
                    extendExtensibleArray(this.events_extent, howmuch);
                    int[] data = (int[]) this.events_extent.getData();
                    for (int i = 0; i < howmuch; i++)
                        data[i] = this.events_cache.get(m + i).extent();
                    this.events_extent.write(data);
                }
            }

            if (m == this.events_cache.size())
                this.events_cache.clear();
            else if (m > 0)
                this.events_cache = this.events_cache.subList(n, this.events_cache.size());
        }

        protected void writeEvents(double time, IGridCalc source)
            throws Exception
        {
            final Collection<IGridCalc.Happening> events = source.getHappenings();
            if (events == null) {
                if (!initEvents_warning) {
                    log.debug("No events, not writing anything");
                    initEvents_warning = true;
                }
                return;
            }

            if (this.events == null)
                this.initEvents();

            this.events_cache.addAll(events);

            if (this.events_cache.size() > CACHE_SIZE2)
                this.flushEvents(time, false);
        }

        protected void writeFiringsSummary(IGridCalc source)
            throws Exception
        {
            Collection<IGridCalc.Event> events = source.getEvents();
            long array[] = new long[events.size()];
            int i=0;
            for (IGridCalc.Event event: events) {
                log.info("event {} pos={} number={}/{}", event, i++, event.event_number(), events.size());
                array[event.event_number()] = event.firings();
            }

            Dataset ds = writeVector("firings", this.sim, array);
            setAttribute(ds, "TITLE", "Firings of each reaction");
            setAttribute(ds, "LAYOUT", "[nevents]");
            setAttribute(ds, "UNITS", "counts");
        }
    }

    /***********************************************************************
     ***************             Model loading            ******************
     ***********************************************************************/

    private static <T> T getSomething(H5File h5, String path)
        throws Exception
    {
        Dataset obj = (Dataset) h5.get(path);
        if (obj == null) {
            log.error("Failed to retrieve \"{}\"", path);
            throw new Exception("Path \"" + path + "\" not found");
        }

        return (T) obj.getData();
    }

    private static int[][] loadPopulationFromTime(H5File h5,
                                                  int trial,
                                                  String output_set,
                                                  double pop_from_time)
        throws Exception
    {
        String path = "/trial" + trial + "/output/" + output_set;

        final int index;
        {
            double[] times = getSomething(h5, path + "/times");
            if (pop_from_time == -1)
                index = times.length - 1;
            else if (pop_from_time < 0)
                throw new Exception("Time must be nonnegative or -1");
            else {
                index = Arrays.binarySearch(times, pop_from_time);
                if (index < 0)
                    throw new Exception("time= " + pop_from_time + " not found "
                                        + "in " + path + "/times");
            }
        }

        String poppath = path + "/population";
        Dataset obj = (Dataset) h5.get(poppath);
        if (obj == null) {
            log.error("Failed to retrieve \"{}\"", path);
            throw new Exception("Path \"" + path + "\" not found");
        }

        /* This is necessary to retrieve dimensions */
        obj.init();

        int rank = obj.getRank();
        long[] dims = obj.getDims();
        long[] start = obj.getStartDims();
        long[] selected = obj.getSelectedDims();
        int[] selectedIndex = obj.getSelectedIndex();

        log.info("Retrieving population from {}:{} row {}", h5, poppath, index);
        log.debug("pristine rank={} dims={} start={} selected={} selectedIndex={}",
                  rank, dims, start, selected, selectedIndex);
        start[0] = index;
        selected[0] = 1;
        selected[1] = dims[1];
        selected[2] = dims[2];
        log.debug("selected rank={} dims={} start={} selected={} selectedIndex={}",
                  rank, dims, start, selected, selectedIndex);
        int[] data = (int[]) obj.getData();
        int[][] pop = ArrayUtil.reshape(data, (int) dims[1], (int) dims[2]);
        // log.debug("{}", (Object) pop);
        return pop;
    }

    protected static LoadModelResult _loadModel(File filename, int trial, Double pop_from_time)
        throws Exception
    {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        if (fileFormat == null)
            throw new UnsatisfiedLinkError("hdf5");

        log.debug("Opening input file {}", filename);
        final H5File h5;
        try {
            h5 = (H5File) fileFormat.createInstance(filename.toString(), FileFormat.READ);
        } catch(Exception e) {
            log.error("Failed to open input file {}", filename);
            throw e;
        }
        assert h5 != null;

        final String xml;
        {
            String[] data = getSomething(h5, "/model/serialized_config");
            xml = data[0];
        }

        final long seed;
        {
            long[] data = getSomething(h5, "/trial" + trial + "/simulation_seed");
            seed = data[0];
        }

        final String[] species = getSomething(h5, "/model/species");

        int[][] pop = null;
        if (!Double.isNaN(pop_from_time))
            pop = loadPopulationFromTime(h5, trial, "__main__", pop_from_time);

        return new LoadModelResult(xml, seed, species, pop);
    }

    public static LoadModelResult loadModel(File filename, int trial, double pop_from_time) {
        try {
            return _loadModel(filename, trial, pop_from_time);
        } catch(Exception e) {
            log.error("Failed to read input file \"{}\"", filename);
            throw new RuntimeException(e);
        }
    }

    public static class LoadModelResult {
        final public String xml;
        final public long seed;
        final public String[] species;
        final public int[][] population;

        LoadModelResult(String xml, long seed, String[] species, int[][] population) {
            this.xml = xml;
            this.seed = seed;
            this.species = species;
            this.population = population;
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

    protected Dataset writeVector(String name, Group parent, String... items)
        throws Exception
    {
        int maxlength = ArrayUtil.maxLength(items) * 4;
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

    protected Group writeMap(String name, Group parent, Set<Map.Entry<Object,Object>> set, String title)
        throws Exception
    {
        Group group = this.output.createGroup(name, parent);
        setAttribute(group, "TITLE", title);

        for (Map.Entry<Object,Object> entry: set) {
            /* Manifest keys allow ascii characters, numbers, dashes and underscores.
             * Convert dashes to underscores so keys are valid python attributes. */
            String key = entry.getKey().toString().toLowerCase().replace("-", "_");
            String value = (String) entry.getValue();

            writeVector(key, group, value);
        }

        return group;
    }

    protected Dataset writeVector(String name, Group parent, double... items)
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

    protected Dataset writeVector(String name, Group parent, int... items)
        throws Exception
    {
        long[] dims = {items.length};

        Dataset ds = this.output.createScalarDS(name, parent,
                                                int_t, dims, null, null,
                                                0, items);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
        return ds;
    }

    protected Dataset writeVector(String name, Group parent, long... items)
        throws Exception
    {
        long[] dims = {items.length};

        Dataset ds = this.output.createScalarDS(name, parent,
                                                long_t, dims, null, null,
                                                0, items);
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), "", "");
        return ds;
    }

    protected void writeSpeciesVector(String name, String title,
                                      Group parent, String[] species, int[] which)
        throws Exception
    {
        final String[] specout;
        if (which == null)
            specout = species;
        else {
            specout = new String[which.length];
            for (int i = 0; i < which.length; i++)
                specout[i] = species[which[i]];
        }

        Dataset ds = writeVector(name, parent, specout);
        setAttribute(ds, "TITLE", title);
        setAttribute(ds, "LAYOUT", "[nspecies]");
        setAttribute(ds, "UNITS", "text");
    }

    protected H5ScalarDS createExtensibleArray(String name, Group parent, Datatype type,
                                               String TITLE, String LAYOUT, String UNITS,
                                               long... dims)
        throws Exception
    {
        long[] maxdims = dims.clone();
        maxdims[0] = H5F_UNLIMITED;
        long[] chunks = dims.clone();

        /* avoid too small chunks */
        chunks[0] = 1;
        while (ArrayUtil.product(chunks) < 1024)
            chunks[0] *= 2;

        /* do not write any data in the beginning */
        dims[0] = 0;

        H5ScalarDS ds = (H5ScalarDS)
            this.output.createScalarDS(name, parent, type,
                                       dims, maxdims, chunks,
                                       compression_level, null);
        ds.init();
        log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                 name, xJoined(dims), xJoined(maxdims), xJoined(chunks));

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
        if (length < ArrayUtil.product(selected))
            log.error("howmuch={} start={} dims={} selected={}" +
                      " getSelected→{} getStride={} getDims={} getStartDims={} getMaxDims={} getChunkSize={} {}↔{}" +
                      "\ndata={}",
                      howmuch, start, dims, selected,
                      ds.getSelectedDims(), ds.getStride(), ds.getDims(), ds.getStartDims(),
                      ds.getMaxDims(), ds.getChunkSize(),
                      length, ArrayUtil.product(selected),
                      data);
    }

    protected static void getGridNumbers(int[][] dst,
                                         int elements[], int ispecout[], IGridCalc source) {
        for (int i = 0; i < elements.length; i++)
            for (int j = 0; j < ispecout.length; j++) {
                dst[i][j] = source.getGridPartNumb(elements[i], ispecout[j]);
                assert dst[i][j] >= 0: "" + i + " " + j + " " + dst[i][j];
            }
    }
}
