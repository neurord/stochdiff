package org.catacomb.movie.gif;

import org.catacomb.report.E;


public final class ByteAccumulator extends Object {
    int iin;
    byte[] ba;

    int ipend;
    int npend;

    int inxb;
    int iout;

    static int[] msk = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511};


    public ByteAccumulator(int n) {
        ba = new byte[n];
        iin = 0;
    }


    public ByteAccumulator(byte[] ba) {
        this.ba = ba;
        iout = 0;
        inxb = 0;
    }



    public int getNextWritePosition() {
        return iin;
    }

    public void checkDoubleSpace() {
        if (iin > ba.length/2) {
            int n = ba.length;
            byte[] bb = new byte[2*n];
            for (int i = 0; i < n; i++) bb[i] = ba[i];
            ba = bb;
        }
    }




    public void trim() {
        byte[] ret = new byte[iin];
        System.arraycopy(ba, 0, ret, 0, ret.length);
        ba = ret;
    }

    public byte[] getData() {
        flush();
        trim();
        return ba;
    }


    public void setByte(int i, byte b) {
        ba[i] = b;
    }

    public void setByte(int i, int b) {
        ba[i] = (byte)b;
    }



    public void appendByte(byte b) {
        ba[iin++] = b;
    }

    public void appendByte(int i) {
        ba[iin++] = (byte)i;
    }


    public void appendBytes(byte[] b) {
        System.arraycopy(b, 0, ba, iin, b.length);
        iin += b.length;
    }


    public void appendBytes(byte[] b, int ioff, int nb) {
        System.arraycopy(b, ioff, ba, iin, nb);
        iin += nb;
    }


    public void appendString(String s) {
        appendBytes(s.getBytes());
    }


    public void appendInt2(int i) {
        /*
         byte[] bt = {(byte)((i >> 8) & 0xff),
           (byte) (i & 0xff)};
           */
        byte[] bt = {(byte)((i) & 0xff),
                     (byte)((i >> 8) & 0xff)
                    };


        appendBytes(bt);
    }

    public void appendInt4(int i) {
        byte[] bt = {(byte)((i >> 24) & 0xff),
                     (byte)((i >> 16) & 0xff),
                     (byte)((i >> 8) & 0xff),
                     (byte)(i & 0xff)
                    };
        appendBytes(bt);

    }


    public void append(int i, int p) {
        if (i < 0) {
            E.error("appending negeative no to byte stream");
        }

        // append last p bits of i to stream;
        ipend += (i << npend);
        npend += p;
        while (npend >= 8) {
            appendByte(ipend & 0xff);
            ipend = (ipend >> 8);
            npend -= 8;
        }
    }


    public void flush() {
        if (npend > 0) {
            appendByte(ipend & 0xff);
        }
    }


    public int nextElt(int p) {
        int ng = 0;

        int ir = 0;

        while (ng < p) {
            if (iout >= ba.length) {
                return -1;
            }

            int ntk = p - ng;
            if (ntk > 8 - inxb) {
                ntk = 8 - inxb;
            }

            ir += ((ba[iout] >> inxb) & msk[ntk]) << ng;


            inxb += ntk;
            if (inxb == 8) {
                iout++;
                inxb = 0;
            }
            ng += ntk;
        }

        return ir;
    }


}






