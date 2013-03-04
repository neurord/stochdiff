package org.textensor.stochdiff;

import java.io.File;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.grid.ResultWriter;
import org.textensor.stochdiff.numeric.grid.ResultWriterText;

public class SDCalc {


    SDCalcType calculationType;

    SDRun sdRun;

    final ResultWriter resultWriter;

    BaseCalc bCalc;

    public SDCalc(SDRun sdr, File outputFile) {
        sdRun = sdr;
        String sr = sdRun.calculation;

        resultWriter = new ResultWriterText(outputFile, false);
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
        bCalc.setResultWriter(resultWriter);
        ret = bCalc.run();

        resultWriter.close();
        return ret;
    }


    public long getParticleCount() {
        return bCalc.getParticleCount();
    }




}
