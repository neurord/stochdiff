package org.textensor.stochdiff.model;

import org.textensor.stochdiff.numeric.morph.SpineProfile;

import java.util.ArrayList;

public class SpineType {

    public String id;

    public ArrayList<Section> sections;

    private SpineProfile r_profile;

    public SpineProfile makeProfile() {
        int np = sections.size();
        double[] vl = new double[np];
        double[] vw = new double[np];
        String[] lbls = new String[np];
        String[] regions = new String[np];
        for (int i = 0; i < np; i++) {
            Section sec = sections.get(i);
            vl[i] = sec.at;
            vw[i] = sec.width;
            if (sec.regionClass != null) {
                regions[i] = sec.regionClass;
            }
            if (sec.label != null) {
                lbls[i] = sec.label;
            }
        }
        SpineProfile ret = new SpineProfile(id, vl, vw, lbls, regions);
        return ret;
    }



    public SpineProfile getProfile() {
        if (r_profile == null) {
            r_profile = makeProfile();
        }
        return r_profile;
    }


}
