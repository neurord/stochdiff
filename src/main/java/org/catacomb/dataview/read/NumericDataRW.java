package org.catacomb.dataview.read;



import org.catacomb.numeric.data.*;
import org.catacomb.report.E;

import java.io.*;



public class NumericDataRW {


    public final static byte CODE_NUM_DATA_SET = 1;
    public final static byte CODE_FLOAT_SCALAR = 2;
    public final static byte CODE_FLOAT_VECTOR = 3;
    public final static byte CODE_DATA_SET_ARRAY = 4;


    public static byte[] binarize(NumDataSet dset) {
        byte[] ret = new byte[0];

        try {

            ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            BufferedOutputStream bufos = new BufferedOutputStream(bout);
            DataOutputStream dos = new DataOutputStream(bufos);


            write(dos, dset);

            dos.close();
            bufos.flush();
            ret = bout.toByteArray();
        } catch (Exception ex) {

            E.error("cannot binarize " + dset + " " + ex);
            ex.printStackTrace();
        }
        return ret;
    }


    public static void write(DataOutputStream dos, Object obj) throws IOException {

        if (obj instanceof NumDataSet) {
            NumDataSet dset = (NumDataSet)obj;
            dos.write(CODE_NUM_DATA_SET);
            writeString(dos, dset.getName());
            dos.writeInt(dset.size());


            for (DataItem dit : dset.getValues()) {
                write(dos, dit);
            }



        } else if (obj instanceof FloatVector) {
            FloatVector fv = (FloatVector)obj;
            dos.write(CODE_FLOAT_VECTOR);
            writeString(dos, fv.getName());
            double[] da = fv.getValue();
            dos.writeInt(da.length);
            for (int i = 0; i < da.length; i++) {
                dos.writeDouble(da[i]);
            }

        } else if (obj instanceof FloatScalar) {
            FloatScalar fs = (FloatScalar)obj;
            dos.write(CODE_FLOAT_SCALAR);
            writeString(dos, fs.getName());
            dos.writeDouble(fs.getValue());


        } else if (obj instanceof DataSetArray) {
            DataSetArray dsa = (DataSetArray)obj;
            dos.write(CODE_DATA_SET_ARRAY);
            writeString(dos, dsa.getName());

            NumDataSet[] dsets = dsa.getDataSets();
            dos.writeInt(dsets.length);

            for (int i = 0; i < dsets.length; i++) {
                write(dos, dsets[i]);
            }

        } else {
            E.error("data ion cannot write " + obj);
        }
    }



    private static void writeString(DataOutputStream dout, String sin) throws IOException {
        String s = sin;
        if (s == null) {
            E.debugError("null string in binary io");
            s = "error";
        }
        dout.writeInt(s.length());
        dout.write(s.getBytes("US-ASCII"));
    }







    public static Object deBinarize(byte[] bdata) {
        return read(bdata);
    }



    public static Object read(byte[] bdata) {
        Object ret = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bdata);
            ret = read(bais);
        } catch (Exception ex) {
            E.error("read error " + ex);
        }
        return ret;
    }



    public static Object read(InputStream ins) {
        Object ret = null;

        try {
            BufferedInputStream bis = new BufferedInputStream(ins);
            DataInputStream din = new DataInputStream(bis);

            byte[] bh = new byte[256];
            int nread = din.read(bh, 0, bh.length);
            if (nread != bh.length) {
                E.error("string read error - want " + bh.length + " got " + nread);
            }

            ret = readDIS(din);

        } catch (Exception ex) {
            E.error("read error " + ex);
        }

        return ret;
    }



    private static Object readDIS(DataInputStream din) throws IOException  {
        Object ret = null;
        int type = din.readByte();
        int nl = din.readInt();
        byte[] ba = new byte[nl];

        int nread = din.read(ba, 0, nl);
        if (nread != nl) {
            E.error("string read error - want " + nl + " got " + nread);
        }
        String name = new String(ba, "US-ASCII");


        if (type == CODE_NUM_DATA_SET) {
            NumDataSet dset = new NumDataSet(name);

            int nel = din.readInt();
            for (int i = 0; i < nel; i++) {
                Object obj = readDIS(din);
                dset.add(obj);
            }
            ret = dset;


        } else if (type == CODE_DATA_SET_ARRAY) {
            int nel = din.readInt();
            NumDataSet[] dsa = new NumDataSet[nel];
            for (int i = 0; i < nel; i++) {
                dsa[i] = (NumDataSet)(readDIS(din));
            }
            DataSetArray arr = new DataSetArray(name, dsa);
            ret = arr;

        } else if (type == CODE_FLOAT_SCALAR) {
            double d = din.readDouble();
            FloatScalar fs = new FloatScalar(name, d);
            ret = fs;


        } else if (type == CODE_FLOAT_VECTOR) {
            int nel = din.readInt();
            double[] da = new double[nel];
            for (int i = 0; i < nel; i++) {
                da[i] = din.readDouble();
            }
            FloatVector fv = new FloatVector(name, da);
            return fv;

        } else {
            E.error("Numeric reader unknown code " + type);
        }

        return ret;
    }



}
