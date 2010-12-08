package org.textensor.vis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import org.textensor.report.E;

public class ElementReader {

    File fsrc;

    ArrayList<VolElt> elts;

    public ElementReader(File f) {
        fsrc = f;
        elts = new ArrayList<VolElt>();
    }

    public void read() {
        try {

            BufferedReader br = new BufferedReader(new FileReader(fsrc));
            StreamTokenizer tz = new StreamTokenizer(br);
            tz.eolIsSignificant(false);

            while (br.ready()) {
                int ielt =  nextInt(tz);
                int nstrip = nextInt(tz);
                int nvert = nextInt(tz);

                int[] slens = new int[nstrip];
                for (int i = 0; i < nstrip; i++) {
                    slens[i] = nextInt(tz);
                }
                float[] verts = new float[3 * nvert];
                float[] norms = new float[3 * nvert];

                int ind = 0;
                for (int i = 0; i < nvert; i++) {
                    verts[ind] = nextFloat(tz);
                    verts[ind+1] = nextFloat(tz);
                    verts[ind+2] = nextFloat(tz);

                    norms[ind] = nextFloat(tz);
                    norms[ind+1] = nextFloat(tz);
                    norms[ind+2] = nextFloat(tz);

                    ind += 3;
                }

                VolElt ve = new VolElt(slens, verts, norms);
                elts.add(ve);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        E.info("loaded " + elts.size() + " curved volume elements");
    }

    private int nextInt(StreamTokenizer tz) throws IOException {
        int itok = tz.nextToken();
        int ret = 0;
        if (tz.ttype == StreamTokenizer.TT_NUMBER) {
            ret = (int)(Math.round(tz.nval));

        } else {
            throw new IOException("need a number but got " + tz);
        }
        return ret;
    }


    private float nextFloat(StreamTokenizer tz) throws IOException {
        int itok = tz.nextToken();
        float ret = 0;
        if (tz.ttype == StreamTokenizer.TT_NUMBER) {
            ret = (float)(tz.nval);

        } else {
            throw new IOException("need a number but got " + tz);
        }
        return ret;
    }

    public ArrayList<VolElt> getElements() {
        return elts;
    }
}
