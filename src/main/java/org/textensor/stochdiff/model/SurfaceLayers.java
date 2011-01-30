package org.textensor.stochdiff.model;

import java.util.StringTokenizer;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.BodyValued;


public class SurfaceLayers implements BodyValued {


    public double[] values;

    String sbod = "";

    public void setBodyValue(String s) {
        sbod += s + " ";
        StringTokenizer st = new StringTokenizer(sbod, " ,");
        int ntok = st.countTokens();
        values = new double[ntok];
        for (int i = 0; i < ntok; i++) {
            values[i] = Double.parseDouble(st.nextToken());
        }
    }

    public double[] getValues() {
        return values;
    }

}
