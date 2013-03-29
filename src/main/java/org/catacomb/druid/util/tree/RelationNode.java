package org.catacomb.druid.util.tree;


import org.catacomb.interlish.structure.Related;
import org.catacomb.interlish.structure.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;




public class RelationNode extends ArrayListNode {


    public Related peer;


    String[] types;
    RelationNode[] targets;


    HashMap<Related, RelationNode> childPeerHM;
    HashMap<String, RelationNode> parentHM;


    public RelationNode(Object parent, Related pr) {
        super(parent, "anon");
        peer = pr;
    }


    public Related getPeer() {
        return peer;
    }

    public boolean samePeer(RelationNode rn) {
        return (peer == rn.peer);
    }



    public void clearChildren() {
        if (childPeerHM != null) {
            childPeerHM.clear();
        }
        super.clearChildren();
    }



    public RelationNode makeChildlessCopy() {
        RelationNode rn = new RelationNode(null, peer);
        return rn;
    }



    public void addChild(ArrayListNode aln) {
        super.addChild(aln);
        if (aln instanceof RelationNode) {
            RelationNode rn = (RelationNode)aln;
            if (childPeerHM == null) {
                childPeerHM = new HashMap<Related, RelationNode>();
            }
            childPeerHM.put(rn.getPeer(), rn);
        }
    }


    public void removeChild(RelationNode rn) {
        super.removeChild(rn);
        childPeerHM.remove(rn.getPeer());
    }



    public RelationNode getPeerEquivalentChild(RelationNode rn) {
        RelationNode ret = null;
        Related tgtpeer = rn.getPeer();
        if (childPeerHM != null && childPeerHM.containsKey(tgtpeer)) {
            ret = childPeerHM.get(tgtpeer);
        }
        return ret;
    }



    public void resolve(HashMap<Related, RelationNode> peers, HashSet<String> relationTypes) {
        Relationship[] rs = peer.getRelationships();

        parentHM = new HashMap<String, RelationNode>();

        int nrel = 0;
        if (rs != null) {
            nrel = rs.length;
        }

        types = new String[nrel];
        targets = new RelationNode[nrel];

        for (int i = 0; i < nrel; i++) {
            String rel = rs[i].getType();
            types[i] = rel;
            Related tgt = rs[i].getTarget();
            RelationNode rnode = peers.get(tgt);
            targets[i] = rnode;


            if (parentHM.containsKey(rel)) {
                // do nothing  ? POSERR;
            } else {
                parentHM.put(rel, rnode);
            }

            if (relationTypes.contains(rel)) {
                // nothing to do;
            } else {
                relationTypes.add(rel);
            }

        }

    }



    public String toString() {
        return peer.toString();
    }



    public boolean fileAway(String rtyp) {
        boolean bfiled = false;

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(rtyp)) {
                targets[i].addChild(this);
                bfiled = true;
            }
        }
        return bfiled;
    }




    public RelationNode getParent(String rel) {
        RelationNode ret = null;
        if (parentHM.containsKey(rel)) {
            ret = parentHM.get(rel);
        }
        parent = ret;
        return ret;
    }





    // recurse down until we find a node with a
    // relation rel
    // run up that relation as far as possible and insert section
    // check for top being already a child of parent
    // if so insert, run down and insert branch if needed
    // ow, insert whole new section

    public void subtreeify(RelationNode parentRN, ArrayList rest, String rel) {


        System.out.println("XXX subtreeifying  " + parentRN + " " + rel);


        // what if multiple occurences of rel (here have just the first) POSERR
        if (parentRN != null && parentHM.containsKey(rel)) {
            // skips root nodes, but maybe these could also have a rel parent? POSERR

            System.out.println("YYY moving down " + rel + " " + this);

            parentRN.removeChild(this);
            RelationNode target = parentHM.get(rel);
            insertUnder(parentRN, target, rest, rel);

        } else {

            RelationNode[] ach;
            ach = children.toArray(new RelationNode[0]);
            for (int i = 0; i < ach.length; i++) {
                ach[i].subtreeify(this, rest, rel);
            }
        }
    }



    private void insertUnder(RelationNode parentRN, RelationNode bot,
                             ArrayList rest, String rel) {
        int nup = 0;
        RelationNode[] path = new RelationNode[6];
        path[nup++] = bot;
        RelationNode current = bot;
        RelationNode next;

        while ((next = current.getParent(rel)) != null) {
            path[nup++] = next;
            current = next;
        }

        // maybe this path matches existing children of parent;
        RelationNode branch = parentRN;
        int id = nup-1;
        while (id > 0) {
            RelationNode req = branch.getPeerEquivalentChild(path[id]);
            if (req == null) {
                break;

            } else {
                branch = req;
                id = id - 1;
            }
        }


        // insert the remaining segment;
        for (int i = id; i >= 0; i--) {
            RelationNode dtr = path[i].makeChildlessCopy();
            branch.addChild(dtr);
            branch = dtr;
            rest.remove(path[i]);
        }

        branch.addChild(this);
    }


}
