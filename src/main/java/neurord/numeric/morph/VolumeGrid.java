package neurord.numeric.morph;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.HashMap;

import neurord.disc.CurvedVolumeSlice;
import neurord.geom.Geom;
import neurord.geom.Position;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class VolumeGrid {
    static final Logger log = LogManager.getLogger();

    public enum geometry_t {
        GEOM_2D,
        GEOM_3D;

        public static geometry_t fromString(String sg) {
            if (sg.toLowerCase().equals("2d"))
                return GEOM_2D;
            if (sg.toLowerCase().equals("3d"))
                return GEOM_3D;
            throw new RuntimeException("unrecognized geometry " + sg + " should be 2D or 3D");
        }
    }

    ArrayList<VolumeElement> elements = new ArrayList<>();

    HashMap<String, ArrayList<VolumeElement>> regionHM = new HashMap<>();

    int nelement;
    String[] eltLabels;
    String[] eltGroupIDs;
    String[] regionLabels;
    int[] eltRegions;


    double[] volumes;
    double[][] positions;
    double[] exposedAreas;

    //<--WK
    boolean[] submembranes;
    //WK-->

    int nconnection;
    int[][] conI;
    double[] conG;

    int[][] eltNbrs;
    double[][] eltNbrG;

    boolean hasCuboids = false;
    boolean hasCurveds = false;

    public void importSlices(ArrayList<VolumeSlice> slices) {
        this.hasCuboids = true;
        for (VolumeSlice slice : slices)
            this.addElements(slice.getElements());
    }

    public void importSmoothSlices(ArrayList<CurvedVolumeSlice> slices) {
        this.hasCurveds = true;
        for (CurvedVolumeSlice slice : slices)
            this.addElements(slice.getElements());
    }

    public void importLines(ArrayList<VolumeLine> lines) {
        this.hasCuboids = true;
        for (VolumeLine line : lines)
            this.addElements(line.getElements());
    }

    public void addElements(List<? extends VolumeElement> elements) {
        for (VolumeElement ve : elements)
            this.addVolumeElement(ve);
    }

    private void addVolumeElement(VolumeElement ve) {
        ve.setNumber(this.elements.size());
        this.elements.add(ve);

        final String region = ve.getRegion();
        if (region == null) {
            log.debug("VolumeElement has null region and cannot be accessed");
            return;
        }

        log.debug("Processing VolumeElement {} from region {}", ve.getAsText(), region);

        if (regionHM.containsKey(region)) {
            regionHM.get(region).add(ve);
        } else {
            ArrayList<VolumeElement> ave = new ArrayList<>();
            ave.add(ve);
            regionHM.put(region, ave);
        }
    }

    public VolumeElement getElement(int i) {
        VolumeElement el = this.elements.get(i);
        assert el.getNumber() == i;
        return el;
    }

    public ArrayList<VolumeElement> getElementsInRegion(String reg) {
        if (regionHM.containsKey(reg))
            return regionHM.get(reg);
        else
            return new ArrayList<>();
    }

    // switch from collections to arrays for the calculation
    public void fix() {
        ArrayList<ElementConnection> connections = new ArrayList<>();
        for (VolumeElement ve : elements) {
            connections.addAll(ve.getConnections());
        }

        ArrayList<String> rA = new ArrayList<String>();
        rA.add("default");

        nelement = elements.size();
        volumes = new double[nelement];
        exposedAreas = new double[nelement];
        positions = new double[nelement][3];
        eltLabels = new String[nelement];
        eltGroupIDs = new String[nelement];
        eltRegions = new int[nelement];
        submembranes = new boolean[nelement];

        for (int i = 0; i < nelement; i++) {
            VolumeElement ve = elements.get(i);

            volumes[i] = ve.getVolume();
            exposedAreas[i] = ve.getExposedArea();

            positions[i][0] = ve.getX();
            positions[i][1] = ve.getY();
            positions[i][2] = ve.getZ();

            eltLabels[i] = ve.getLabel();
            eltGroupIDs[i] = ve.getGroupID();
            submembranes[i] = ve.isSubmembrane();

            String sr = ve.getRegion();
            if (sr == null || sr.length() == 0) {
                eltRegions[i] = 0;

            } else if (rA.contains(sr)) {
                eltRegions[i] = rA.indexOf(sr);

            } else {
                int nn = rA.size();
                eltRegions[i] = nn;
                rA.add(sr);
            }

            /*      System.out.println("VE " + i + ": volume " + volumes[i] +
                         " exposedArea " + exposedAreas[i] +
                         " position " + positions[i][0] + " " +
                         positions[i][1] + " " + positions[i][2] +
                         " ELTLable " + eltLabels[i] + " SR " + sr + " SUBMEMBRANE " + ve.isSubmembrane());
              */
        }
        regionLabels = rA.toArray(new String[0]);

        nconnection = connections.size();
        conI = new int[nconnection][2];
        conG = new double[nconnection];
        for (int i = 0; i < nconnection; i++) {
            ElementConnection ec = connections.get(i);
            int ia = ec.getElementA().getNumber();
            int ib = ec.getElementB().getNumber();
            conI[i][0] = ia;
            conI[i][1] = ib;
            double d = Geom.distanceBetween(positions[ia], positions[ib]);
            conG[i] = ec.getContactArea() / d;
        }



        int[] nnbr = new int[nelement];

        for (int i = 0; i < nconnection; i++) {
            nnbr[conI[i][0]] += 1;
            nnbr[conI[i][1]] += 1;
        }


        eltNbrs = new int[nelement][];
        eltNbrG = new double[nelement][];
        for (int i = 0; i < nelement; i++) {
            eltNbrs[i] = new int[nnbr[i]];
            eltNbrG[i] = new double[nnbr[i]];
        }
        int[] inbr = new int[nelement];
        for (int i = 0; i < nconnection; i++) {
            int i0 = conI[i][0];
            int i1 = conI[i][1];
            eltNbrs[i0][inbr[i0]] = i1;
            eltNbrG[i0][inbr[i0]] = conG[i];
            inbr[i0] += 1;

            eltNbrs[i1][inbr[i1]] = i0;
            eltNbrG[i1][inbr[i1]] = conG[i];
            inbr[i1] += 1;
        }
    }


    public int[] getRegionIndexes() {
        return eltRegions;
    }

    public int size() {
        assert volumes != null: "volumes not fixed";
        return nelement;
    }

    public double[] getElementVolumes() {
        return volumes;
    }

    public double[] getExposedAreas() {
        return exposedAreas;
    }

    public boolean[] getSubmembranes()
    {
        return submembranes;
    }

    public int[][] getPerElementNeighbors() {
        return eltNbrs;
    }

    public double[][] getPerElementCouplingConstants() {
        return eltNbrG;
    }


    public String[] getRegionLabels() {
        return regionLabels;
    }

    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        sb.append("volumeGrid " + nelement);
        sb.append("\n");
        for (int i = 0; i < nelement; i++) {
            sb.append("" + elements.get(i).getAsText());
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getAsTableText() {
        StringBuffer sb = new StringBuffer();
        sb.append("element_index " + elements.get(0).getHeadings());
        sb.append("\n");
        for (int i = 0; i < nelement; i++) {
            sb.append("" + i + " " + elements.get(i).getAsPlainText());
            sb.append("\n");
        }
        return sb.toString();
    }

    public Vector<Object> gridData() {
        double[]
            x0 = new double[nelement], y0 = new double[nelement], z0 = new double[nelement],
            x1 = new double[nelement], y1 = new double[nelement], z1 = new double[nelement],
            x2 = new double[nelement], y2 = new double[nelement], z2 = new double[nelement],
            x3 = new double[nelement], y3 = new double[nelement], z3 = new double[nelement],
            volume = new double[nelement], deltaZ = new double[nelement];
        int i = 0;
        for (VolumeElement el: elements) {
            Position[] boundary = el.getBoundary();
            assert boundary.length == 4; /* we do not support anything else atm */

            x0[i] = boundary[0].getX();
            y0[i] = boundary[0].getY();
            z0[i] = boundary[0].getZ();
            x1[i] = boundary[1].getX();
            y1[i] = boundary[1].getY();
            z1[i] = boundary[1].getZ();
            x2[i] = boundary[2].getX();
            y2[i] = boundary[2].getY();
            z2[i] = boundary[2].getZ();
            x3[i] = boundary[3].getX();
            y3[i] = boundary[3].getY();
            z3[i] = boundary[3].getZ() + el.getDeltaZ();
            volume[i] = el.getVolume();
            deltaZ[i] = el.getDeltaZ();

            i++;
        }

        return new Vector<>(Arrays.asList(new Object[]
            {x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, volume, deltaZ}));
    }

    public String getLabel(int i) {
        return eltLabels[i];
    }

    public String getGroupID(int i) {
        return eltGroupIDs[i];
    }

    public int[][] getAreaIndexes(String[] targets) {
        int[][] ret = new int[targets.length][];
        for (int i = 0; i < targets.length; i++) {
            ArrayList<VolumeElement> matched = this.filterElementsByLabel(targets[i]);
            ret[i] = new int[matched.size()];
            for (int j = 0; j < ret[i].length; j++)
                ret[i][j] = matched.get(j).getNumber();
        }
        return ret;
    }

    private ArrayList<VolumeElement> getMatches(String sti) {

        ArrayList<VolumeElement> matched = new ArrayList<>();

        int iob = sti.indexOf("[");
        int icb = sti.indexOf("]");
        String pre = sti.substring(0, iob+1);
        String post = sti.substring(icb, sti.length());

        String range = sti.substring(iob + 1, icb);
        range = range.replace(" ", "");

        final int rangemin;
        final int rangemax;
        final ArrayList<Integer> indices = new ArrayList<>();
        if (range.indexOf(":") >= 0) {
            String rpre = range.substring(0, range.indexOf(":"));
            String rpost = range.substring(range.indexOf(":") + 1, range.length());
            if (rpre.length() > 0)
                rangemin = Integer.parseInt(rpre);
            else
                rangemin = 0;

            if (rpost.length() > 0)
                rangemax = Integer.parseInt(rpost);
            else
                rangemax = Integer.MAX_VALUE;
        } else if (range.equals("*")) {
            rangemin = 0;
            rangemax = Integer.MAX_VALUE;
        } else {
            String[] split = range.split(",");
            for (String index: split)
                indices.add(Integer.parseInt(index));

            rangemin = rangemax = -1;
        }

        log.debug("Looking for {}{}:{} or {}{}", pre, rangemin, rangemax, indices, post);

        for (VolumeElement el: this.elements) {
            String s = el.getLabel();

            if (s != null && s.startsWith(pre) && s.endsWith(post)) {
                String sin = s.substring(pre.length(), s.indexOf(post));
                int ind = Integer.parseInt(sin);

                if (rangemin <= ind && rangemax >= ind || indices.contains(ind))
                    matched.add(el);
            }
        }

        if (matched.isEmpty())
            throw new RuntimeException("There are no matches for target: " + sti);
        return matched;
    }

    public ArrayList<VolumeElement> filterElementsByLabel(String label) {
        boolean submembrane = label.endsWith(":submembrane");
        if (submembrane)
            label = label.substring(0, label.length() - ":submembrane".length());

        if (label.indexOf("[") >= 0) {
            if (submembrane)
                throw new RuntimeException("\":submembrane\" is not allowed with labelled elements");

            return this.getMatches(label);
        }

        ArrayList<VolumeElement> ans = new ArrayList<>();

        for (VolumeElement el: this.elements)
            if (el.getLabel() != null &&
                el.getLabel().equals(label) &&
                (!submembrane || el.isSubmembrane()))
                ans.add(el);
        if (!ans.isEmpty())
            return ans;

        for (VolumeElement el: this.elements)
            if (el.getRegion() != null &&
                el.getRegion().equals(label) &&
                (!submembrane || el.isSubmembrane()))
                ans.add(el);
        if (!ans.isEmpty())
            return ans;

        throw new RuntimeException("no elements labeled by \"" + label + "\"");
    }

    public boolean siteIsFractional(String label) {
        /* FIXME: need a better way */
        for (VolumeElement el: this.elements)
            if (el.getRegion() != null &&
                el.getRegion().equals(label))
                return true;
        return false;
    }

    public boolean isCuboid() {
        return hasCuboids && !hasCurveds;
    }

    public boolean isCurved() {
        return hasCurveds && !hasCuboids;
    }


    public String getAsElementsText() {
        StringBuffer sb = new StringBuffer();
        int ielt = 0;
        for (VolumeElement ve : elements) {
            if (ve instanceof CurvedVolumeElement) {
                sb.append("" + ielt + " ");
                sb.append(((CurvedVolumeElement)ve).getText3D());

            } else {
                String msg = ("cannot handle " + ve + " in element export");
                throw new RuntimeException(msg);
            }
            ielt += 1;
        }
        return sb.toString();
    }
}
