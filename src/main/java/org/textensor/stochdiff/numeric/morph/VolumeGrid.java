//6 18 2007: WK added a variable (submembranes[]) and a function (getSubmembranes());
//			 the variable is init and set in the fix() function.
//written by Robert Cannon
package org.textensor.stochdiff.numeric.morph;

import java.util.ArrayList;

import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.Geom;
//  import org.textensor.stochdiff.geom.Position;


public class VolumeGrid {

    public static final int GEOM_2D = 0;
    public static final int GEOM_3D = 1;

    ArrayList<VolumeElement> elements;

    HashMap<String, ArrayList<VolumeElement>> regionHM;



    int nelement;
    String[] eltLabels;

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

    public VolumeGrid() {
        elements = new ArrayList<VolumeElement>();

        regionHM = new HashMap<String, ArrayList<VolumeElement>>();
    }


    public void importSlices(ArrayList<VolumeSlice> gridAL) {
        for (VolumeSlice vs : gridAL) {
            for (VolumeElement ve : vs.getElements()) {
                addVolumeElement(ve);
            }
        }
    }

    public void importLines(ArrayList<VolumeLine> gridAL) {
        for (VolumeLine vs : gridAL) {
            for (VolumeElement ve : vs.getElements()) {
                addVolumeElement(ve);

            }
        }
    }


    public void addElements(ArrayList<VolumeElement> veal) {
        for (VolumeElement ve : veal) {
            addVolumeElement(ve);
        }
    }

    private void addVolumeElement(VolumeElement ve) {
        elements.add(ve);

        String sr = ve.getRegion();
        if (sr != null) {
            if (regionHM.containsKey(sr)) {
                regionHM.get(sr).add(ve);

                //  Position[] sbdry = ve.getSurfaceBoundary();

            } else {
                ArrayList<VolumeElement> ave = new ArrayList<VolumeElement>();
                ave.add(ve);
                regionHM.put(sr, ave);
            }
        }
    }


    public ArrayList<VolumeElement> getElementsInRegion(String reg) {
        ArrayList<VolumeElement> ret = null;
        if (regionHM.containsKey(reg)) {
            ret = regionHM.get(reg);
        } else {
            // POSERR - any use in impty array - error?
            ret = new ArrayList<VolumeElement>();
        }
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
        eltRegions = new int[nelement];
        //<--WK
        submembranes = new boolean[nelement];
        //WK-->

        for (int i = 0; i < nelement; i++) {
            VolumeElement ve = elements.get(i);
            volumes[i] = ve.getVolume();
            exposedAreas[i] = ve.getExposedArea();
            positions[i][0] = ve.getX();
            positions[i][1] = ve.getY();
            positions[i][2] = ve.getZ();
            eltLabels[i] = ve.getLabel();
            //<--WK
            submembranes[i] = ve.getSubmembrane();
            //WK-->
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
        regionLabels = rA.toArray(new String[rA.size()]);

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

    public int getNElements() {
        if (volumes == null) {
            E.error("grid not fixed - call fix explicitly");
        }
        return nelement;
    }


    public double[] getElementVolumes() {
        return volumes;
    }

    public double[] getExposedAreas() {
        return exposedAreas;
    }

    //<--WK
    public boolean[] getSubmembranes()
    {
        return submembranes;
    }
    //WK-->

    public int[][] getPerElementNeighbors() {
        return eltNbrs;
    }


    public double[][] getPerElementCouplingConstants() {
        return eltNbrG;
    }


    public int size() {
        return getNElements();
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



    public String getLabel(int i) {
        return eltLabels[i];
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


                } else {
                    E.warning("There are no matches for target: " + sti);
                }

            } else {
                E.warning("An action is defined for area " + sti + " but there are no points with this label");
                ret[i] = new int[0];
            }
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







}
