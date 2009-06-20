package org.catacomb.util;

import java.util.StringTokenizer;


public class ScriptUtil {

    public static int appendLines(String src, StringBuffer sb, String pfx, String sfx) {

        int line = 0;
        StringTokenizer sti = new StringTokenizer(src, "\n");
        while (sti.hasMoreTokens()) {
            String tok = sti.nextToken();
            if (tok.trim().length() > 0) {
                sb.append(pfx + tok + sfx + "\n");
                line += 1;
            }
        }
        return line;
    }

}
