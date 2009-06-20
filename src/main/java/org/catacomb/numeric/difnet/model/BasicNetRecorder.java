package org.catacomb.numeric.difnet.model;

import java.util.ArrayList;

import org.catacomb.numeric.difnet.NetState;
import org.catacomb.report.E;





public class BasicNetRecorder {

    ArrayList<PointRecorder> pointRecorders;

    int nrec;
    String[] recLabels;
    int[] recIndices;


    public BasicNetRecorder() {
        pointRecorders = new ArrayList<PointRecorder>();
    }

    public void addPotentialRecorder(String id, String tag, String probePort) {
        pointRecorders.add(new PointRecorder(id, tag, probePort));
    }


    public void resolve(int[] remeshMap) {
        nrec = pointRecorders.size();
        recLabels = new String[nrec];
        recIndices = new int[nrec];

        int iel = 0;
        for (PointRecorder pr : pointRecorders) {
            recLabels[iel] = pr.getID();

            String sp = pr.getPort();
            if (sp == null || sp.length() == 0) {
                E.warning("null port on probe " + pr);
            } else {
                try {
                    int ipt = Integer.parseInt(sp);
                    if (ipt < 0) {
                        E.warning("negative port id in " + pr);

                    } else {
                        recIndices[iel] = remeshMap[ipt];

                        //E.info("new index for rec " + iel + " original pt " + ipt +
                        //      " is " + recIndices[iel]);
                    }


                } catch (Exception ex) {
                    E.error("must have integer port ids, not " + sp);
                }
            }

            iel += 1;
        }
    }



    public String[] getRecorderLabels() {
        return recLabels;
    }

    public double[] getValues(NetState netState) {
        double[] ret = new double[nrec];
        for (int i = 0; i < nrec; i++) {
            ret[i] = netState.getValueAt(recIndices[i]);
        }
        return ret;
    }

}




class PointRecorder {

    String id;
    String tag;
    String port;

    PointRecorder(String sid, String stag, String pp) {
        id = sid;
        tag = stag;
        port = pp;
    }

    public String getID() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public String getPort() {
        return port;
    }

}