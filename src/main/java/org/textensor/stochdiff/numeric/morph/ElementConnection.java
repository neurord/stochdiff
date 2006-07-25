package org.textensor.stochdiff.numeric.morph;


public class ElementConnection {

    VolumeElement eltA;
    VolumeElement eltB;

    double contactArea;

    public ElementConnection(VolumeElement a, VolumeElement b,
                             double ca) {
        eltA = a;
        eltB = b;
        contactArea = ca;
    }


    public VolumeElement getElementA() {
        return eltA;
    }

    public VolumeElement getElementB() {
        return eltB;
    }

    public double getContactArea() {
        return contactArea;
    }

}
