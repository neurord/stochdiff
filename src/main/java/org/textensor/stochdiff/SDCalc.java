package org.textensor.stochdiff;

import java.io.File;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;


public class SDCalc {


    SDCalcType calculationType;

    SDRun sdRun;

    ResultWriter resultWriter;


    BaseCalc bCalc;

    public SDCalc(SDRun sdr, File outputFile) {
        sdRun = sdr;
        String sr = sdRun.calculation;

        if (sdRun.continueOutput() && outputFile.exists() && sdRun.getStartTime() > 0) {
            resultWriter = new ResultWriter(outputFile);
            resultWriter.pruneFrom("gridConcentrations", 3, sdRun.getStartTime());


        } else {
            resultWriter = new ResultWriter(outputFile);
        }


        for (SDCalcType  sdct : SDCalcType.values()) {
            if (sdct.hasLabel(sr)) {
                calculationType = sdct;
            }
        }
        if (calculationType == null) {
            E.warning("unrecognized calculation type " + sr);
        }
    }



    public void run() {
        bCalc = calculationType.getCalc(sdRun);
        if (resultWriter != null) {
            bCalc.setResultWriter(resultWriter);
        }
        bCalc.run();

        if (resultWriter != null) {
            resultWriter.close();
        }

    }


    public long getParticleCount() {
        return bCalc.getParticleCount();
    }




}
