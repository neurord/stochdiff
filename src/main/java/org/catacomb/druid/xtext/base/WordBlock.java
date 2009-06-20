package org.catacomb.druid.xtext.base;

import org.catacomb.druid.xtext.data.XType;
import org.catacomb.interlish.structure.TextDisplayed;




public class WordBlock extends TextBlock implements TextDisplayed {


    WordBlock linkee;

    public WordBlock() {
        super();
    }

    public WordBlock(String s) {
        super(s);
    }

    public TextBlock makeCopy() {
        return new WordBlock();
    }


    public String getExtendedText() {
        String txt = text;
        if (linkee != null) {
            txt += " " + linkee.getExtendedText();
        }
        return txt;
    }


    public boolean matches(TextBlock tb) {
        return (tb instanceof WordBlock);
    }


    public void toggleLink(WordBlock wb) {
        if (linkee == null) {
            linkee = wb;
        } else {
            linkee = null;
        }
    }

    public boolean hasLinkee() {
        return (linkee != null);
    }

    public WordBlock getLinkee() {
        return linkee;
    }


    public void weakSetType(XType xt) {
        if (getType() == null) {
            setType(xt);
            if (linkee != null) {
                linkee.weakSetType(xt);
            }
        } else {
            //  E.info("weak set type not setting - already got type " + getType());
        }
    }


    public String getDisplayText() {
        return getExtendedText();
    }


}
