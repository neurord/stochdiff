package neurord.model;

import java.io.File;
import java.util.List;
import java.util.HashMap;

import neurord.disc.SpineLocator;
import neurord.disc.TreeBoxDiscretizer;
import neurord.disc.TreeCurvedElementDiscretizer;
import neurord.numeric.BaseCalc;
import neurord.numeric.BaseCalc.distribution_t;
import neurord.numeric.BaseCalc.algorithm_t;
import neurord.numeric.morph.TreePoint;
import neurord.numeric.morph.VolumeGrid;
import neurord.numeric.morph.VolumeGrid.geometry_t;
import neurord.numeric.chem.ReactionTable;
import neurord.numeric.chem.StimulationTable;
import neurord.numeric.grid.ResultWriterHDF5;
import neurord.numeric.grid.ResultWriterHDF5.LoadModelResult;
import neurord.util.ArrayUtil;
import neurord.xml.StringListAdapter;
import neurord.xml.ModelReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

@XmlRootElement(name="SDRun")
public class SDRun implements IOutputSet {
    static final Logger log = LogManager.getLogger();

    @XmlElement(name="ReactionScheme")
    private ReactionScheme reactionScheme;

    @XmlElement(name="StimulationSet")
    private StimulationSet stimulationSet;

    @XmlElement(name="Morphology")
    private Morphology morphology;

    @XmlElement(name="InitialConditions")
    private InitialConditions initialConditions;

    @XmlElement(name="OutputScheme")
    public OutputScheme outputScheme;

    private Discretization discretization;

    public String action;

    private String geometry;
    public double depth2D = 0.5;

    private Double runtime;
    private Double starttime;
    private Double endtime;

    public String output;


    public int spineSeed;
    public long simulationSeed;

    // time step for fixed step calculations;
    private Double fixedStepDt;

    private double outputInterval;

    @XmlJavaTypeAdapter(StringListAdapter.class)
    private List<String> outputSpecies;

    public String outputQuantity = "NUMBER"; // either "NUMBER" or "CONCENTRATION"

    /**
     * Accepted tolerance for adaptive calculations
     * (delta f / f  for an algorithm dependent function f).
     */
    public double tolerance = 0.001;

    /**
     * How many times our calculated allowed leap must be longer than
     * normal event waiting time, for us to choose leaping.
     */
    public double leap_min_jump = 2;

    public String calculation;

    public String distribution;
    public String algorithm;

    private transient VolumeGrid volumeGrid;
    private transient int[][] stimulationTargets;

    // just getters from here on;

    public distribution_t getDistribution() {
        if (this.distribution == null)
            return distribution_t.BINOMIAL;
        return distribution_t.valueOf(this.distribution);
    }

    public algorithm_t getAlgorithm() {
        if (this.algorithm == null)
            return algorithm_t.INDEPENDENT;
        return algorithm_t.valueOf(this.algorithm);
    }

    public geometry_t getGeometry() {
        if (this.geometry == null)
            return geometry_t.GEOM_2D;
        return geometry_t.fromString(this.geometry);
    }

