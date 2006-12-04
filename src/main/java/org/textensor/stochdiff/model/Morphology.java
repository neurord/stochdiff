package org.textensor.stochdiff.model;


import java.util.ArrayList;
import java.util.HashMap;


import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.morph.SpineDistribution;
import org.textensor.stochdiff.numeric.morph.SpinePopulation;
import org.textensor.stochdiff.numeric.morph.TreePoint;

public class Morphology implements AddableTo {

    public ArrayList<SpineType> spineTypes;

    public ArrayList<Segment> segments;

    boolean resolved = false;

    private ArrayList<MorphPoint> p_points;

    private ArrayList<SpineAllocation> p_spineAllocations;



    public void add(Object obj) {

        if (obj instanceof Segment) {
            if (segments == null) {
                segments = new ArrayList<Segment>();
            }
            segments.add((Segment)obj);

        } else if (obj instanceof SpineType) {
            if (spineTypes == null) {
                spineTypes = new ArrayList<SpineType>();
            }
            spineTypes.add((SpineType)obj);


        } else if (obj instanceof SpineAllocation) {
            if (p_spineAllocations == null) {
                p_spineAllocations = new ArrayList<SpineAllocation>();
            }
            p_spineAllocations.add((SpineAllocation)obj);

        } else {
            E.error("cant ad a " + obj.getClass() + " to a morphology");
        }
    }


    public void resolve() {
        HashMap<String, SpineType> spineHM = new HashMap<String, SpineType>();
        if (spineTypes != null) {
            for (SpineType st : spineTypes) {
                spineHM.put(st.id, st);
            }
        }

        if (p_spineAllocations != null) {
            for (SpineAllocation ss : p_spineAllocations) {
                ss.resolve(spineHM);
            }
        }

        if (segments == null) {
            // nothing to do;
        } else {
            HashMap<String, Segment> segmentHM = new HashMap<String, Segment>();
            for (Segment seg : segments) {
                segmentHM.put(seg.getID(), seg);
            }

            for (Segment seg : segments) {
                seg.resolve(segmentHM);
            }

            ArrayList<MorphPoint> wk = new ArrayList<MorphPoint>();
            for (Segment seg : segments) {
                wk.add(seg.getStart());
                wk.add(seg.getEnd());
            }

            p_points = new ArrayList<MorphPoint>();
            for (MorphPoint mp : wk) {
                if (mp.redundant()) {
                    mp.transferConnections();
                } else {
                    p_points.add(mp);
                }
            }

        }
        resolved = true;
    }


    /* export the morphology from the Segement/MorphPoint format (which is
     * designed for IO) to the TreePoint format for the calculation
     */

    public TreePoint[] getTreePoints() {
        if (!resolved) {
            resolve();
        }
        ArrayList<TreePoint> tpts = new ArrayList<TreePoint>();
        HashMap<MorphPoint, TreePoint> mtHM = new HashMap<MorphPoint, TreePoint>();
        int ic = 0;
        for (MorphPoint mp : p_points) {
            TreePoint tp = mp.toTreePoint();
            tpts.add(tp);
            mtHM.put(mp, tp);
            tp.setWork(ic);
            ic++;
        }
        for (MorphPoint mp : p_points) {
            TreePoint tp = mtHM.get(mp);
            for (MorphPoint pn : mp.getNeighbors()) {
                TreePoint tpn = mtHM.get(pn);
                if (tp.getWork() < tpn.getWork()) {
                    TreePoint.neighborize(tp, tpn);
                }
            }

            for (MorphPoint pn : mp.segidHM.keySet()) {
                tp.setIDWith(mtHM.get(pn), mp.segidHM.get(pn));
            }

            for (MorphPoint pn : mp.regionHM.keySet()) {
                tp.setRegionWith(mtHM.get(pn), mp.regionHM.get(pn));
            }


            if (mp.hasOffsetChildren()) {
                for (MorphPoint oc : mp.getOffsetChildren()) {
                    TreePoint tpc = mtHM.get(oc);
                    tp.addOffsetChild(tpc);
                }
            }
        }

        TreePoint[] ret = tpts.toArray(new TreePoint[0]);
        return ret;
    }




    public SpineDistribution getSpineDistribution() {

        ArrayList<SpinePopulation> spa = new ArrayList<SpinePopulation>();
        if (p_spineAllocations != null) {
            for (SpineAllocation sa: p_spineAllocations) {
                SpinePopulation sp = sa.makePopulation();
                if (sp != null) {
                    spa.add(sp);
                }
            }
        }
        SpinePopulation[] pa = spa.toArray(new SpinePopulation[0]);

        SpineDistribution sd = new SpineDistribution(pa);

        return sd;

    }


}
