package org.catacomb.act;

import java.util.Comparator;

public class SignatureComparator implements Comparator<BlockSignature> {



    public SignatureComparator() {

    }

    public int compare(BlockSignature fs1, BlockSignature fs2) {

        int ret = 0;
        if (fs1.getTypeCode() < fs2.getTypeCode()) {
            ret = -1;

        } else if (fs1.getTypeCode() > fs2.getTypeCode()) {
            ret = 1;

        } else {

            ret = fs1.getName().compareToIgnoreCase(fs2.getName());
        }
        return ret;
    }

}
