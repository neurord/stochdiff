package org.textensor.stochdiff;

import java.io.File;
import java.util.List;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.grid.ResultWriter;
import org.textensor.stochdiff.numeric.grid.ResultWriterText;
import org.textensor.stochdiff.numeric.grid.ResultWriterHDF5;

import org.textensor.util.Settings;
import org.textensor.util.inst;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SDCalc {
    static final Logger log = LogManager.getLogger(SDCalc.class);

    final SDRun sdRun;
    final SDCalcType calculationType;

    protected final List<ResultWriter> resultWriters = inst.newArrayList();

    BaseCalc bCalc;

    public SDCalc(SDRun sdr, File output) {
        this.sdRun = sdr;
        this.calculationType = SDCalcType.valueOf(sdr.calculation);

        final String[] writers = Settings.getPropertyList("stochdiff.writers",
                                                          "text");

        for (String type: writers) {
            final ResultWriter writer;
            if (type.equals("text")) {
                writer = new ResultWriterText(output, false);
                log.info("Using text writer for {}", writer.outputFile());
            } else if (type.equals("h5")) {
                writer = new ResultWriterHDF5(output);
                log.info("Using HDF5 writer for {}", writer.outputFile());
            } else {
                log.error("Unknown writer '{}'", type);
                throw new RuntimeException("uknown writer: " + type);
            }
            this.resultWriters.add(writer);
        }

        //        if (sdRun.continueOutput() && outputFile.exists() && sdRun.getStartTime() > 0)
        //            resultWriter.pruneFrom("gridConcentrations", 3, sdRun.getStartTime());
    }

    public int run() {
        int ret = 0;
        bCalc = calculationType.getCalc(this.sdRun);
        for (ResultWriter resultWriter: this.resultWriters)
            bCalc.addResultWriter(resultWriter);
        return bCalc.run();
    }

    public void close() {
        for (ResultWriter resultWriter: this.resultWriters)
            resultWriter.close();
    }

    public long getParticleCount() {
        return bCalc.getParticleCount();
    }
}
