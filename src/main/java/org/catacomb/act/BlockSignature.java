package org.catacomb.act;


public interface BlockSignature {

    // NB - these values set the (cosmetic) stubs order in scripts;
    public static final int INIT = 0;
    public static final int RECEIVE = 1;
    public static final int READ = 2;
    public static final int SHOW = 3;
    public static final int SEND = 4;
    public static final int SETTER = 5;
    public static final int GETTER = 6;
    public static final int UNKNOWN = 7;



    public final static int USER_SOURCE = 1;
    public final static int SYSTEM_SOURCE = 2;


    public int getTypeCode();

    public String getName();


}
