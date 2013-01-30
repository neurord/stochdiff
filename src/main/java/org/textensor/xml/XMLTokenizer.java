package org.textensor.xml;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

import java.util.HashMap;

import org.textensor.report.E;


public class XMLTokenizer {

    static int iq;
    static int ieq;
    static int iabo;
    static int iabc;
    static int iqm;
    static int iexc;
    static int ims;

    StreamTokenizer streamTokenizer;

    int count;


    static {
        String sord = "\"=<>?!-";
        iq = sord.charAt(0);
        ieq = sord.charAt(1);
        iabo = sord.charAt(2);
        iabc = sord.charAt(3);
        iqm = sord.charAt(4);
        iexc = sord.charAt(5);
        ims = sord.charAt(6);
    }



    HashMap<String, String> cdataHM;

    String srcString;

    public XMLTokenizer(String s) {
        // EFF remove this - just for debugging;
        srcString = extractCDATAs(s);


        streamTokenizer = new StreamTokenizer(new StringReader(srcString));
        initializeStreamTokenizer(streamTokenizer);
    }


    private String extractCDATAs(String src) {
        StringBuffer sret = new StringBuffer();
        int icur = 0;
        int iscd = src.indexOf("<![CDATA[");

        while (iscd >= icur) {
            sret.append(src.substring(icur, iscd));
            int iecd = src.indexOf("]]>", iscd + 9);
            if (iecd >= 0) {
                String cdata = src.substring(iscd + 9, iecd);
                if (cdataHM == null) {
                    cdataHM = new HashMap<String, String>();
                }
                String rpl = "xyz" + cdataHM.size();
                cdataHM.put(rpl, cdata);
                sret.append(rpl);

            } else {
                iecd = iscd + 6;
                E.error("no closure of cdata beginning character " + iscd + "? ");
            }
            icur = iecd + 3;
            iscd = src.indexOf("<![CDATA[", icur);
        }
        if (icur < src.length()) {
            sret.append(src.substring(icur, src.length()));
        }
        return sret.toString();
    }


    private void setStringValue(XMLToken xmlt, String sv) {
        if (sv.startsWith("xyz")) {
            if (cdataHM != null && cdataHM.containsKey(sv)) {
                sv = cdataHM.get(sv);
            } else {
                E.warning("looks like a CDATA key, but not present? " + sv);
            }
        }

        xmlt.setStringValue(sv);
    }


    public int lineno() {
        return streamTokenizer.lineno();
    }


    public void initializeStreamTokenizer(StreamTokenizer st) {
        st.resetSyntax();
        st.eolIsSignificant(false);
        st.slashStarComments(false);
        st.slashSlashComments(false);
        st.lowerCaseMode(false);
        String slim = "AZaz09";
        st.wordChars(slim.charAt(0), slim.charAt(1));
        st.wordChars(slim.charAt(2), slim.charAt(3));
        st.wordChars(slim.charAt(4), slim.charAt(5));
        // st.wordChars(0x00A0, 0x00FF);


        String wsc = " \t\n";
        for (int i = 0; i < wsc.length(); i++) {
            int ic = wsc.charAt(i);
            st.whitespaceChars(ic, ic);
        }


        st.quoteChar(iq);

        String swc = "_/.:&;,()\'+-.[]{}$";
        for (int i = 0; i < swc.length(); i++) {
            int ic = swc.charAt(i);
            st.wordChars(ic, ic);
        }
    }


