package org.catacomb.dataview.formats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.catacomb.report.E;

import java.util.StringTokenizer;

import java.util.ArrayList;

public class MeshDataReader implements DataReader {

    File file;

    double[][][] mesh;

    double[][][] data;

    double[] frameValues;

    String[] specieNames;

    int nvar = 0;

    DataReader coReader;

    public MeshDataReader(File f, DataReader dr) {
        file = f;
        coReader = dr;
    }


    public boolean canRead(String line) {
        boolean ret = false;
        if (line.startsWith("volumeGrid") || line.startsWith("gridConcentrations")) {
            ret = true;
        }
        return ret;
    }


    // REFAC generalize block names
    public void read() {
        ArrayList<double[][]> gcal = new ArrayList<double[][]>();
        ArrayList<Double> gridTimes = new ArrayList<Double>();
        specieNames = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine(); // has to be the magic no;

            while (br.ready()) {
                String line = br.readLine();
                if (line.trim().length() == 0) {
                    // just skip it;

                } else if (line.startsWith("volumeGrid")) {
                    StringTokenizer st = new StringTokenizer(line, " ");
                    st.nextToken();
                    int nel = Integer.parseInt(st.nextToken());
                    E.info("reading the mesh " + nel);
                    readMesh(br, nel);


                } else if (line.startsWith("gridConcentrations")) {
                    StringTokenizer st = new StringTokenizer(line, " ");
                    st.nextToken();
                    int nel = Integer.parseInt(st.nextToken());
                    int nspec = Integer.parseInt(st.nextToken());
                    double v = Double.parseDouble(st.nextToken());
                    //    E.info("reading grid " + nel + " " + nspec + " " + v);
                    gridTimes.add(new Double(v));
                    if (specieNames == null) {
                        specieNames = new String[nspec];
                        for (int i = 0; i < nspec; i++) {
                            specieNames[i] = st.nextToken();
                        }
                    }
                    gcal.add(readGridConcs(br, nel, nspec));

                    if (nvar <= 0) {
                        nvar = nspec;
                    }

                } else if (coReader != null && coReader.canRead(line)) {
                    coReader.readBlock(line, br);

                } else {
                    E.error("unknown content type " + line);
                    break;
                }
            }

        } catch (Exception ex) {
            E.error("Read error " + ex);
            ex.printStackTrace();
        }

        int ng = gcal.size();
        data = new double[ng][][];
        frameValues = new double[ng];
        for (int i = 0; i < ng; i++) {
            frameValues[i] = gridTimes.get(i).doubleValue();
            data[i] = gcal.get(i);
        }
    }



    private void readMesh(BufferedReader br, int nel) {
        mesh = new double[nel][][];
        try {
            for (int ilin = 0; ilin < nel; ilin++) {
                String line = br.readLine();
                double[] dat = parseLine(line);
                int n = dat.length / 3;
                mesh[ilin] = new double[3][n];
                for (int i = 0; i < n; i++) {
                    mesh[ilin][0][i] = dat[3 * i];
                    mesh[ilin][1][i] = dat[3 * i + 1];
                    mesh[ilin][2][i] = dat[3 * i + 2];
                }
            }
        } catch (Exception ex) {
            E.error("ex " + ex);
        }
    }


    private double[][] readGridConcs(BufferedReader br, int nel, int nspec) {
        double[][] ret = new double[nspec][nel];
        try {
            for (int ilin = 0; ilin < nel; ilin++) {
                String line = br.readLine();
                double[] dl = parseLine(line);
                for (int i = 0; i < nspec && i < dl.length; i++) {
                    ret[i][ilin] = dl[i];
                }
            }
        } catch (Exception ex) {
            E.error("ex " + ex);
        }
        return ret;
    }




    private double[] parseLine(String s) {
        StringTokenizer st = new StringTokenizer(s, " ()");
        int n = st.countTokens();

        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            ret[i] = Double.parseDouble(st.nextToken());
        }
        return ret;
    }


    public double[][][] getMesh() {
        return mesh;
    }


    public double[][][] getData() {
        return data;
    }




    public double[] getFrameValues() {
        return frameValues;
    }


    public String[] getValueNames() {
        String[] ret = specieNames;

        if (ret == null) {
            ret = new String[nvar];
            for (int i = 0; i < nvar; i++) {
                ret[i] = "species " + i;
            }
        }
        return ret;
    }

    // todo - move read method chunks down to here;
    public void readBlock(String line, BufferedReader br) {
        // TODO Auto-generated method stub

    }



}
