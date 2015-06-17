package org.textensor.stochdiff.inter;

import java.io.File;
import java.util.StringTokenizer;

import org.textensor.util.FileUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class StateReader {
    static final Logger log = LogManager.getLogger(StateReader.class);

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
                    log.warn("wrong length for specie list {}, {}", bits.length, ret.nspec);
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
            log.warn("ignoring initial conditions file");
            ret = null;
        }
        return ret;
    }
}
