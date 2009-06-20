package org.catacomb.serial.om;


public class OmWriter {

    protected String formatDouble(double d) {
        String ret = "0";
        if (d != 0.0) {
            ret = String.format("%8.3g", new Double(d)).trim();
        }
        return ret;
    }

    public void addBodyElement(OmElement ome, String sn, String sv) {
        if (sv != null && sv.length() > 0) {
            OmElement elt = new OmElement(sn);
            elt.setBody(sv);
            ome.addElement(elt);
        }
    }

    public void addAttribute(OmElement ome, String sn, String sv) {
        if (sv != null && sv.length() > 0) {
            ome.addAttribute(sn, sv);
        }
    }

    public void addAttribute(OmElement ome, String sn, double d) {
        addAttribute(ome, sn, formatDouble(d));
    }

}
