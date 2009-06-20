package org.catacomb.serial.jar;



public class CustomJar {


    private final static byte[] MAGIC_SDJ = {(byte)0x45, (byte)0x53, (byte)0x44, (byte)0x41};

    private final static byte[] MAGIC_ZIP = {(byte)0x50, (byte)0x4b, (byte)0x03, (byte)0x04};

    private final static String MAIN = "mainname";
    private final static String MIME = "mimetype";




    public static boolean isMetaName(String s) {
        return (s != null && (s.equals(MAIN) || s.equals(MIME)));
    }

    public static String getMetaMain() {
        return MAIN;
    }


    public static String getMetaMime() {
        return MIME;
    }



    public static boolean claims(byte[] ba) {
        return (claims(ba, MAGIC_SDJ) || claims(ba, MAGIC_ZIP));
    }


    public static boolean claims(byte[] ba, byte[] magic) {
        boolean matches = true;
        for (int i = 0; i < 4; i++) {
            if (ba[i] == magic[i]) {

            } else {
                matches = false;
            }
        }
        return matches;
    }



    public static void naturalize(byte[] ba) {
        for (int i = 0; i < 4; i++) {
            ba[i] = MAGIC_ZIP[i];
        }
    }


}