    public XMLToken nextToken() {
        XMLToken xmlt = new XMLToken();
        int itok = ntok(streamTokenizer);


        if (streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
            xmlt.setType(XMLToken.NONE);


        } else if (itok == iq) {
            xmlt.setType(XMLToken.STRING);
            // quoted string;
            String sss = streamTokenizer.sval;
            setStringValue(xmlt, StringEncoder.xmlUnescape(sss));


        } else if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
            xmlt.setType(XMLToken.STRING);
            setStringValue(xmlt, StringEncoder.xmlUnescape(streamTokenizer.sval));

        } else if (streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            xmlt.setType(XMLToken.NUMBER);
            // boolean, int or double, all as doubles;
            double d = streamTokenizer.nval;
            ntok(streamTokenizer);
            if (streamTokenizer.ttype == StreamTokenizer.TT_WORD
                    && ((streamTokenizer.sval).startsWith("E-")
                        || (streamTokenizer.sval).startsWith("E+") || (streamTokenizer.sval).startsWith("E"))) { // POSERR
                // -
                // catches
                // wrong
                // things?

                String s = streamTokenizer.sval.substring(1, streamTokenizer.sval.length());
                int ppp = Integer.parseInt(s);
                // err ("st.sval " + st.sval);
                // err ("read exponent: " + ppp);
                d *= Math.pow(10., ppp);
            } else {
                streamTokenizer.pushBack();
            }
            xmlt.setDValue(d);


        } else if (itok == iabo) {
            itok = ntok(streamTokenizer);
            String sv = streamTokenizer.sval;

            if (itok == iqm) {
                // should be the first line of a file - read on until
                // the next question mark, just keeping the text in sinfo
                // for now;
                xmlt.setType(XMLToken.INTRO);
                String svalue = "";
                itok = -1;
                while (itok != iqm) {
                    itok = ntok(streamTokenizer);
                    if (streamTokenizer.sval != null)
                        svalue += streamTokenizer.sval + " ";
                }
                setStringValue(xmlt, svalue);

            } else if (itok == iexc) {
                itok = ntok(streamTokenizer);
                String sval = streamTokenizer.sval;

                String svalue = "";
                if (sval != null && sval.startsWith("[CDATA[")) {
                    E.error("shouldn't get CDATA in xml tokenizer");

                } else if (sval.startsWith("--")) {
                    xmlt.setType(XMLToken.COMMENT);
                    svalue = streamTokenizer.sval.substring(2, streamTokenizer.sval.length()) + " ";
                    while (itok != iabc || !(svalue.endsWith("--"))) {
                        itok = ntok(streamTokenizer);
                        if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                            svalue += " " + streamTokenizer.sval;
                            //  pstok = streamTokenizer.sval;
                        } else if (streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                            svalue += " " + streamTokenizer.nval;
                            //  pstok = "";
                        }
                    }
                    xmlt.setStringValue(svalue.substring(0, svalue.length() -2));
                    streamTokenizer.pushBack();


                } else if (itok == ims) {
                    itok = ntok(streamTokenizer);
                    if (itok == ims) {
                        E.info("reading comment start as separate minus signs");
                        int[] ipr = new int[3];
                        while (ipr[0] != ims || ipr[1] != ims || ipr[2] != iabc) {
                            itok = ntok(streamTokenizer);

                            if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                                svalue += streamTokenizer.sval + " ";
                            } else if (streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                                svalue += " " + streamTokenizer.nval;
                            }
                            if (streamTokenizer.sval != null && streamTokenizer.sval.endsWith("--")) {
                                ipr[1] = ims;
                                ipr[2] = ims;
                            } else {
                                ipr[0] = ipr[1];
                                ipr[1] = ipr[2];
                                ipr[2] = itok;
                            }
                        }
                        streamTokenizer.pushBack();
                    } else {
                        E.error("found <!- but not followed by -  at " + streamTokenizer.lineno());
                    }
                } else {
                    E.error("found <! but not followed by -  at " + streamTokenizer.lineno());
                }
                setStringValue(xmlt, svalue);


            } else if (sv.startsWith("/")) {
                xmlt.setType(XMLToken.CLOSE);
                setStringValue(xmlt, sv.substring(1, sv.length()));

            } else {
                if (sv.endsWith("/")) {
                    xmlt.setType(XMLToken.OPENCLOSE);
                    setStringValue(xmlt, sv.substring(0, sv.length() - 1));
                } else {
                    xmlt.setType(XMLToken.OPEN);
                    setStringValue(xmlt, sv);
                }
            }

            itok = ntok(streamTokenizer);
            if (itok == iabc) {
                // fine - end of tag;

            } else if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                String[] attNV = new String[160]; // EFF check eff
                int natt = 0;

                while (itok != iabc) {

                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                        if (streamTokenizer.sval.equals("/")) {
                            xmlt.setType(XMLToken.OPENCLOSE);

                        } else {
                            attNV[2 * natt] = streamTokenizer.sval;
                            itok = ntok(streamTokenizer);
                            if (itok == ieq) {
                                itok = ntok(streamTokenizer);

                                if (itok == iq) {
                                    attNV[2 * natt + 1] = streamTokenizer.sval;
                                    natt++;
                                } else {
                                    E.error("expecting quoted string " + " while reading atributes "
                                                 + "but got " + stok(itok) + " sval=" + streamTokenizer.sval
                                                 + " nval=" + streamTokenizer.nval);
                                    E.info("original string was " + srcString);
                                }
                            } else {
                                E.error("at " + streamTokenizer.lineno()
                                             + " expecting = while reading attributes " + "but got " + stok(itok)
                                             + " sval=" + streamTokenizer.sval + " nval=" + streamTokenizer.nval);
                                E.info("original string was " + srcString);
                            }
                        }
                    } else {
                        E.error("at line " + streamTokenizer.lineno()
                                     + " found non-word while reading attributes " + stok(itok)
                                     + "  item so far = " + this);
                        E.info("original string was " + srcString);
                    }
                    itok = ntok(streamTokenizer);
                }
                String[] sat = new String[2 * natt];
                for (int i = 0; i < 2 * natt; i++) {
                    sat[i] = attNV[i];
                }
                xmlt.setAttributes(sat);

            } else {
                E.error("expecting word " + stok(itok));

            }

        } else {
            // just return the token as a string;
            xmlt.setType(XMLToken.STRING);
            setStringValue(xmlt, stok(itok));

        }
        return xmlt;
    }



    private int ntok(StreamTokenizer st) {
        int itok = -1;
        try {
            itok = st.nextToken();
        } catch (IOException e) {
            err(" " + e);
            itok = -999;
        }

        /*
         * if (count < 20) { E.info("token " + count + " " + itok + " " + st.sval + " " +
         * st.nval); count += 1; }
         */

        return itok;
    }


    private String stok(int itok) {
        return "" + (char)itok;
    }


    private void err(String s) {
        System.out.println(s);
    }

}
