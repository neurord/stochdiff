
package org.catacomb.interlish.report;




public class LogFallback {

    public static void log(String s) {
        (new PrintLogger()).log(s);
    }

}
