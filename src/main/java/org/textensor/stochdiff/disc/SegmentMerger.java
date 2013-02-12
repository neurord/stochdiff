package org.textensor.stochdiff.disc;

import java.util.ArrayList;

import org.textensor.stochdiff.numeric.morph.TreePoint;

/* This will remove points that divide segments into too many
 * sections, subject to criteria on the allowed change in radius and
 * the maximum length.
 */



public class SegmentMerger {

    private TreePoint[] merge(TreePoint[] pt, double maxdr, double maxlen, double tol) {


        for (int i = 0; i < pt.length; i++) {
            pt[i].setWork(2);
        }
        TreePoint p0 = pt[0];
        recMerge(p0, maxdr, maxlen, tol);

        // remove dead points.
        int nl = 0;
        for (int i = 0; i < pt.length; i++) {
            if (pt[i].getWork() > 0)
                nl++;
        }
        TreePoint[] pr = new TreePoint[nl];
        nl = 0;
        for (int i = 0; i < pt.length; i++) {
            if (pt[i].getWork() > 0)
                pr[nl++] = pt[i];
        }
        return pr;
    }


    private void recMerge(TreePoint cp, double maxdr, double maxlen, double tol) {
        // may have already been done
        cp.setWork(1);

        for (int j = 0; j < cp.nnbr; j++) {
            TreePoint cq = cp.nbr[j];
            if (cq.getWork() == 2 && cq.nnbr == 2
                    && Math.abs((cq.r - cp.r) / (cq.r + cp.r)) < 0.5 * maxdr
                    && cp.distanceTo(cq) < maxlen) {
                // if nnbr is not two, either it is a terminal, or a branch point:
                // in either case there is nothing to do;

                TreePoint cprev = cp;
                ArrayList<TreePoint> vpt = new ArrayList<TreePoint>();
                double ltot = 0.;
                double ldtot = 0.;
                while (cq.nnbr == 2 && cprev.distanceTo(cq) < maxlen
                        && Math.abs((cq.r - cp.r) / (cq.r + cp.r)) < 0.5 * maxdr) {
                    vpt.add(cq);
                    double dl = cprev.distanceTo(cq);
                    ltot += dl;
                    ldtot += dl * (cprev.r + cq.r);

                    TreePoint cnxt = (cq.nbr[0] == cprev ? cq.nbr[1] : cq.nbr[0]);
                    cprev = cq;
                    cq = cnxt;
                }
                double dl = cprev.distanceTo(cq);
                ltot += dl;
                ldtot += dl * (cprev.r + cq.r);


                double lab = cp.distanceTo(cq);
                double ldab = lab * (cp.r + cq.r);
                int nadd = (int)(ltot / maxlen);
                if (nadd > vpt.size())
                    nadd = vpt.size(); // cannot happen at present;
                // recycle nadd points;

                boolean cor = (Math.abs((lab - ltot) / (lab + ltot)) > 0.5 * tol || Math.abs((ldab - ldtot)
                               / (ldab + ldtot)) > 0.5 * tol);
                if (cor && nadd == 0)
                    nadd = 1;

                if (nadd == 0) {
                    cp.replaceNeighbor(vpt.get(0), cq);
                    cq.replaceNeighbor(vpt.get(vpt.size() - 1), cp);
                } else {
                    for (int i = 0; i < nadd; i++) {
                        TreePoint cm = vpt.get(i);
                        cm.setWork(1);
                        cm.locateBetween(cp, cq, (1. + i) / (1. + nadd));
                        if (i == nadd - 1 && nadd < vpt.size()) {
                            cm.replaceNeighbor(vpt.get(nadd), cq);
                            cq.replaceNeighbor(vpt.get(vpt.size() - 1), cm);
                        }
                    }
                }


                // just kill the rest;
                for (int jd = nadd; jd < vpt.size(); jd++) {
                    TreePoint cd = vpt.get(jd);
                    cd.nnbr = 0;
                    cd.setWork(0);
                }


                if (cor) {
                    double dpar = lab / (nadd + 1);
                    double fl = ltot / lab;
                    double dperp = Math.sqrt((fl * fl - 1) * dpar * dpar);
                    double fr = ldtot / (fl * ldab);
                    for (int i = 0; i < nadd; i++) {
                        TreePoint cm = (vpt.get(i));
                        cm.r *= fr;
                        if (i % 2 == 0) {
                            cm.movePerp(cp, cq, ((i / 2) % 2 == 0 ? dperp : -dperp));
                        }
                    }

                }



            }
            if (cq.getWork() == 2)
                recMerge(cq, maxdr, maxlen, tol);
        }
    }


}
