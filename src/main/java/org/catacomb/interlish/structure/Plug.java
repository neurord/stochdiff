package org.catacomb.interlish.structure;

import org.catacomb.be.XYLocation;
import org.catacomb.interlish.content.ConnectionFlavor;


public interface Plug {


    XYLocation getXYLocation();

    void setXYLocation(XYLocation xyl);

    void connectTo(Socket skt);

    void disconnect();

    String getID();

    ConnectionFlavor getFlavor();

    boolean isFree();

    Socket getSocket();

    public void syncLocation();


    public final static int ANY = 0;
    public final static int INPUT = 1;
    public final static int OUTPUT = 2;

    boolean directionMatches(Socket skt);

}
