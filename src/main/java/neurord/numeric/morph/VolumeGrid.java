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

    HashMap<String, int[]> areaHM;

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
            addVolumeElement(ve);
    }

    private void addVolumeElement(VolumeElement ve) {
        this.elements.add(ve);

        final String region = ve.getRegion();
        assert region != null;

        log.debug("Processing VolumeElement {} from region {}", ve.getAsText(), region);

        if (regionHM.containsKey(region)) {
            regionHM.get(region).add(ve);
        } else {
            ArrayList<VolumeElement> ave = new ArrayList<>();
            ave.add(ve);
            regionHM.put(region, ave);
        }
    }

    public ArrayList<VolumeElement> getElementsInRegion(String reg) {
        final ArrayList<VolumeElement> ret;
        if (regionHM.containsKey(reg))
            ret = regionHM.get(reg);
        else
            // POSERR - any use in impty array - error?
            ret = new ArrayList<VolumeElement>();
        return ret;
    }

    // switch from collections to arrays for the calculation
    public void fix() {
        ArrayList<ElementConnection> connections = new ArrayList<ElementConnection>();
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
            ve.cache(i);
        }
        regionLabels = rA.toArray(new String[0]);

        makeAreaHM();


        nconnection = connections.size();
        conI = new int[nconnection][2];
        conG = new double[nconnection];
        for (int i = 0; i < nconnection; i++) {
            ElementConnection ec = connections.get(i);
            int ia = ec.getElementA().getCached();
            int ib = ec.getElementB().getCached();
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

    public int[][] getAreaIndexes(String[] targetIDs) {
        int[][] ret = new int[targetIDs.length][];
        for (int i = 0; i < targetIDs.length; i++) {
            String sti = targetIDs[i];
            if (areaHM.containsKey(sti)) {
                ret[i] = areaHM.get(sti);

            } else if (sti.indexOf("[") >= 0) {
                int[] ms = getMatches(areaHM, sti);
                if (ms != null && ms.length > 0) {
                    ret[i] = ms;
                } else
                    throw new RuntimeException("There are no matches for target: " + sti);

            } else
                throw new RuntimeException("An action is defined for area " + sti + " but there are no points with this label");
        }
        return ret;
    }


    private int[] getMatches(HashMap<String, int[]> areaHM, String sti) {

        ArrayList<Integer> aidx = new ArrayList<Integer>();

        int iob = sti.indexOf("[");
        int icb = sti.indexOf("]");
        String pre = sti.substring(0, iob+1);
        String post = sti.substring(icb, sti.length());

        String range = sti.substring(iob + 1, icb);

        range = range.replace(" ", "");
        int rangemin = 0;
        int rangemax = -1;
        if (range.indexOf(":") >= 0) {
            String rpre = range.substring(0, range.indexOf(":"));
            String rpost = range.substring(range.indexOf(":") + 1, range.length());
            if (rpre.length() > 0) {
                rangemin = Integer.parseInt(rpre);
            }
            if (rpost.length() > 0) {
                rangemax = Integer.parseInt(rpost);
            } else {
                rangemax = 1000000;
                // just a large number bigger than the max number of spines
            }
        }

        for (String s : areaHM.keySet()) {
            log.info("area key {}", s);

            if (s.startsWith(pre) && s.endsWith(post)) {
                String sin = s.substring(pre.length(), s.indexOf(post));
                int ind = Integer.parseInt(sin);
                boolean ok = false;
                if (rangemin <= ind && rangemax >= ind) {
                    ok = true;
                } else if (("," + range + ",").indexOf("," + sin + ",") >= 0) {
                    ok = true;
                }
                if (ok) {
                    for (int i : areaHM.get(s)) {
                        aidx.add(i);
                    }
                } else {
                    //
                }

            }
        }




        int na = aidx.size();
        int[] ret = new int[na];
        for (int i = 0; i < na; i++) {
            ret[i] = aidx.get(i);
        }
        return ret;
    }


    public void makeAreaHM() {
        HashMap<String, ArrayList<Integer>> idHM;
        idHM = new HashMap<String, ArrayList<Integer>>();

        for (int i = 0; i < nelement; i++) {
            String sl = eltLabels[i];
            if (sl != null && sl.length() > 0) {
                if (!idHM.containsKey(sl)) {
                    idHM.put(sl, new ArrayList<Integer>());
                }
                idHM.get(sl).add(new Integer(i));
            }

            String sr = regionLabels[eltRegions[i]];
            if (sr != null && sr.length() > 0) {
                if (!idHM.containsKey(sr)) {
                    idHM.put(sr, new ArrayList<Integer>());
                }
                idHM.get(sr).add(new Integer(i));
            }
        }


        areaHM = new HashMap<String, int[]>();
        for (String s : idHM.keySet()) {
            ArrayList<Integer> ali = idHM.get(s);
            int[] ia = new int[ali.size()];
            for (int i = 0; i < ia.length; i++) {
                ia[i] = ali.get(i).intValue();
            }
            areaHM.put(s, ia);
        }
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
