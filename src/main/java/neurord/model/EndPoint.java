package neurord.model;

import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class EndPoint extends MorphPoint {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public String on;
    @XmlAttribute public String at;
    @XmlAttribute public Double atFraction;

    private EndPoint() { }

    public EndPoint(String id, double x, double y, double z, double r) {
        super(id, x, y, z, r);
    }

    public void resolve(HashMap<String, Segment> segmentHM, MorphPoint ep) {
        if (on != null) {
            Segment tgtSeg = segmentHM.get(on);
            if (tgtSeg == null) {
                log.error("point refers to segment '{}' but that segment cannot be found", on);
                throw new RuntimeException("point refers to segment " + on + " but that segment cannot be found");
            }

            if (at != null) {
                if (at.equals("start"))
                    pointConnect(tgtSeg.getStart());
                else if (at.equals("end"))
                    pointConnect(tgtSeg.getEnd());
                else {
                    log.error("connection to segment: at value can only be 'start' or 'end', not '{}'", at);
                    throw new RuntimeException("connection to segment: at value can only be 'start' or 'end', not " + at);
                }

            } else if (atFraction != null) {

                if (atFraction == 0) {
                    pointConnect(tgtSeg.getStart());

                } else if (atFraction == 1) {
                    pointConnect(tgtSeg.getEnd());

                } else {
                    tgtSeg.checkResolved(segmentHM);
                    tgtSeg.checkHasPositions();

                    interiorPointConnect(tgtSeg, atFraction, ep);
                }
            } else {
                log.error("must either set 'at' or 'atFraction' if 'on' is specified");
                throw new RuntimeException("must either set 'at' or 'atFraction' if 'on' is specified");
            }
        }

    }

    private void supplySize(double x, double y, double z, double r) {
        if (this.x == null)
            this.x = x;
        if (this.y == null)
            this.y = y;
        if (this.z == null)
            this.z = z;
        if (Double.isNaN(this.r))
            this.r = r;
    }


    public boolean radiiDiffer(MorphPoint mp) {
        return Math.abs((r - mp.r) / (r + mp.r)) > 1.e-4;
    }


    private void pointConnect(MorphPoint target) {
        log.debug("pointConnect: {} to {}", this, target);
        this.supplySize(target.x, target.y, target.z, target.r);

        if (this.radiiDiffer(target)) {
            target.addNeighbor(this);
            this.addNeighbor(target);
        } else
            this.r_peerPoint = target;
    }

    public void interiorPointConnect(Segment tgtSeg, double f, MorphPoint twds) {
        MorphPoint st = tgtSeg.getStart();
        MorphPoint ed = tgtSeg.getEnd();

        double wf = 1. - f;
        double cx = wf * st.x + f * ed.x;
        double cy = wf * st.y + f * ed.y;
        double cz = wf * st.z + f * ed.z;
        double cr = wf * st.r + f * ed.r;

        //  double rr = (Double.isNaN(r) ? cr : r);

        // the attachment position should be offset cr towards the end of the
        // new segment;
        double dx = (twds.x -cx);
        double dy = (twds.y -cy);
        double dz = (twds.z -cz);
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);

        supplySize(cx + cr * dx/d, cy + cr * dy/d, cz * cr * dz/d, cr);

        st.addOffsetChild(this);
    }

    public String toString() {
        String on = this.on != null ? " on=" + this.on : "";
        String at = this.at != null ? " at=" + this.at : "";
        String atFraction = this.atFraction != null ? " atFraction=" + this.atFraction : "";
        return super.toString() + on + at + atFraction;
    }
}
