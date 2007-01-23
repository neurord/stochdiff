package org.textensor.stochdiff;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;


public class SDCalc {


    SDCalcType calculationType;

    SDRun sdRun;

    ResultWriter resultWriter;


    BaseCalc bCalc;

    public SDCalc(SDRun sdr) {
        sdRun = sdr;
        String sr = sdRun.calculation;

        for (SDCalcType  sdct : SDCalcType.values()) {
            if (sdct.hasLabel(sr)) {
                calculationType = sdct;
            }
        }
        if (calculationType == null) {
            E.warning("unrecognized calculation type " + sr);
        }
    }


    public void setResultWriter(ResultWriter rw) {
        resultWriter = rw;
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


    public Long getParticleCount() {
        return bCalc.getParticleCount();
    }




}
