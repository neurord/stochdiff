package org.catacomb.interlish.report;


public class Slower {

    public static void pause(int n) {
        try {

            Thread.sleep(n);

        } catch (Exception ex) {

        }
    }

}
