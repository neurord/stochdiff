package org.catacomb.druid.util.tree;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;


public class RelationTree implements PivotedTree {


    ArrayListNode rootNode;

    HashMap<Related, RelationNode> peers;

    HashSet<String> relationTypes;

    String[][] pivotOrders;

    String[] pivotNames;


    int iPivot;

    int rootPolicy = Tree.AUTO_ROOT;

    TreeChangeReporter tcReporter;



    public RelationTree(ArrayList<Related> coll) {
        init(coll);
        setDefaultPivotOrders();
        build(0);
    }


    public RelationTree(SingleParent sp) {
        ArrayList<Related> coll = Trawler.trawlChildren(sp);
        init(coll);
        setDefaultPivotOrders();
        build(0);
    }


    public TreeNode getRoot() {
        return rootNode;
    }


    public void setRootPolicy(int ipol) {
        rootPolicy = ipol;
    }


    public int getRootPolicy() {
        return rootPolicy;
    }


    public String getPivotRelation() {
        return pivotNames[iPivot];
    }


    public void init(ArrayList<Related> coll) {

        peers = new HashMap<Related, RelationNode>();
        relationTypes = new HashSet<String>();


        ArrayList<RelationNode> nodes = addAll(coll);
        // resolve target references to nodes and
        // extract different relation types

        // nodes is same as valcol ArrayList valcol = peers.values();
        resolveAll(nodes);


        String[] sa = (relationTypes.toArray(new String[0]));
        setPivotNames(sa);

        // at this stage, there is just one relationNode for each
        // source object. Most branch nodes will need duplicating a
        // few times according to the number of distinct tree paths
        // they could occur on.
    }



    private ArrayList<RelationNode> addAll(ArrayList<Related> coll) {
        ArrayList<RelationNode> arl = new ArrayList<RelationNode>();

        for (Related related : coll) {
            RelationNode rnode = new RelationNode(null, related);
            peers.put(related, rnode);
            arl.add(rnode);
        }
        return arl;
    }



    private void resolveAll(ArrayList<RelationNode> coll) {
        for (RelationNode rnode : coll) {
            rnode.resolve(peers, relationTypes);
        }
    }



    // called whenever an item is added to the model this tree is representing.
    // The item should be set up first, then the tree should be notified.
    private void newItem(Related parent, Related child) {
        RelationNode rnode = new RelationNode(parent, child);
        peers.put(child, rnode);
        rnode.resolve(peers, relationTypes);

        if (tcReporter != null) {
            tcReporter.nodeAddedUnder((RelationNode)(rnode.getParent()), rnode);
        }

    }



    public void newBranch(Related parent, Related child) {
        if (child instanceof SingleParent) {
            ArrayList<Related> coll = Trawler.trawlChildren((SingleParent)child);
            ArrayList<RelationNode> nodes = addAll(coll);
            resolveAll(nodes);

        } else {
            newItem(parent, child);
        }
        build(iPivot); // EFF economize;
    }


    public void childRemoved(Related parent, Related child) {
        RelationNode rnParent = peers.get(parent);
        RelationNode rnChild = peers.get(child);
        peers.remove(child);
        build(iPivot); // EFF economize;
        if (tcReporter != null) {
            tcReporter.nodeRemoved(rnParent, rnChild);
        }
    }


    public void setDefaultPivotOrders() {
        String[] sa1 = (relationTypes.toArray(new String[0]));

        String[] sa2 = new String[sa1.length];
        for (int i = 0; i < sa1.length; i++) {
            sa2[i] = sa1[sa1.length - 1 - i];
        }
        String[][] sa = { sa1, sa2 };
        setPivotOrders(sa);
    }



    public void setPivotOrders(String[][] saa) {
        pivotOrders = saa;
    }



    public String[] getPivotNames() {
        if (pivotNames == null) {
            String[] sa = { "pivot 1", "pivot 2" };
            setPivotNames(sa);
        }
        return pivotNames;
    }


