package neurord;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import neurord.model.SDRun;
import neurord.numeric.BaseCalc;
import neurord.numeric.morph.VolumeGrid;
import neurord.numeric.grid.ResultWriter;
import neurord.numeric.grid.ResultWriterText;
import neurord.numeric.grid.ResultWriterHDF5;

import neurord.util.Settings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SDCalc {
    public static final Logger log = LogManager.getLogger();

    final SDRun sdRun;

    final static String[] writers = Settings.getPropertyList("neurord.writers",
                                                             "Write output in those formats",
                                                             "h5");
    final static  int trials = Settings.getProperty("neurord.trials",
                                                    "How many trials to run",
                                                    1);
    final static int threads = Settings.getProperty("neurord.threads",
                                                    "How many threads to use (0 == #CPUs)",
                                                    0);

    protected final List<ResultWriter> resultWriters = new ArrayList<>();

    public SDCalc(SDRun sdr, File output) {
        this.sdRun = sdr;

        if (trials > 1 && sdr.simulationSeed > 0) {
            log.warn("Ignoring fixed simulation seed");
            sdr.simulationSeed = 0;
        }

        for (String type: writers) {
            final ResultWriter writer;
            final VolumeGrid grid = sdr.getVolumeGrid();
            final String[] species = sdr.getSpecies();
            if (type.equals("text")) {
                writer = new ResultWriterText(output, sdr, sdr.getOutputSets(), species, grid, false);
                log.info("Using text writer for {}", writer.outputFile());
            } else if (type.equals("h5")) {
                writer = new ResultWriterHDF5(output, sdr, sdr.getOutputSets(), species, grid);
                log.info("Using HDF5 writer for {}", writer.outputFile());
            } else {
                log.error("Unknown writer '{}'", type);
                throw new RuntimeException("uknown writer: " + type);
            }
            this.resultWriters.add(writer);
        }
    }

    protected BaseCalc prepareCalc(int trial) {
        SDCalcType calculationType = SDCalcType.valueOf(this.sdRun.calculation);
        BaseCalc calc = calculationType.getCalc(trial, this.sdRun);
        for (ResultWriter resultWriter: this.resultWriters)
                calc.addResultWriter(resultWriter);
        return calc;
    }

    public void run() {
        log.info("Beginning calculations ({} trials)", this.trials);

        if (trials == 1)
            this.prepareCalc(0).run();
        else {
            int poolSize = threads > 0 ? threads : Runtime.getRuntime().availableProcessors();
            ExecutorService pool = Executors.newFixedThreadPool(poolSize);
            log.info("Running with pool {}", pool);

            for (int i = 0; i < trials; i++) {
                log.info("Starting trial {}", i);
                pool.execute(this.prepareCalc(i));
            }

            log.info("Executing shutdown of pool {}", pool);
            pool.shutdown();
            while (true)
                try {
                    pool.awaitTermination(1, TimeUnit.MINUTES);
                    return;
                } catch(InterruptedException e) {
                    log.info("Waiting: {}", pool);
                }
        }
    }
}
