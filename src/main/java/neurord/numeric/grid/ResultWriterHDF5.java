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

    final protected File outputFile;
    protected H5File output;
    final protected Map<Integer, Trial> trials = new HashMap<>();

    static final int CACHE_SIZE1 = 1024;
    static final int CACHE_SIZE2 = 8*1024;

    final String[] species;
    final int[] ispecout1;
    final int nel;
    final int[][] ispecout2;
    final int[][] elementsout2;
    final IOutputSet outputSet;
    final List<? extends IOutputSet> outputSets;

    protected H5File.Group model;

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

            for (int i = 0; i < this.ispecout2.length; i++) {
                this.ispecout2[i] = outputSets.get(i).getIndicesOfOutputSpecies(species);

                String region = outputSets.get(i).getRegion();
                if (region != null) {
                    /* Find elements which match specified regions */
                    ArrayList<Integer> list = new ArrayList<>();
                    for (int j = 0; j < this.nel; j++)
                        if (region.equals(grid.getElementRegion(j)))
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
            log.warn("java.library.path: {}", Settings.getProperty("java.library.path"));
            throw new RuntimeException(e);
        } catch(Exception e) {
            log.warn("java.library.path: {}", Settings.getProperty("java.library.path"));
            throw new RuntimeException(e);
        }
    }

    protected void _init()
        throws Exception
    {
        this.output = new H5File(this.outputFile);

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
        H5File.Group g = this.output.createGroup("/manifest");
        g.writeMap(manifest.getMainAttributes().entrySet());
        g.close();
    }

    protected H5File.Group model() throws Exception {
        if (this.model == null) {
            this.model = output.createGroup("/model");
            this.model.setAttribute("TITLE", "model parameters");
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
        String name = "/trial" + trial;
        H5File.Group group = this.output.createGroup(name);
        group.setAttribute("TITLE", "trial " + trial);
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
    public synchronized void closeTrial(IGridCalc source) {
        try {
            this.closeTrial(source.trial(), source);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    public synchronized void writeGrid(VolumeGrid vgrid, double startTime, IGridCalc source) {
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

        final H5File.Group model = this.model();

        t._writeGrid(vgrid, startTime, source);

        writeSpeciesVector("species", "names of all species", model, species, null);

        t.writeRegionLabels(model, source);
        t.writeStimulationData(model, source);
        t.writeReactionData(model, source);
        t.writeEventData(model, source);

        {
            H5File.Group output_info = model.createSubGroup("output");
            output_info.setAttribute("TITLE", "output species");
            t.writeOutputInfo(output_info);
            output_info.close();
        }

        {
            String s = source.getSource().serialize();
            H5File.Dataset ds = model.writeVector("serialized_config", s);
            ds.setAttribute("TITLE", "serialized config");
            ds.setAttribute("LAYOUT", "XML");
            ds.close();
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
        final H5File.Dataset concs;
        final int[][][] concs_cache;
        final H5File.Dataset times;
        final double[] times_cache;
        protected int concs_times_count;

        final int[] ispecout;
        final int[] elements;

        protected PopulationOutput(H5File.Group parent, String name, int[] elements, int[] ispecout)
            throws Exception
        {
            this.ispecout = ispecout;
            this.elements = elements;

            int cache_size = CACHE_SIZE1;
            while (cache_size * elements.length * ispecout.length * 4 > 512*1024
                   && cache_size > 1)
                cache_size /= 2;
            log.info("Using population cache_size of {}", cache_size);

            /* times × nel × nspecout, but we write only for only time 'time' at one time */
            this.concs = parent.createExtensibleArray("population", int.class,
                                                      "population of species in voxels over time",
                                                      "[snapshot × nel × nspecout]",
                                                      "count",
                                                      cache_size, elements.length, ispecout.length);

            this.times = parent.createExtensibleArray("times", double.class,
                                                      "times when snapshots were written",
                                                      "[times]",
                                                      "ms",
                                                      cache_size);

            this.concs_cache = new int[cache_size][elements.length][ispecout.length];
            this.times_cache = new double[cache_size];
        }

        protected void writePopulation(double time, IGridCalc source)
            throws Exception
        {
            getGridNumbers(this.concs_cache[this.concs_times_count],
                           this.elements, this.ispecout, source);
            this.times_cache[this.concs_times_count] = time;
            this.concs_times_count++;

            if (this.concs_times_count == this.times_cache.length)
                this.flushPopulation(time);
        }

        protected void flushPopulation(double time)
            throws Exception
        {
            if (this.concs_times_count == 0)
                return;
            log.debug("Writing {} pop entries at time {}", this.concs_times_count, time);

            {
                final int[][][] cache;
                if (this.concs_times_count == this.times_cache.length)
                    cache = this.concs_cache;
                else
                    cache = Arrays.copyOfRange(this.concs_cache, 0, this.concs_times_count);


                final int[] flat = new int[this.concs_times_count * cache[0].length * cache[0][0].length];
                ArrayUtil._flatten(flat, cache, cache[0][0].length, 0);

                this.concs.extend(this.concs_times_count, flat);
            }

            this.times.extend(this.concs_times_count, this.times_cache);

            this.concs_times_count = 0;
        }
    }

    protected class Trial {
        protected final H5File.Group group;
        protected final H5File.Group sim;
        protected List<PopulationOutput> populations = new ArrayList<>();
        protected H5File.Dataset event_statistics;
        protected H5File.Dataset statistics_times;
        protected H5File.Group events;
        protected List<IGridCalc.Happening> events_cache;
        protected H5File.Dataset
            events_event, events_kind,
            events_extent, events_time, events_waited, events_original;

        protected Trial(H5File.Group group)
            throws Exception
        {
            this.group = group;

            this.sim = group.createSubGroup("output");
            this.sim.setAttribute("TITLE", "results of the simulation");
        }

        protected void close(IGridCalc source)
            throws Exception
        {
            if (this.events_cache != null)
                this.flushEvents(Double.POSITIVE_INFINITY, true);
            for (PopulationOutput output: this.populations)
                output.flushPopulation(Double.POSITIVE_INFINITY);
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
                                     "region_name", "region", "type", "group" };

            Class[] memberTypes = { double.class, double.class, double.class,
                                    double.class, double.class, double.class,
                                    double.class, double.class, double.class,
                                    double.class, double.class, double.class,
                                    double.class, double.class,
                                    String.class,
                                    String.class, int.class, String.class, String.class };
            assert memberNames.length == 19;
            assert memberNames.length == memberTypes.length;

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
                String[] region_names = vgrid.getElementRegions();
                assert region_names.length == nel;
                data.add(region_names);

                List<String> regions = Arrays.asList(vgrid.getRegionLabels());
                int[] region_indices = new int[region_names.length];
                for (int i = 0; i < region_indices.length; i++)
                    region_indices[i] = regions.indexOf(region_names[i]);
                assert region_indices.length == nel;
                data.add(region_indices);
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

            {
                H5File.Dataset grid = model().writeCompoundDS("grid",
                                                              memberNames, memberTypes,
                                                              nel, data.toArray());

                log.info("Created {} with size=[{}]", "grid", data.size());
                grid.setAttribute("TITLE", "voxels");
                grid.setAttribute("LAYOUT",
                                  "[nel × {x,y,z, x,y,z, x,y,z, x,y,z, volume, deltaZ, label, region#, type, group}]");
                grid.close();
            }

            {
                H5File.Dataset ds =
                    model().writeArray("neighbors", vgrid.getPerElementNeighbors(), -1);
                ds.setAttribute("TITLE", "adjacency mapping between voxels");
                ds.setAttribute("LAYOUT", "[nel × neighbors*]");
                ds.setAttribute("UNITS", "indices");
                ds.close();
            }
            {
                H5File.Dataset ds =
                    model().writeArray("couplings", vgrid.getPerElementCouplingConstants());
                ds.setAttribute("TITLE", "coupling rate between voxels");
                ds.setAttribute("LAYOUT", "[nel × neighbors*]");
                ds.setAttribute("UNITS", "nm^2 / nm ?");
                ds.close();
            }
        }

        protected void writeSimulationData(IGridCalc source)
            throws Exception
        {
            long seed = source.getSimulationSeed();
            log.debug("Writing simulation seed ({}) for trial {}", seed, source.trial());
            this.group.setAttribute("simulation_seed", seed);
        }

        protected void writeRegionLabels(H5File.Group parent, IGridCalc source)
            throws Exception
        {
            String[] regions = source.getSource().getVolumeGrid().getRegionLabels();
            H5File.Dataset ds = parent.writeVector("regions", regions);
            ds.setAttribute("TITLE", "names of regions");
            ds.setAttribute("LAYOUT", "[nregions]");
            ds.setAttribute("UNITS", "text");
        }

        protected void writeStimulationData(H5File.Group parent, IGridCalc source)
            throws Exception
        {
            StimulationTable table = source.getSource().getStimulationTable();

            H5File.Group group = parent.createSubGroup("stimulation");
            group.setAttribute("TITLE", "stimulation parameters");

            {
                String[] targets = table.getTargetIDs();
                if (targets.length == 0) {
                    log.debug("Not writing stimulation data (empty targets)");
                    return;
                }

                H5File.Dataset ds = group.writeVector("target_names", targets);
                ds.setAttribute("TITLE", "names of stimulation targets");
                ds.setAttribute("LAYOUT", "[nstimulations]");
                ds.setAttribute("UNITS", "text");
            }

            {
                int[][] targets = source.getSource().getStimulationTargets();
                H5File.Dataset ds = group.writeArray("targets", targets, -1);
                ds.setAttribute("TITLE", "stimulated voxels");
                ds.setAttribute("LAYOUT", "[??? × ???]");
                ds.setAttribute("UNITS", "indices");
            }

            group.close();
        }

        protected void writeReactionData(H5File.Group parent, IGridCalc source)
            throws Exception
        {
            ReactionTable table = source.getSource().getReactionTable();

            H5File.Group group = parent.createSubGroup("reactions");
            group.setAttribute("TITLE", "reaction scheme");

            {
                int[][] indices = table.getReactantIndices();
                H5File.Dataset ds = group.writeArray("reactants", indices, -1);
                ds.setAttribute("TITLE", "reactant indices");
                ds.setAttribute("LAYOUT", "[nreact × nreactants*]");
                ds.setAttribute("UNITS", "indices");
            }
            {
                int[][] indices = table.getProductIndices();
                H5File.Dataset ds = group.writeArray("products", indices, -1);
                ds.setAttribute("TITLE", "product indices");
                ds.setAttribute("LAYOUT", "[nreact × nproducts*]");
                ds.setAttribute("UNITS", "indices");
            }
            {
                int[][] stoichio = table.getReactantStoichiometry();
                H5File.Dataset ds = group.writeArray("reactant_stoichiometry", stoichio, -1);
                ds.setAttribute("TITLE", "reactant stoichiometry");
                ds.setAttribute("LAYOUT", "[nreact × nreactants*]");
                ds.setAttribute("UNITS", "indices");
            }
            {
                int[][] stoichio = table.getProductStoichiometry();
                H5File.Dataset ds = group.writeArray("product_stoichiometry", stoichio, -1);
                ds.setAttribute("TITLE", "product stoichiometry");
                ds.setAttribute("LAYOUT", "[nreact × nproducts*]");
                ds.setAttribute("UNITS", "indices");
            }

            {
                double[] rates = table.getRates();
                H5File.Dataset ds = group.writeVector("rates", rates);
                ds.setAttribute("TITLE", "reaction rates");
                ds.setAttribute("LAYOUT", "[nreact]");
                ds.setAttribute("UNITS", "transitions/ms");
            }
            {
                int[] pairs = table.getReversiblePairs().clone();
                /* pairs has only one index set per pair. Make it symmetrical. */
                for (int i = 0; i < pairs.length; i++)
                    if (pairs[i] >= 0) {
                        assert pairs[pairs[i]] == -1;
                        pairs[pairs[i]] = i;
                    }

                H5File.Dataset ds = group.writeVector("reversible_pairs", pairs);
                ds.setAttribute("TITLE", "indices of reverse reaction");
                ds.setAttribute("LAYOUT", "[nreact]");
                ds.setAttribute("UNITS", "indices");
            }

            group.close();
        }

        protected void writeEventData(H5File.Group parent, IGridCalc source)
            throws Exception
        {
            // FIXME: maybe make stoichiometry writing a separate switch?

            Collection<IGridCalc.Event> events = source.getEvents();
            if (events == null) {
                    log.debug("No dependency data, not writing dependency scheme");
                    return;
            }

            H5File.Group group = parent.createSubGroup("events");
            group.setAttribute("TITLE", "description of all event types");

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
                H5File.Dataset ds = group.writeVector("descriptions", descriptions);
                ds.setAttribute("TITLE", "signatures of reaction channels");
                ds.setAttribute("LAYOUT", "[nchannel]");
                ds.setAttribute("UNITS", "text");
            }

            {
                H5File.Dataset ds = group.writeArray("elements", elements, -1);
                ds.setAttribute("TITLE", "voxel numbers of reaction channels");
                ds.setAttribute("LAYOUT", "[nchannel x {source,target}]");
                ds.setAttribute("UNITS", "index");
            }

            {
                H5File.Dataset ds = group.writeVector("types", types);
                ds.setAttribute("TITLE", "types of reaction channels");
                ds.setAttribute("LAYOUT", "[nchannel]");
                ds.setAttribute("UNITS", "enumeration");
            }

            {
                H5File.Dataset ds = group.writeArray("substrates", substrates, -1);
                ds.setAttribute("TITLE", "event substrates");
                ds.setAttribute("LAYOUT", "[nchannel x nspecies*]");
                ds.setAttribute("UNITS", "indices");
            }

            {
                H5File.Dataset ds = group.writeArray("stoichiometries", stoichiometries, 0);
                ds.setAttribute("TITLE", "substrate stoichiometries");
                ds.setAttribute("LAYOUT", "[nchannel x nspecies*]");
                ds.setAttribute("UNITS", "indices");
            }

            if (dependent != null) {
                H5File.Dataset ds = group.writeArray("dependent", dependent, -1);
                ds.setAttribute("TITLE", "dependent reaction channels");
                ds.setAttribute("LAYOUT", "[nchannel x ndependent*]");
                ds.setAttribute("UNITS", "indices");
            }

            group.close();
        }

        protected void writeOutputInfo(H5File.Group parent, String identifier,
                                        int[] which, int[] elements)
            throws Exception
        {
            H5File.Group group = parent.createSubGroup(identifier);

            writeSpeciesVector("species", "names of output species", group, species, which);

            H5File.Dataset ds = group.writeVector("elements", elements);
            ds.setAttribute("TITLE", "indices of output elements");
            ds.setAttribute("LAYOUT", "[nelements]");
            ds.setAttribute("UNITS", "indices");
        }

        protected void writeOutputInfo(H5File.Group parent)
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

        protected void _writeOutput(int i, double time, IGridCalc source)
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

                H5File.Group group = this.sim.createSubGroup(ident);
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

        protected void initEventStatistics(boolean periodic, IGridCalc source, int expected)
            throws Exception
        {
            assert this.event_statistics == null;

            /* times × events × 2 or times × channels × 2 */
            String type = "events";
            this.event_statistics =
                this.sim.createExtensibleArray("event_statistics", int.class,
                                               "actual event counts since last snapshot",
                                               "[times × " + type + " × species]",
                                               "count",
                                               CACHE_SIZE1, expected, 2);

            if (periodic)
                this.statistics_times =
                    this.sim.createExtensibleArray("statistics_times", double.class,
                                                   "times when statistics were written",
                                                   "[times]",
                                                   "ms",
                                                   CACHE_SIZE1);

            /* Only write stuff for the first trial to save money and time */
            if (source.trial() > 0)
                return;

            String[] descriptions = new String[expected];
            for (IGridCalc.Event ev: source.getEvents()) {
                int stat_index = ev.stat_index();
                if (stat_index >= 0)
                    descriptions[stat_index] = ev.stat_index_description();
            }
            for (int i = 0; i < descriptions.length; i++)
                if (descriptions[i] == null)
                    descriptions[i] = "";

            H5File.Dataset ds = model().writeVector("event_statistics", descriptions);
            ds.setAttribute("TITLE", "descriptions of statistics rows");
            ds.setAttribute("LAYOUT", "[nstatistics]");
            ds.setAttribute("UNITS", "text");
        }

        protected void writeEventStatistics(double time, IGridCalc source)
            throws Exception
        {
            final int[][] stats = source.getEventStatistics();
            if (stats == null) {
                log.debug("Not writing event statistics (no data)");
                return;
            }

            if (this.event_statistics == null) {
                this.initEventStatistics(source.getSource().getStatisticsInterval() > 0,
                                         source,
                                         stats.length);
                if (this.event_statistics == null)
                    return;
            }

            log.debug("Writing event statistics at time {}", time);
            this.event_statistics.extend(stats.length, stats);

            if (this.statistics_times != null)
                this.statistics_times.extend(1, new double[]{ time });
        }

        protected void initEvents()
            throws Exception
        {
            assert this.events == null;

            this.events = this.sim.createSubGroup("events");

            this.events_time = this.events.createExtensibleArray(
                    "times", double.class,
                    "at what time the event happened",
                    "[time]",
                    "ms",
                    CACHE_SIZE2);
            this.events_waited = this.events.createExtensibleArray(
                    "waited", double.class,
                    "time since the previous instance of this event",
                    "[waited]",
                    "ms",
                    CACHE_SIZE2);
            this.events_original = this.events.createExtensibleArray(
                    "original_wait", double.class,
                    "time originally schedule to wait",
                    "[original_wait]",
                    "ms",
                    CACHE_SIZE2);
            this.events_event = this.events.createExtensibleArray(
                    "events", int.class,
                    "index of the event that happened",
                    "[event#]",
                    "",
                    CACHE_SIZE2);
            this.events_kind = this.events.createExtensibleArray(
                    "kinds", int.class,
                    "mechanism of the event that happened",
                    "[kind]",
                    "",
                    CACHE_SIZE2);
            this.events_extent = this.events.createExtensibleArray(
                    "extents", int.class,
                    "the extent of the reaction or event",
                    "[extent]",
                    "count",
                    CACHE_SIZE2);

            long chunk_size = this.events_event.chunks[0];
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
                int[] ints = new int[howmuch];
                double[] doubles = new double[howmuch];

                {
                    for (int i = 0; i < howmuch; i++)
                        doubles[i] = this.events_cache.get(m + i).time();
                    this.events_time.extend(howmuch, doubles);
                }

                {
                    for (int i = 0; i < howmuch; i++)
                        doubles[i] = this.events_cache.get(m + i).waited();
                    this.events_waited.extend(howmuch, doubles);
                }

                {
                    for (int i = 0; i < howmuch; i++)
                        doubles[i] = this.events_cache.get(m + i).original_wait();
                    this.events_original.extend(howmuch, doubles);
                }

                {
                    for (int i = 0; i < howmuch; i++)
                        ints[i] = this.events_cache.get(m + i).event_number();
                    this.events_original.extend(howmuch, ints);
                }

                {
                    for (int i = 0; i < howmuch; i++)
                        ints[i] = this.events_cache.get(m + i).kind().ordinal();
                    this.events_original.extend(howmuch, ints);
                }

                {
                    for (int i = 0; i < howmuch; i++)
                        ints[i] = this.events_cache.get(m + i).extent();
                    this.events_original.extend(howmuch, ints);
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
    }

    /***********************************************************************
     ***************             Model loading            ******************
     ***********************************************************************/

    private static <T> T getSomething(H5File h5, String path)
        throws Exception
    {
        throw new RuntimeException();
        /*
        H5File.Dataset obj = (H5File.Dataset) h5.get(path);
        if (obj == null) {
            log.error("Failed to retrieve \"{}\"", path);
            throw new Exception("Path \"" + path + "\" not found");
        }

        return (T) obj.getData();
        */
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
        H5File.Dataset obj = null; // (H5File.Dataset) h5.get(poppath);
        if (obj == null) {
            log.error("Failed to retrieve \"{}\"", path);
            throw new Exception("Path \"" + path + "\" not found");
        }

        /* This is necessary to retrieve dimensions */
/*
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
*/

        return null;
    }

    protected static LoadModelResult _loadModel(File filename, int trial, Double pop_from_time)
        throws Exception
    {
        log.debug("Opening input file {}", filename);
/*
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

        long seed;
        try {
            long[] data = getAttribute(h5, "/trial" + trial, "simulation_seed");
            seed = data[0];
        } catch(Exception e) {
            long[] data = getSomething(h5, "/trial" + trial + "/simulation_seed");
            seed = data[0];
        }

        final String[] species = getSomething(h5, "/model/species");

        int[][] pop = null;
        if (!Double.isNaN(pop_from_time))
            pop = loadPopulationFromTime(h5, trial, "__main__", pop_from_time);

        Make sure file is closed so that we can overwrite it.
        h5.close();

        return new LoadModelResult(xml, seed, species, pop);
*/

        return null;
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

    protected void writeSpeciesVector(String name, String title,
                                      H5File.Group parent, String[] species, int[] which)
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

        H5File.Dataset ds = parent.writeVector(name, specout);
        ds.setAttribute("TITLE", title);
        ds.setAttribute("LAYOUT", "[nspecies]");
        ds.setAttribute("UNITS", "text");
        ds.close();
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
