package org.catacomb.dataview;

import org.catacomb.graph.gui.DataDisplay;
import org.catacomb.report.E;


public class Plotter {


    // not much here -= just testing vm exit
    public static void main(String[] argv) {

        DataDisplay.setBatch();

        // DataDisplay dd = new DataDisplay(100, 100);

//      WorldCanvas wc = new WorldCanvas(100, 100);

        showThreads();

    }




    public static void showThreads() {

        // Find the root thread group
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }

        // Visit each thread group
        visit(root, 0);

    }

    // This method recursively visits all thread groups under `group'.
    public static void visit(ThreadGroup group, int level) {
        // Get threads in `group'
        int numThreads = group.activeCount();
        Thread[] threads = new Thread[numThreads*2];
        numThreads = group.enumerate(threads, false);

        // Enumerate each thread in `group'
        for (int i=0; i<numThreads; i++) {
            // Get thread
            Thread thread = threads[i];
            E.info("Thread: " + thread.isDaemon() + " " + thread);
        }

        // Get thread subgroups of `group'
        int numGroups = group.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[numGroups*2];
        numGroups = group.enumerate(groups, false);

        // Recursively visit each subgroup
        for (int i=0; i<numGroups; i++) {
            visit(groups[i], level+1);
        }
    }

}
