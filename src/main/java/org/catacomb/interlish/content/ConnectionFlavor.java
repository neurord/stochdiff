package org.catacomb.interlish.content;

import org.catacomb.datalish.SColor;


public class ConnectionFlavor {

    private String flavor;

    private SColor scolor;



    public ConnectionFlavor(String cf) {
        flavor = cf;
        scolor = new SColor("#0000ff");
    }

    public String toString() {
        return " cf[f=" + flavor + ", c=" + scolor + "]";
    }


    public int hashCode() {
        return flavor.hashCode();
    }

    public boolean equals(Object cf) {
        boolean ret = false;
        if (cf instanceof ConnectionFlavor) {
            String cff = ((ConnectionFlavor)cf).getFlavor();
            ret = flavor.equals(cff);
        }
        return ret;
    }


    public void setColor(SColor sc) {
        scolor = sc;
    }

    public SColor getColor() {
        return scolor;
    }

    public String getFlavor() {
        return flavor;
    }


    public boolean matches(ConnectionFlavor cf) {
        return flavor.equals(cf.getFlavor());
    }

}
