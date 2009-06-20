package org.catacomb.numeric.difnet.model;

import java.util.ArrayList;

import org.catacomb.numeric.difnet.NetState;
import org.catacomb.numeric.difnet.StateNode;
import org.catacomb.numeric.difnet.Stimulus;
import org.catacomb.numeric.function.ScalarFunction;
import org.catacomb.report.E;





public class BasicNetStim {

    ArrayList<PointStim> pointStims;

    public BasicNetStim() {
        pointStims = new ArrayList<PointStim>();
    }


    public void addCurrentStim(String id, String tag, String probePort,
                               ScalarFunction sfg) {
        pointStims.add(new PointCurrentStim(id, tag, probePort, sfg));
    }

    public void addVoltageStim(String id, String tag, String probePort,
                               ScalarFunction sf) {
        pointStims.add(new PointVoltageStim(id, tag, probePort, sf));
    }




    public void resolve(int[] remeshMap) {
        for (PointStim pr : pointStims) {
            String sp = pr.getPort();
            if (sp == null || sp.length() == 0) {
                E.warning("null port on probe " + pr);
            } else {
                try {
                    int ipt = Integer.parseInt(sp);
                    if (ipt < 0) {
                        E.warning("negative port id in " + pr);

                    } else {
                        int iti = remeshMap[ipt];
                        pr.setTargetIndex(iti);
                    }

                } catch (Exception ex) {
                    E.error("must have integer port ids, not " + sp);
                }
            }
        }
    }



    public void attachTo(NetState netState) {
        for (PointStim pr : pointStims) {
            int iti = pr.getTargetIndex();
            if (iti >= 0) {
                pr.attachTo(netState.getNode(iti));
            }
        }
    }

}




abstract class PointStim {

    String id;
    String tag;
    String port;

    int targetIndex;

    PointStim(String sid, String stag, String pp) {
        id = sid;
        tag = stag;
        port = pp;
        targetIndex = -1;
    }

    void setTargetIndex(int i) {
        targetIndex = i;
    }

    int getTargetIndex() {
        return targetIndex;
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

    abstract void attachTo(StateNode snode);
}


class PointCurrentStim extends PointStim implements Stimulus {
    ScalarFunction sfuncC;

    PointCurrentStim(String sid, String stag, String pp, ScalarFunction sfg) {
        super(sid, stag, pp);
        sfuncC = sfg;
    }

    void attachTo(StateNode snode) {
        snode.setStimulus(this);
    }

    public double getValue(double t) {
        return sfuncC.getScalar(t);
    }


    public int getType() {
        return Stimulus.FLUX;
    }

}



class PointVoltageStim extends PointStim implements Stimulus {
    ScalarFunction sfunc;

    PointVoltageStim(String sid, String stag, String pp, ScalarFunction sf) {
        super(sid, stag, pp);
        sfunc = sf;
    }


    void attachTo(StateNode snode) {
        snode.setStimulus(this);
    }


    public double getValue(double t) {
        double d = sfunc.getScalar(t);
        return d;
    }


    public int getType() {
        return Stimulus.VALUE;
    }

}







