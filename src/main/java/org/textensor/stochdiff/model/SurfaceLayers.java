package org.textensor.stochdiff.model;

import java.util.StringTokenizer;

public class SurfaceLayers {

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
