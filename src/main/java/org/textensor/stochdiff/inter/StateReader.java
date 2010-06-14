package org.textensor.stochdiff.inter;

import java.io.File;
import java.util.StringTokenizer;

import org.textensor.report.E;
import org.textensor.util.FileUtil;

public class StateReader {



    public static SDState readState(String fnm) {
        String s = FileUtil.readStringFromFile(new File(fnm));
        return readStateString(s);
    }


    public static SDState readStateString(String sdata) {
        SDState ret = new SDState();

        boolean readok = true;

        StringTokenizer st = new StringTokenizer(sdata, "\n");
        if (st.hasMoreTokens()) {
            String[] bits = st.nextToken().split(" ");
            ret.nel = Integer.parseInt(bits[1]);
            ret.nspec = Integer.parseInt(bits[2]);

        } else {
            readok = false;
        }
        if (readok) {
            if (st.hasMoreTokens()) {
                String[] bits = st.nextToken().split(" ");

                if (bits.length != ret.nspec) {
                    E.error("wrong length for species lists " + bits.length + " " + ret.nspec);
                    readok = false;
                } else {
                    ret.specids = bits;
                }
            } else {
                readok = false;
            }
        }
        double[][] conc = new double[ret.nel][ret.nspec];
        if (readok) {
            for (int iel = 0; iel < ret.nel; iel++) {
                if (st.hasMoreTokens()) {
                    String[] bits =  st.nextToken().split(" ");
                    if (bits.length == ret.nspec) {
                        for (int j = 0; j < ret.nspec; j++) {
                            conc[iel][j] = Double.parseDouble(bits[j]);
                        }
                    }
                } else {
                    readok = false;
                    break;
                }
            }
        }
        ret.conc = conc;

        if (!readok) {
            E.warning("ignoring initial conditions file");
            ret = null;
        }
        return ret;
    }




}