    public void setPivotNames(String[] sa) {
        pivotNames = sa;
    }


    public void repivot(String s) {
        int ibo = 0;
        String[] sa = getPivotNames();
        for (int i = 0; i < sa.length; i++) {
            if (sa[i].equals(s)) {
                ibo = i;
            }
        }
        build(ibo);
    }



    public void build(int poin) {
        int pivotOrder = poin;
        if (pivotOrder >= pivotOrders.length) {
            System.out.println("WARNING - requested non-existent pivot order " + pivotOrder);
            pivotOrder = 0;
        }
        iPivot = pivotOrder;


        // put all nodes in top level set;
        ArrayList<RelationNode> rest = new ArrayList<RelationNode>();
        rest.addAll(peers.values());

        for (RelationNode rn : rest) {
            rn.clearChildren();
        }

        // iterate over relation types
        String[] apo = pivotOrders[pivotOrder];


        String srel = null;
        if (apo.length > 0) {
            srel = apo[0];
        }
        ArrayList<RelationNode> roots = getRoots(rest, srel);
        if (rootNode == null) {
            rootNode = new ArrayListNode(null, "root");
        }

        rootNode.setChildren(roots);


        // TODO at this stage, we're pivoted only on the first criterion;

        /*
         * for (int icrit = 1; icrit < apo.length; icrit++) { treeify(roots, rest,
         * apo[icrit]); }
         */

        if (rest.isEmpty()) {

        } else {
            ArrayListNode aln = new ArrayListNode(rootNode, "other");
            aln.setChildren(rest);
            rootNode.addChild(aln);
        }
    }



    public void treeify(ArrayList<RelationNode> roots, ArrayList<RelationNode> rest, String rel) {
        // recurse down each root tree until we find a node with a
        // relation rel
        // run up that relation as far as possible and insert section
        // check for top being already a child of parent
        // if so insert, run down and insert branch if needed
        // ow, insert whole new section

        for (RelationNode rnode : roots) {
            rnode.subtreeify(null, rest, rel); // POSERR null, or root node?
        }

    }



    // file everything according to rel, returns a set of root nodes all of
    // which have children. Unfiled elements are left in rest
    private ArrayList<RelationNode> getRoots(ArrayList<RelationNode> rest, String rel) {

        if (rel != null) {
            // file away what we can;
            ListIterator<?> nodeiter = rest.listIterator();
            while (nodeiter.hasNext()) {
                RelationNode rnode = (RelationNode)(nodeiter.next());
                if (rnode.fileAway(rel)) {
                    nodeiter.remove();
                }
            }
        }



        // copy childed elements to return aray, remove from rest;
        ArrayList<RelationNode> rwc = new ArrayList<RelationNode>();
        ListIterator<?> restiter = rest.listIterator();
        while (restiter.hasNext()) {
            RelationNode rnode = (RelationNode)(restiter.next());
            if (rnode.hasChildren()) {
                rwc.add(rnode);
                restiter.remove();
            }
        }
        return rwc;
    }



    public Object[] getPathTo(Related child) {
        ArrayList<Object> retal = new ArrayList<Object>();

        Object[] ret = null;

        RelationNode rnode = peers.get(child);

        if (rnode == null) {
            E.warning("relation tree has no peer for " + child + " ?");

        } else {
            String srel = getPivotRelation();

            rnode = rnode.getParent(srel);
            // dont expand out the child, make sure it is visble,
            // not its children. Also if the child is a leaf it would turn
            // the jtree expand path into a no-op if included in the path

            while (rnode != null) {
                retal.add(0, rnode);
                rnode = rnode.getParent(srel);
            }
            retal.add(0, rootNode);
            ret = retal.toArray();
        }
        return ret;
    }


    public void setTreeChangeReporter(TreeChangeReporter tcr) {
        tcReporter = tcr;
    }


    public Object[] getObjectPath(String s, boolean b) {
        E.missing();
        return null;
    }



}
