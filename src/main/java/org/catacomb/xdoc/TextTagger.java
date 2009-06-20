
package org.catacomb.xdoc;


import org.catacomb.report.E;
import org.catacomb.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TextTagger {

    static String[][] legalizers = {{"<", "&lt;"},
        {">", "&gt;"},
        {"'", "&apos;"},
        {"\"", "&quot;"}
    };

    //				   {"&", "&amp;"},


    static String[][] linkizers = {
        {"((\\s)(http://[\\w\\.]*)(\\s))", " <ain href=\"$3\">$3</ain> "},
        {"((\\s)(\\w+\\.\\w+\\.\\w+)(\\s))", " <ain href=\"http://$3\">$3</ain> "},
        {"(\\{(.+?)\\|(.+?)\\})", " <ain href=\"$3\">$2</ain> "},
        {"(?m)^file: *(.+) *$", "file: <ai href\"($1)\">$1</ai><br/>\\\n"},
        {" #(\\d*)", " <CR>$1</CR>"}
    };


    static String[][] emphasizers = {
        {"((\\s_)([\\w\\s]+)_([\\s\\p{Punct}]))", " <u>$3</u>$4"},
        {"((\\s\\*)([\\w\\s]+)\\*([\\s\\p{Punct}]))", " <b>$3</b>$4"},
        {"((\\s/)([\\w\\s]+)/([\\s\\p{Punct}]))", " <i>$3</i>$4"}
    };



    // should check there is no markup in the header;
    static String[][] headerizers = {
        {"^H:(.*)$",  "\\\n\\\n<h2>$1</h2>\\\n\\\n "},
        {"^H1:(.*)$", "\\\n\\\n<h1>$1</h1>\\\n\\\n"},
        {"^H2:(.*)$", "\\\n\\\n<h2>$1</h2>\\\n\\\n"},
        {"^H3:(.*)$", "\\\n\\\n<h3>$1</h3>\\\n\\\n"},
        {"^H4:(.*)$", "\\\n\\\n<h4>$1</h4>\\\n\\\n"}
    };


    static String[][] elementizers = {
        {"(^(\\w+?): *(.*?) *$)", "<element name=\"$2\" value=\"$3\"/>\n"}
    };





    // paragraph patterns need compiling in MULTILINE mode;
    static String paragraphSeparator = "(?<=(\r\n|\r|\n))([ \\t]*$)+";
    static String paragraphMatch = "(^.*\\S+.*$)+";   // doesn't work?
    //   static String listItemPrefix = " +\\S +";

    static String listItemPrefix = " *[+*-] +";

    static String attributeLine = "(^(\\w+?) *= *(.*?) *$)";
    static String elementTag="^(\\w+?):";


    static TextTagger tagger;

    public static TextTagger getTagger() {
        if (tagger == null) {
            tagger = new TextTagger();
        }
        return tagger;
    }

    public TextTagger() {
        // some static variables should be instance variables undre user configuration ****
    }


    public void init() {

    }



    public String textToXML(String plainText, String defaultRoot) {
        String xtxt = plainText;



        // escape any dodgy characters;
        xtxt = legalize(xtxt);



        // extract any attributes;
        String[] wrapper = new String[2];
        xtxt = extractAttributes(xtxt, defaultRoot, wrapper);


        // put in headers - mark these up before paragraphizing
        xtxt = headerize(xtxt);

        // extract elements of form "name: value "
        xtxt=elementize(xtxt);


        // lists
        xtxt = listize(xtxt);


        // paragraphs won't wrap existing markup, so do them before the rest
        xtxt = paragraphize(xtxt);

        xtxt = linkize(xtxt);

        xtxt = emphasize(xtxt);


        // wrap in root element
        xtxt = xmlwrap(xtxt, wrapper);

        // finished - should check legality here...
        return xtxt;
    }


    public String tagText(String plainText) {
        String xtxt = plainText;

        xtxt = xtxt.replaceAll("-p-", "\n\n");

        // escape any dodgy characters;
        xtxt = legalize(xtxt);



        // put in headers - mark these up before paragraphizing
        xtxt = headerize(xtxt);

        // extract elements of form "name: value "
//   xtxt=elementize(xtxt);

        // lists
        xtxt = listize(xtxt);


        // paragraphs won't wrap existing markup, so do them before the rest
        xtxt = paragraphize(xtxt);

        xtxt = linkize(xtxt);

        xtxt = emphasize(xtxt);

        return xtxt;
    }




    public String textToEdit(String plainText, String defaultRoot) {
        // return legal xml with the content untouched and wrapped in a pre;
        String xtxt = plainText;

        String fullxml = textToXML(plainText, defaultRoot);


        // escape any dodgy characters;
        xtxt = legalize(xtxt);



        // extract any attributes;
        String[] wrapper = new String[2];
        xtxt = extractAttributesAsElements(xtxt, "source", wrapper);

        // extract elements of form "name: value "
        //      xtxt=elementize(xtxt);

        xtxt = preWrap(xtxt);

        xtxt = xmlwrap(xtxt, wrapper);


        StringBuffer sb = new StringBuffer();
        sb.append("<editsource>\n");
        sb.append(xtxt);
        sb.append("\n");
        sb.append("   <output>\n");
        sb.append(fullxml);
        sb.append("\n");
        sb.append("   </output>\n");
        sb.append("</editsource>\n");

        // finished - should check legality here...
        return sb.toString();
    }




    public void textToXML(File ftxt, File fxml) {
        String txt = FileUtil.readStringFromFile(ftxt);
        String xtxt = textToXML(txt, "default");
        FileUtil.writeStringToFile(xtxt, fxml);
    }


    public void textToEdit(File ftxt, File fedt) {
        String txt = FileUtil.readStringFromFile(ftxt);
        String etxt = textToEdit(txt, "default");
        FileUtil.writeStringToFile(etxt, fedt);
    }




    public String applyReplacements(String txtin, String[][] reps) {
        String txt = txtin;
        for (int i = 0; i < reps.length; i++) {
            String[] lp = reps[i];
            txt = txt.replaceAll(lp[0], lp[1]);
        }
        return txt;
    }


    public String applyReplacementsMultiline(String txtin, String[][] reps) {
        String txt = txtin;
        for (int i = 0; i < reps.length; i++) {
            String[] lp = reps[i];
            Pattern pattern = Pattern.compile(lp[0], Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(txt);
            String output = matcher.replaceAll(lp[1]);
            txt = output;
        }
        return txt;
    }




    public String legalize(String txt) {
        return applyReplacements(txt, legalizers);
    }


    public String linkize(String txt) {
        return applyReplacements(txt, linkizers);
    }


    public String emphasize(String txt) {
        return applyReplacements(txt, emphasizers);
    }


    public String headerize(String txt) {
        return applyReplacementsMultiline(txt, headerizers);
    }


    public String elementize(String txt) {
        //      return applyReplacementsMultiline(txt, elementizers);
        return elementize2(txt);
    }



    public String htmlizeParagraph(String apar) {
        String stmp = legalize(apar);
        String ret = emphasize(stmp);
        return ret;
    }





    // should be 5 lines max !!!! ******

    public String elementize2(String txt) {
        Pattern pat = Pattern.compile(elementTag);
        Matcher matcher = pat.matcher("");

        StringBuffer sb = new StringBuffer();

        try {
            BufferedReader sr = new BufferedReader(new StringReader(txt));
            while (sr.ready()) {
                String line = sr.readLine();
                if (line == null) {
                    break;
                } else {
                    matcher.reset(line);
                    if (matcher.find() && matcher.start() == 0) {
                        String tag = matcher.group(1);
                        sb.append(makeElement(tag, line));
                        sb.append("\n");
                    } else {
                        sb.append(line);
                        sb.append("\n");
                    }
                }

            }
        } catch (IOException ex) {
            E.error("io exception listizing ");
            ex.printStackTrace();
        }

        return sb.toString();
    }


    // do it all with a regex? **********
    private String makeElement(String tag, String line) {
        String ret = "";
        if (line.startsWith(tag + ":")) {
            StringBuffer sb = new StringBuffer();
            sb.append("<");
            sb.append(tag);

            String rest = line.substring(tag.length()+1, line.length());
            StringTokenizer st = new StringTokenizer(rest, ",");
            boolean doneclose = false;
            while (st.hasMoreTokens()) {
                String stok = st.nextToken();

                if (doneclose) {
                    sb.append(stok);
                } else {
                    int ieq = stok.indexOf("=");
                    if (ieq > 0) {
                        String nm = stok.substring(0, ieq);
                        String val = stok.substring(ieq+1, stok.length());
                        nm = nm.trim();
                        nm = nm.replaceAll(" ", "_");
                        sb.append(" ");
                        sb.append(nm);
                        sb.append("=\"");
                        val = val.trim();
                        sb.append(val);
                        sb.append("\"");

                    } else {
                        sb.append(">");
                        sb.append(stok);
                        doneclose = true;
                    }
                }

            }
            if (!doneclose) {
                sb.append(">");
            }

            sb.append("</");
            sb.append(tag);
            sb.append(">\n");
            ret = sb.toString();
        } else {
            E.error(" misinterpreted element? " +
                    "---" + tag + "---  ---" + line);
        }
        return ret;
    }





    public String paragraphize(String s) {
        return paragraphize1(s);
    }



    public String paragraphize1(String sin) {
        String s = sin;
        s = s.replaceAll("-p-", "\n\n");
        String[] paras = Pattern.compile(paragraphSeparator, Pattern.MULTILINE).split(s);

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < paras.length; i++) {
            String par = paras[i];
            par = par.trim();
            insertParagraph(sb, par);
        }
        return sb.toString();
    }



    public String paragraphize2(String s) {
        // alt paragraphizer
        // Compile the pattern
        Pattern pattern = Pattern.compile(paragraphMatch, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(s);

        StringBuffer sb = new StringBuffer();
        // Read the paragraphs
        while (matcher.find()) {
            String para = matcher.group();
            para = para.trim();
            insertParagraph(sb, para);
        }
        return sb.toString();
    }


    void insertParagraph(StringBuffer sb, String para) {
        if (para.length() > 0) {
            if (para.indexOf("<") < 0) {
                // no markup in the paragraph. Must avoid splitting any exiting markup.
                sb.append("\n<p>\n");
                sb.append(para);
                sb.append("\n</p>\n\n");
            } else {
                sb.append(para);
                sb.append("\n");
            }
        }
    }




    public String listize(String txt) {
        // spot indented blocks beginning with the same symbol, such as
        /*

        + point one
        more on point 1
        + pont 2 and so on
        + last one

        so to initialize list it is one or more spaces, a character, one or more spaces.
        continuing items have the same indent. New items have indent with character. Blank lines
        can precede another point, but otherwise end the list as does breaking the indent.
        */

        Pattern pat = Pattern.compile(listItemPrefix);
        Matcher matcher = pat.matcher("");

        StringBuffer sb = new StringBuffer();

        try {
            BufferedReader sr = new BufferedReader(new StringReader(txt));

            while (sr.ready()) {
                String line = sr.readLine();

                if (line == null) {
                    break;
                } else {

                    matcher.reset(line);
                    if (matcher.find() && matcher.start() == 0) {

                        String itemintro = matcher.group();
                        wrapList(sr, sb, itemintro, line);

                    } else {
                        sb.append(line);
                        sb.append("\n");
                    }
                }

            }
        } catch (IOException ex) {
            E.error("io exception listizing ");
            ex.printStackTrace();
        }

        return sb.toString();
    }


    public void wrapList(BufferedReader sr, StringBuffer sb,
                         String itemintro, String fl) throws IOException {
        String firstline = fl;
        String itemcont = itemintro.replaceFirst("\\S", " ");

        boolean inli = false;
        boolean inul = false;

        sb.append("\n\n<ul>\n");
        inul = true;

        sb.append("  <li>\n");
        inli = true;

        firstline = firstline.substring(itemintro.length(), firstline.length());
        sb.append(firstline);
        sb.append("\n");

        String line = "";
        while (sr.ready()) {
            line = sr.readLine();
            if (line == null) {
                break;

            } else if (line.trim().length() == 0) {
                // just skip it;

            } else if (line.startsWith(itemcont)) {
                sb.append(line);
                sb.append("\n");
                line = null;

            } else if (line.startsWith(itemintro)) {
                if (inli) {
                    sb.append("   </li>\n");
                }
                line = line.substring(itemintro.length(), line.length());
                sb.append("   <li>\n");
                sb.append(line);
                sb.append("\n");
                line = null;

            } else {
                break;
            }
        }


        if (inli) {
            sb.append("</li>\n");
        }

        if (inul) {
            sb.append("</ul>\n\n");
        }

        if (line != null) {
            sb.append("\n\n");
            sb.append(line);
            sb.append("\n");
        }
    }









    String xmlwrap(String xtxt, String[] wrapper) {
        StringBuffer sb = new StringBuffer();
        sb.append(wrapper[0]);
        sb.append("\n");
        sb.append(xtxt);
        sb.append("\n");
        sb.append(wrapper[1]);
        sb.append("\n");
        return sb.toString();
    }


    String preWrap(String s) {
        StringBuffer sb = new StringBuffer();
        sb.append("<pre>\n");
        sb.append(s);
        sb.append("\n");
        sb.append("</pre>\n");
        return sb.toString();
    }




    String htmlWrap(String s) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>\n");
        sb.append("<head></head>\n");
        sb.append("<body>\n");
        sb.append(s);
        sb.append("\n");
        sb.append("</body>\n");
        return sb.toString();
    }



    String readAttributes(String xtxtin, String[][] sat) {
        String xtxt = xtxtin;
        Pattern pattern = Pattern.compile(attributeLine, Pattern.MULTILINE);

        int nat = 0;
        Matcher matcher = pattern.matcher("");
        while (true) {
            xtxt = xtxt.trim();
            matcher.reset(xtxt);
            if (xtxt.length() > 0 && matcher.find() && matcher.start() == 0) {
                String group = matcher.group();
                xtxt = xtxt.substring(group.length(), xtxt.length());

                String name = matcher.group(2);
                String value = matcher.group(3).trim();

                if (containsAttribute(sat, nat, name)) {
                    E.warning(" ignoringdubplicate attribute " +
                              name + " " + value);

                } else {
                    sat[nat][0] = name;
                    sat[nat][1] = value;
                    nat++;
                }

            } else {
                break;
            }
        }
        return xtxt;
    }



    // ugly - shouldn't use if more than a couple of attributes;
    private boolean containsAttribute(String[][] sat, int nat, String name) {
        boolean contains = false;
        for (int i = 0; i<nat; i++) {
            if (sat[i][0].equals(name)) {
                contains = true;
                break;
            }
        }
        return contains;
    }




    String extractAttributes(String xtxtin, String defaultRoot, String[] wrapper) {
        String xtxt = xtxtin;
        String[][] sat = new String[100][2]; //***

        xtxt = readAttributes(xtxt, sat);

        StringBuffer sb = new StringBuffer();
        String eltname = defaultRoot;
        if (eltname == null) {
            eltname = "default";
        }
        for (int i = 0; i < sat.length && sat[i][0] != null; i++) {
            if (sat[i][0].equals("type")) {
                eltname = sat[i][1].replaceAll("\\s", "_");
            } else {
                sb.append("\n      ");
                sb.append(sat[i][0]);
                sb.append("=\"");
                sb.append(sat[i][1]);
                sb.append("\"");
            }

        }
        wrapper[0] = ("<" + eltname + " src=\"txt\"" + sb.toString() + ">");
        wrapper[1] = ("</" + eltname + ">");
        return xtxt;
    }






    public String extractAttributesAsElements(String xtxtin, String defaultRoot,
            String[] wrapper) {
        String[][] sat = new String[100][2];

        String xtxt = readAttributes(xtxtin, sat);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < sat.length && sat[i][0] != null; i++) {
            sb.append("   <attribute name=\"");
            sb.append(sat[i][0]);
            sb.append("\" value=\"");
            sb.append(sat[i][1]);
            sb.append("\"/>\n");
        }
        wrapper[0] = ("<" + defaultRoot + ">\n" + sb.toString());
        wrapper[1] = ("</" + defaultRoot + ">");
        return xtxt;
    }




    public static void main(String[] argv) {

        TextTagger tt = new TextTagger();

        String sd = argv[0];

        File fdir = new File(sd);
        File[] af = fdir.listFiles();

        for (int i = 0; i < af.length; i++) {
            String fnm = af[i].getName();
            if (fnm.endsWith(".txt")) {



                File ftxt = af[i];
                String sr = ftxt.getName();
                sr = sr.substring(0, sr.length() - 4);

                File fedt = new File(fdir, sr + ".edt");
                tt.textToEdit(ftxt, fedt);

                File fxml = new File(fdir, sr + ".xml");
                tt.textToXML(ftxt, fxml);
            }
        }
    }





}
