package org.textensor.stochdiff;

import java.io.File;
import java.util.List;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.grid.ResultWriter;
import org.textensor.stochdiff.numeric.grid.ResultWriterText;
import org.textensor.stochdiff.numeric.grid.ResultWriterHDF5;

import org.textensor.util.inst;

public class SDCalc {


    SDCalcType calculationType;

    SDRun sdRun;

    protected final List<ResultWriter> resultWriters = inst.newArrayList();

    BaseCalc bCalc;

    public SDCalc(SDRun sdr, File outputFile) {
        sdRun = sdr;
        String sr = sdRun.calculation;

        this.resultWriters.add(new ResultWriterText(outputFile, false));
        this.resultWriters.add(new ResultWriterHDF5(outputFile));


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