    @Override
    public String getRegion() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return "out";
    }

    @Override
    public double getOutputInterval(double fallback) {
        return this.outputInterval;
    }

    public double getOutputInterval() {
        return this.outputInterval;
    }

    public double getFixedStepDt() {
        if (this.fixedStepDt != null)
            return this.fixedStepDt;
        else
            return Float.POSITIVE_INFINITY;
    }

    public List<? extends IOutputSet> getOutputSets() {
        if (this.outputScheme != null)
            return this.outputScheme.outputSets;
        else
            return null;
    }

    public boolean writeDependencies() {
        if (this.outputScheme == null || this.outputScheme.dependencies == null)
            return false;
        return this.outputScheme.dependencies;
    }

    transient private boolean _reactionSchemeResolved = false;
    public ReactionScheme getReactionScheme() {
        if (!this._reactionSchemeResolved) {
            if (this.reactionScheme == null) {
                log.error("<ReactionScheme> element is required");
                throw new RuntimeException("<ReactionScheme> element is required");
            }
            this.reactionScheme.resolve();
            this._reactionSchemeResolved =true;
        }
        return this.reactionScheme;
    }

    transient private ReactionTable reactionTable;
    public ReactionTable getReactionTable() {
        if (this.reactionTable == null)
            this.reactionTable = this.getReactionScheme().makeReactionTable();
        return this.reactionTable;
    }

    transient private StimulationTable stimulationTable;
    public StimulationTable getStimulationTable() {
        if (this.stimulationTable == null) {
            List<InjectionStim> stimulations = null;
            if (this.stimulationSet != null)
                stimulations = this.stimulationSet.stimulations;
            this.stimulationTable = new StimulationTable(stimulations,
                                                         this.getReactionTable());
        }
        return this.stimulationTable;
    }

    public Morphology getMorphology() {
        if (this.morphology == null) {
            log.error("<Morphology> element is required");
            throw new RuntimeException("<Morphology> element is required");
        }
        this.morphology.resolve();
        return this.morphology;
    }

    public String[] getSpecies() {
        return this.getReactionScheme().getSpecies();
    }

    @Override
    public List<String> getNamesOfOutputSpecies() {
        return this.outputSpecies;
    }

    @Override
    public int[] getIndicesOfOutputSpecies(String[] species) {
        return OutputSet.outputSpecieIndices("outputSpecies", this.outputSpecies, species);
    }

    public Discretization getDiscretization() {
        if (this.discretization != null)
            return this.discretization;
        else
            return Discretization.SINGLE_VOXEL;
    }

    private transient InitialConditions _empty_initalConditions;
    public InitialConditions getInitialConditions() {
        if (this.initialConditions != null)
            return this.initialConditions;
        else {
            if (this._empty_initalConditions == null)
                this._empty_initalConditions = new InitialConditions();
            return this._empty_initalConditions;
        }
    }

    public double getStartTime() {
        if (this.starttime != null)
            return this.starttime;
        else
            return 0;
    }

    public double getEndTime() {
        if (this.endtime != null)
            return this.endtime;
        else if (this.runtime != null)
            return this.runtime + getStartTime();
        else {
            log.error("Either runtime or endtime must be specified in the model file");
            throw new RuntimeException("Either runtime or endtime must be specified in the model file");
        }
    }

    public boolean continueOutput() {
        boolean ret = false;
        if (output == null) {
            // fine - not specified
        } else {
            String lco = output.toLowerCase().trim();
            if (lco.equals("continue")) {
                ret = true;
            } else if (lco.equals("new")) {
                ret = false;
            } else {
                log.error("Unrecognized output option: " + output + " (expecting 'new' or 'continue')");
                throw new RuntimeException("Unrecognized output option: " + output + " (expecting 'new' or 'continue')");
            }
        }
        return ret;
    }

    public synchronized VolumeGrid getVolumeGrid() {
        if (this.volumeGrid == null) {
            final Morphology morph = this.getMorphology();
            final TreePoint[] tpa = morph.getTreePoints();
            final Discretization disc = this.getDiscretization();

            double d = disc.getDefaultMaxElementSide();
            double deltaX = disc.spineDeltaX != null ? disc.spineDeltaX : d;

            // <--WK 6 22 2007
            // (1) iterate through all endpoints and their associated radii.
            // (2) divide each radius by successively increasing odd numbers until
            // the divided value becomes less than the defaultMaxElementSide.
            // (3) select the smallest among the divided radii values as d.
            double[] diameters = new double[tpa.length];
            double[] candidate_grid_sizes = new double[tpa.length];
            for (int i = 0; i < tpa.length; i++) {
                double diameter = tpa[i].getRadius() * 2;
                diameters[i] = diameter;
                double denominator = 1;
                while (diameter / denominator > d)
                    denominator += 2; // divide by successive odd numbers

                candidate_grid_sizes[i] = diameter / denominator;
            }

            d = Math.min(d, ArrayUtil.min(candidate_grid_sizes));
            log.info("subvolume grid size is: {} (from radii {}, candidates {}, default {})",
                     d, diameters, candidate_grid_sizes, disc.getDefaultMaxElementSide());

            if (disc.curvedElements()) {
                TreeCurvedElementDiscretizer tced = new TreeCurvedElementDiscretizer(tpa);
                volumeGrid = tced.buildGrid(d, disc.getResolutionHM(), disc.getSurfaceLayers(),
                                            disc.getMaxAspectRatio());

            } else
                 volumeGrid = TreeBoxDiscretizer.buildGrid(tpa,
                                                           d, disc.getResolutionHM(), disc.getSurfaceLayers(),
                                                           this.getGeometry(), this.depth2D);

            SpineLocator.locate(this.spineSeed,
                                morph.getSpineDistribution(),
                                deltaX,
                                volumeGrid);
            volumeGrid.fix();
        }

        return this.volumeGrid;
    }

    public synchronized int[][] getStimulationTargets() {
        if (this.stimulationTargets == null) {
            VolumeGrid grid = this.getVolumeGrid();
            String[] targets = this.getStimulationTable().getTargetIDs();
            this.stimulationTargets = grid.getAreaIndexes(targets);
        }

        return this.stimulationTargets;
    }

    public double[] getRegionConcentration(int region) {
        String[] allregions = this.getVolumeGrid().getRegionLabels();
        String[] regions = new String[]{ allregions[region] };

        return this.getInitialConditions().makeRegionConcentrations(regions, this.getSpecies())[0];
    }

    public double[][] getRegionConcentrations() {
        String[] regions = this.getVolumeGrid().getRegionLabels();
        return this.getInitialConditions().makeRegionConcentrations(regions, this.getSpecies());
    }

    public double[][] getRegionSurfaceDensities() {
        String[] regions = this.getVolumeGrid().getRegionLabels();
        return this.getInitialConditions().makeRegionSurfaceDensities(regions, this.getSpecies());
    }

    public String serialize() {
        try {
            ModelReader<SDRun> loader = new ModelReader(SDRun.class);
            return loader.marshall(this);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static SDRun deserialize(String xml, Long seed) {
        ModelReader<SDRun> loader = new ModelReader(SDRun.class);
        HashMap<String,String> overrides = null;

        if (seed != null) {
            overrides = new HashMap<>();
            overrides.put("SDRun.simulationSeed", "" + seed);
        }

        log.info("Loading model from xml with seed {}", seed);
        try {
            return loader.unmarshall(xml, overrides);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static SDRun deserialize(File filename, Long seed) {
        ModelReader<SDRun> loader = new ModelReader(SDRun.class);
        HashMap<String,String> overrides = null;

        if (seed != null) {
            overrides = new HashMap<>();
            overrides.put("SDRun.simulationSeed", "" + seed);
        }

        log.info("Loading model from \"{}\" with seed {}", filename, seed);
        try {
            return loader.unmarshall(filename, overrides);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A helper field to transport the population between
     * HDF5 source file and actual simulation.
     */
    private transient int[][] population;

    public int[][] getPopulation() {
        return this.population;
    }

    public static boolean speciesMatch(String[] a, String[] b) {
        if (a.length != b.length) {
            log.error("Different number of species: {} vs {}", a.length, b.length);
            return false;
        }

        for (int i = 0; i < a.length; i++)
            if (!a[i].equals(b[i])) {
                log.error("Species {}mismatch: {} vs {}", i, a[i], b[i]);
                return false;
            }

        return true;
    }

    public static SDRun loadFromFile(File model_file,
                                     File ic_file,
                                     int trial,
                                     double population_from_time) {
        boolean h5 = model_file.toString().endsWith(".h5");
        if (Double.isNaN(population_from_time) && ic_file != null)
            population_from_time = 0; /* the default */
        boolean have_time = !Double.isNaN(population_from_time);

        if (!h5 && ic_file == null && have_time)
            throw new RuntimeException("Cannot load population from .xml file (--ic-time option)");

        final String xml;

        LoadModelResult ic = null;
        if (ic_file != null)
             ic = ResultWriterHDF5.loadModel(ic_file, trial, population_from_time);

        final SDRun sdrun;
        LoadModelResult model = null;
        if (h5) {
            /* load full state from model_file if wanted and ic_file not specified separately */
            model =
                ResultWriterHDF5.loadModel(model_file, trial,
                                           ic_file == null ? population_from_time : Double.NaN);
            sdrun = deserialize(model.xml, ic != null ? ic.seed : model.seed);
        } else
            sdrun = deserialize(model_file, ic != null ? ic.seed : null);

        if (have_time) {
            String[] species = ic == null ? model.species : ic.species;
            if (!speciesMatch(sdrun.getSpecies(), species))
                throw new RuntimeException("Species list mismatch");

            sdrun.population = ic == null ? model.population : ic.population;
        }

        return sdrun;
    }

    public double stepSize() {
        return Math.min(Math.min(this.getFixedStepDt(),
                                 this.getOutputInterval()),
                        this.getEndTime() - this.getStartTime());
    }
}
