package org.catacomb.be;


public class ChangeEvent {

    String type;
    Object src;

    int count;

    private static int counter = 0;


    // REFAC - clearer

    private ChangeEvent() {
        count = counter;
    }

    public ChangeEvent(String s, Object obj) {
        type = s;
        src = obj;
        count = counter;
        counter += 1;
    }


    public Object getSource() {
        return src;
    }

    private ChangeEvent(ChangeEvent ce) {
        count = ce.count;
        src = ce.src;
        type = ce.type;
    }

    public ChangeEvent makeCopy() {
        return new ChangeEvent(this);
    }


    public boolean laterThan(ChangeEvent ce) {
        return (ce == null || count > ce.count);
    }


}
