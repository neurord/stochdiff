package neurord.numeric.morph;


public class ElementConnection {

    final VolumeElement a, b;
    final double contactArea;

    public ElementConnection(VolumeElement a, VolumeElement b, double contactArea) {
        /* Connections must be one way and cannot repeat */
        for (ElementConnection aconn: a.connections)
            assert aconn.getElementB() != b;
        for (ElementConnection bconn: b.connections)
            assert bconn.getElementB() != a;

        this.a = a;
        this.b = b;
        this.contactArea = contactArea;

    }

    public VolumeElement getElementA() {
        return this.a;
    }

    public VolumeElement getElementB() {
        return this.b;
    }

    public double getContactArea() {
        return this.contactArea;
    }

    public String toString() {
        return String.format("ElementConnection: %s — %s contactArea=%f",
                             a.getNumber(), b.getNumber(), contactArea);
    }
}
