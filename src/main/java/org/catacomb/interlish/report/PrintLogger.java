package org.catacomb.interlish.report;




public class PrintLogger implements Logger {

    public void log(String s) {
        System.out.println(s);
    }

    public void log(Message m) {
        log(m.toString());
    }

    public void optionalIncrementLog(int ifr, String string) {
        // TODO Auto-generated method stub

    }

    public void init(String string) {
        // TODO Auto-generated method stub

    }

    public void end() {
        // TODO Auto-generated method stub

    }

}
