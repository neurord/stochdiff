package org.textensor.stochdiff.model;


import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.numeric.morph.SpineDistribution;
import org.textensor.stochdiff.numeric.morph.SpinePopulation;
import org.textensor.stochdiff.numeric.morph.TreePoint;

public class Morphology {

    @XmlElement(name="SpineType")
    public ArrayList<SpineType> spineTypes;

    @XmlElement(name="Segment")
    public ArrayList<Segment> segments;

    transient private ArrayList<MorphPoint> p_points;

    @XmlElement(name="SpineAllocation")
    private ArrayList<SpineAllocation> p_spineAllocations;

    transient private boolean resolved = false;

    public void resolve() {
        HashMap<String, SpineType> spineHM = new HashMap<>();
        if (spineTypes != null)
            for (SpineType st : spineTypes)
                spineHM.put(st.id, st);

        if (p_spineAllocations != null)
            for (SpineAllocation ss : p_spineAllocations)
                ss.resolve(spineHM);

        if (segments != null) {
            HashMap<String, Segment> segmentHM = new HashMap<>();
            for (Segment seg : segments)
                segmentHM.put(seg.getID(), seg);

            for (Segment seg : segments)
                seg.resolve(segmentHM);

            ArrayList<MorphPoint> wk = new ArrayList<>();
            for (Segment seg : segments) {
                wk.add(seg.getStart());
                wk.add(seg.getEnd());
            }

            p_points = new ArrayList<>();
            for (MorphPoint mp : wk)
                if (mp.redundant())
                    mp.transferConnections();
                else
                    p_points.add(mp);
        }

        resolved = true;
    }


    /* export the morphology from the Segement/MorphPoint format (which is
     * designed for IO) to the TreePoint format for the calculation
     */

    public TreePoint[] getTreePoints() {
        if (!resolved)
            resolve();

        ArrayList<TreePoint> tpts = new ArrayList<TreePoint>();
        HashMap<MorphPoint, TreePoint> mtHM = new HashMap<MorphPoint, TreePoint>();
        int ic = 0;
        for (MorphPoint mp : p_points) {
            TreePoint tp = mp.toTreePoint();
            tpts.add(tp);
            mtHM.put(mp, tp);
            tp.setWork(ic++);
        }
        for (MorphPoint mp : p_points) {
            TreePoint tp = mtHM.get(mp);
            for (MorphPoint pn : mp.getNeighbors()) {
                TreePoint tpn = mtHM.get(pn);
                if (tp.getWork() < tpn.getWork())
                    TreePoint.neighborize(tp, tpn);
            }

            for (MorphPoint pn : mp.segidHM.keySet())
                tp.setIDWith(mtHM.get(pn), mp.segidHM.get(pn));

            for (MorphPoint pn : mp.regionHM.keySet())
                tp.setRegionWith(mtHM.get(pn), mp.regionHM.get(pn));

            if (mp.hasOffsetChildren())
                for (MorphPoint oc : mp.getOffsetChildren()) {
                    TreePoint tpc = mtHM.get(oc);
                    tp.addOffsetChild(tpc);
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
