package org.catacomb.druid.xtext.base;



import org.catacomb.druid.xtext.StandardGuises;
import org.catacomb.druid.xtext.data.XType;
import org.catacomb.interlish.content.BasicTouchTime;
import org.catacomb.report.E;

public abstract class TextBlock extends Block {


    public String text;
    int nchar;

    int[] cachedLimits;

    XType type;

    BasicTouchTime cacheTime;

    // each block needs next and previous
    // container blocks should have two sub-blocks start and end, that
    // go in the linked list (containers don't go in linked list)


    public TextBlock() {
        super();

        cachedLimits = new int[4];
        cacheTime = new BasicTouchTime();
    }


    public TextBlock(String s) {
        this();
        text = s;
    }


    public int[] getCachedPosition() {
        return cachedLimits;
    }


    public void setCachedPosition(int x, int y, int w, int h) {
        cachedLimits[0] = x;
        cachedLimits[1] = y;
        cachedLimits[2] = w;
        cachedLimits[3] = h;
    }


    public void setText(String s) {
        text = s;
        nchar = s.length();
    }


    public int textLength() {
        return nchar;
    }


    public String getText() {
        return text;
    }

    public String getExtendedText() {
        return text;
    }

    boolean isText() {
        return (text != null);
    }





    public TextBlock nextTextBlock() {
        TextBlock ret = null;
        Block b = this;
        while (b.hasNext()) {
            b = b.next();
            if (b instanceof TextBlock) {
                ret = (TextBlock)b;
                break;
            }
        }
        return ret;
    }



    public TextBlock previousTextBlock() {
        TextBlock ret = null;
        Block b = this;
        while (b.hasPrevious()) {
            b = b.previous();
            if (b instanceof TextBlock) {
                ret = (TextBlock)b;
                break;
            }
        }
        return ret;
    }





    public WordBlock getNextWordBlock() {
        WordBlock ret = null;
        Block b = this;
        while (b.hasNext()) {
            b = b.next();
            if (b instanceof WordBlock) {
                ret = (WordBlock)b;
                break;
            }
        }
        return ret;
    }



    public WordBlock getPreviousWordBlock() {
        WordBlock ret = null;
        Block b = this;
        while (b.hasPrevious()) {
            b = b.previous();
            if (b instanceof WordBlock) {
                ret = (WordBlock)b;
                break;
            }
        }
        return ret;
    }




    public void insertCharacter(char c, int ipos) {
        if (ipos == nchar) {
            setText(text + c);
        } else if (ipos == 0) {
            setText(c + text);
        } else {
            setText(text.substring(0, ipos) + c + text.substring(ipos, nchar));
        }

    }


    public void newlineAfter() {
        Block b = new NewlineBlock();
        b.setNext(next());
        setNext(b);
    }


    public void insertNewline(int pos) {
        insert(new NewlineBlock(), pos);
    }


    public abstract TextBlock makeCopy();

    public abstract boolean matches(TextBlock tb);


    public void insert(TextBlock b_middle, int caretPos) {
        String texta = text.substring(0, caretPos);
        String textb = text.substring(caretPos, nchar);

        TextBlock b_end = makeCopy();

        Block b_after = next();
        this.setText(texta);
        b_end.setText(textb);

        this.setNext(b_middle);
        b_middle.setNext(b_end);
        b_end.setNext(b_after);
    }



    public void remove() {
        if (previous() == null) {
            E.warning("null previous?");
        } else {
            Block pr = previous();
            Block nx = next();
            if (pr instanceof TextBlock && nx instanceof TextBlock) {
                TextBlock tpr = (TextBlock)pr;
                TextBlock tnx = (TextBlock)nx;

                if (tpr.matches(tnx)) {
                    tpr.append(tnx);

                } else {
                    E.warning("shouldnt get here? " + pr.getClass() + " " + nx.getClass());
                    previous().setNext(next());
                }
            }
        }
    }


    public void append(TextBlock tb) {
        Block bn = tb.next();
        setText(text + tb.getText());
        setNext(bn);
    }


    public void deleteCharBefore(int caretPos) {
        String tnew = text.substring(0, caretPos-1) + text.substring(caretPos, nchar);
        setText(tnew);
    }






    public void setType(XType xt) {
        if (type != null) {

        }
        type = xt;
        notifyAppearanceChange();
    }

    public XType getType() {
        return type;
    }

    public Guise getGuise() {
        Guise ret = null;
        if (type != null) {
            ret = type.getGuise();
        } else {
            ret = StandardGuises.getDefault();
        }
        return ret;
    }

}
