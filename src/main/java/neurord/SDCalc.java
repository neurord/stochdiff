package neurord;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
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
import neurord.util.Logging;

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
    protected final Hashtable<Integer, Object> results;

    public SDCalc(SDRun sdr, File output) {
        this.sdRun = sdr;

        for (String type: writers) {
            final ResultWriter writer;
            final VolumeGrid grid = sdr.getVolumeGrid();
            final String[] species = sdr.getSpecies();
            if (type.equals("text")) {
                writer = new ResultWriterText(output,
                                              sdr,
                                              sdr.getOutputSets(),
                                              species,
                                              grid,
                                              false);
                log.info("Using text writer for {}", writer.outputFile());
            } else if (type.equals("h5")) {
                writer = new ResultWriterHDF5(output,
                                              sdr,
                                              sdr.getOutputSets(),
                                              species,
                                              grid);
                log.info("Using HDF5 writer for {}", writer.outputFile());
            } else {
                log.error("Unknown writer '{}'", type);
                throw new RuntimeException("uknown writer: " + type);
            }
            this.resultWriters.add(writer);
        }

        this.results = new Hashtable<>(trials);
    }

    protected BaseCalc prepareCalc(int trial) {
        SDCalcType calculationType = SDCalcType.valueOf(this.sdRun.calculation);
        BaseCalc calc = calculationType.getCalc(trial, this.sdRun);
        for (ResultWriter resultWriter: this.resultWriters)
                calc.addResultWriter(resultWriter);
        calc.storeResultIn(this.results);
        return calc;
    }

    public int run() {
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
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            } catch(InterruptedException e) {
                log.info("Interrupted while waiting for tasks to finish: {}", pool);
            }
        }

        boolean good = true;
        for (int i = 0; i < trials; i++) {
            Object result = this.results.get(i);
            if (result == null) {
                good = false;
                log.error("Trial {} did not finish correctly!", i);
            } else if (result instanceof Throwable) {
                good = false;
                log.error("Trial {} failed!", i, result);
            } else
                log.debug("Trial {} succeeded", i);
        }

        return good ? 0 : 1;
    }
}
