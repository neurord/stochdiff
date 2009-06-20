package org.catacomb.druid.xtext.base;



public class NewlineBlock extends TextBlock {


    public NewlineBlock() {
        super();
    }


    public TextBlock makeCopy() {
        return new NewlineBlock();
    }


    public int textLength() {
        return 1;
    }


    public boolean matches(TextBlock tb) {
        return (tb instanceof NewlineBlock);
    }




}
