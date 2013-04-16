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

    SDCalcType calculationType;

    SDRun sdRun;

    protected final List<ResultWriter> resultWriters = inst.newArrayList();

    BaseCalc bCalc;

    public SDCalc(SDRun sdr, File outputFile) {
        sdRun = sdr;
        String sr = sdRun.calculation;

        final String[] writers = Settings.getPropertyList("stochdiff.writers",
                                                          "text");

        for (String type: writers) {
            final ResultWriter writer;
            if (type.equals("text"))
                writer = new ResultWriterText(outputFile, false);
            else if (type.equals("h5"))
                writer = new ResultWriterHDF5(outputFile);
            else {
                log.error("Unknown writer '{}'", type);
                throw new RuntimeException("uknown writer: " + type);
            }
            this.resultWriters.add(writer);
        }

        //        if (sdRun.continueOutput() && outputFile.exists() && sdRun.getStartTime() > 0)
        //            resultWriter.pruneFrom("gridConcentrations", 3, sdRun.getStartTime());

        for (SDCalcType  sdct : SDCalcType.values()) {
            if (sdct.hasLabel(sr)) {
                calculationType = sdct;
            }
        }
        if (calculationType == null) {
            E.warning("unrecognized calculation type " + sr);
        }
    }



    public int run() {
        int ret = 0;
        bCalc = calculationType.getCalc(sdRun);
        for (ResultWriter resultWriter: this.resultWriters)
            bCalc.addResultWriter(resultWriter);
        ret = bCalc.run();

        for (ResultWriter resultWriter: this.resultWriters)
            resultWriter.close();
        return ret;
    }


    public long getParticleCount() {
        return bCalc.getParticleCount();
    }
}
