package neurord.neuroml;

import java.util.ArrayList;
import java.util.HashMap;

import neurord.inter.Transitional;
import neurord.model.Morphology;
import neurord.model.Segment;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class cell implements Transitional {
    static final Logger log = LogManager.getLogger();

    public String name;

    public ArrayList<segment> segments;

    public ArrayList<cable> cables;

    public ArrayList<MorphMLCableGroup> cableGroups;

    public String notes;

    HashMap<String, MorphMLPoint> srcptHM;

    //HashMap<String, MorphPoint> ptHM;
    // ArrayList<MorphPoint> points;

    // public NeuroMLBiophysics biophysics;

    meta meta;

    public ArrayList<segment> getSegments() {
        return segments;
    }

    public Object getFinal() {
        return getStochDiffMorphology();
    }

    public Morphology getStochDiffMorphology() {
        Morphology ret = new Morphology();

        HashMap<String, String> cableHM = new HashMap<String, String>();
        for (cable c : cables) {
            cableHM.put(c.getID(), c.getLabel());
        }


        ArrayList<segment> segs = getSegments();


        for (segment seg : segs) {
            Segment s = seg.getStochDiffSegment(cableHM);
            ret.segments.add(s);

            log.info("Added segment {}", s);
        }

        return ret;
    }




    /*

    public CellMorphology getCellMorphology() {
    	return getCellMorphology(name);
    }

    public CellMorphology getCellMorphology(String id) {
    	// E.info("finalizing from " + this + " " + setOfPoints.size() + " nseg=" + cell.getSegments().size());

    	boolean gotRoot = false;

    	ptHM = new HashMap<String, MorphPoint>();
    	points = new ArrayList<>();


    	ArrayList<MorphMLSegment> segs = getSegments();

    	for (MorphMLSegment seg : segs) {

    		String pid = seg.getParentID();

    		if (pid == null) {
    			if (gotRoot) {
    				E.error("multiple points with no parent?");
    			}
    			gotRoot = true;
    			// only allowed once in cell - defines the root segment
    			MorphPoint rpp = getOrMakePoint(seg.getProximal(), "rootpoint");
    			MorphPoint rpc = getOrMakePoint(seg.getDistal(), seg.id);
    			rpc.setParent(rpp);
    			rpc.minor = true;
    			String sn = seg.getName();
    			if (sn != null) {
    				rpp.addLabel(sn);
    				rpc.addLabel(sn);
    			}


    		} else {
    			MorphPoint rpp = ptHM.get(pid);
    			MorphPoint rpc = getOrMakePoint(seg.getDistal(), seg.getID());
    			rpc.minor = true;
    			String sn = seg.getName();
    			if (sn != null) {
    				rpc.addLabel(sn);
    			}

    			if (seg.getProximal() != null) {
    				// could be better to attach to rpp.parent, rather than rpp
    				MorphMLPoint p = seg.getProximal();
    				MorphPoint w = new MorphPoint(p.getID(), p.getX(), p.getY(), p.getZ(), p.getR());
    				if (rpp.getParent() != null &&
    						distanceBetween(w, rpp.getParent()) < distanceBetween(w, rpp)) {
    				   rpp = (MorphPoint)rpp.getParent();
    				   rpc.minor = true;
    				}
    			}
    			rpc.setParent(rpp);
    		}
    	}

    	CellMorphology cm = new CellMorphology();
    	cm.id = id;
    	cm.setPoints(points);
    	cm.resolve();
     //    E.info("returning MorphML import " + cm);

        cm.checkConnected();

        return cm;
    }









    private double distanceBetween(Point a, Point b) {
    	 return Geom.distanceBetween(a.getPosition(), b.getPosition());
    }



    private MorphPoint getOrMakePoint(MorphMLPoint p, String id) {
    	MorphPoint ret = null;
    		if (ptHM.containsKey(id)) {
    			ret = ptHM.get(id);
    		} else {

    		//	E.info("new point at " + sp.getID() + " " + sp.getX() + " " + sp.getY() + " " + sp.getR());
    			ret = new MorphPoint(id, p.getX(), p.getY(), p.getZ(), p.getR());
    			ptHM.put(id, ret);
    			points.add(ret);
    		}


    		return ret;
    	}
    */
}
