package org.catacomb.druid.xtext.base;



public class PunctuationBlock extends TextBlock {


    public PunctuationBlock() {
        super();
    }

    public TextBlock makeCopy() {
        return new PunctuationBlock();
    }


    public boolean matches(TextBlock tb) {
        return (tb instanceof PunctuationBlock);
    }



}
