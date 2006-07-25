package org.textensor.stochdiff.numeric.morph;

import java.util.HashSet;

import java.util.ArrayList;

import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.Geom;

public class VolumeGrid {

    public static final int GEOM_2D = 0;
    public static final int GEOM_3D = 1;

    ArrayList<VolumeElement> elements;

    HashMap<String, ArrayList<VolumeElement>> regionHM;



    int nelement;
    String[] eltLabels;
    String[] eltRegions;
    double[] volumes;
    double[][] positions;

    int nconnection;
    int[][] conI;
    double[] conG;

    int[][] eltNbrs;
    double[][] eltNbrG;


    public VolumeGrid() {
        elements = new ArrayList<VolumeElement>();

        regionHM = new HashMap<String, ArrayList<VolumeElement>>();
    }


    public void importSlices(HashSet<VolumeSlice> gridHS) {
        for (VolumeSlice vs : gridHS) {
            for (VolumeElement ve : vs.getElements()) {
                addVolumeElement(ve);
            }
        }
    }

    public void importLines(HashSet<VolumeLine> gridHS) {
        for (VolumeLine vs : gridHS) {
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

        nelement = elements.size();
        volumes = new double[nelement];
        positions = new double[nelement][3];
        eltLabels = new String[nelement];
        eltRegions = new String[nelement];

        for (int i = 0; i < nelement; i++) {
            VolumeElement ve = elements.get(i);
            volumes[i] = ve.getVolume();
            positions[i][0] = ve.getX();
            positions[i][1] = ve.getY();
            positions[i][2] = ve.getZ();
            eltLabels[i] = ve.getLabel();
            eltRegions[i] = ve.getRegion();
            ve.cache(i);
        }

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


    public int getNElements() {
        if (volumes == null) {
            E.error("grid not fixed - call fix explicitly");
        }
        return nelement;
    }


    public double[] getElementVolumes() {
        return volumes;
    }


    public int[][] getPerElementNeighbors() {
        return eltNbrs;
    }


    public double[][] getPerElementCouplingConstants() {
        return eltNbrG;
    }


    public int size() {
        return getNElements();
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


    public String getLabel(int i) {
        return eltLabels[i];
    }


    public int[] getElementIndexes(String[] targetIDs) {
        HashMap<String, Integer> idHM = new HashMap<String, Integer>();
        for (int i = 0; i < nelement; i++) {
            String sl = eltLabels[i];
            String sr = eltRegions[i];
            if (sl != null && sl.length() > 0) {
                if (idHM.containsKey(sl)) {
                    // do nothing, just the first is used for now - TODO
                } else {
                    idHM.put(sl, new Integer(i));
                }

            }
            if (sr != null && sr.length() > 0) {
                if (idHM.containsKey(sr)) {
                    // do nothing, just the first is used for now - TODO
                } else {
                    idHM.put(sr, new Integer(i));
                }
            }
        }

        int[] iret = new int[targetIDs.length];
        for (int i = 0; i < iret.length; i++) {
            String st = targetIDs[i];
            if (st != null) {
                if (idHM.containsKey(st)) {
                    iret[i] = idHM.get(st).intValue();
                } else {
                    if (st.equals("unused")) {
                        // somewhat ADHOC - "unused" is taken to mean the user
                        // doesn't want it, so generates no warning message;
                    } else {
                        E.warning("no such element in grid " + st);
                    }
                }
            }
        }
        return iret;
    }




}
