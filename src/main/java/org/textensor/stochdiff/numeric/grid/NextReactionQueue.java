package org.textensor.numeric.grid;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.PriorityQueue;

import org.textensor.stochdiff.numeric.math.RandomGenerator;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;
import org.textensor.stochdiff.numeric.grid.GridCalc;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import static org.textensor.stochdiff.numeric.grid.GridCalc.intlog;

public class NextReactionQueue {

    public abstract class NextEvent {
        final int element;

        double time;
        double propensity;

        NextEvent(int element) {
            this.element = element;
        }

        /**
         * Add and remove particles as appropriate for this event type.
         */
        abstract void execute();

        /**
         * Calculate propensity of this event.
         */
        public abstract double propensity();

        /**
         * Reculculate propensity and the next time.
         */
        double _calculate() {
            double old = this.propensity;
            this.propensity = this.propensity();
            return old;
        }

        void calculate(double current) {
            /* .time is not updated until the event is removed from queue */
            this._calculate();
            updateEvent(this, current + random.exponential(this.propensity));

            for (NextEvent dependent: this.dependent) {
                double old = dependent._calculate();
                double t = (dependent.time - current) * old / dependent.propensity + current;
                updateEvent(dependent, t);
            }
        }

        Collection<NextEvent> dependent = inst.newArrayList();

        public abstract int[] reactants();

        public int element() {
            return element;
        }

        public abstract void addDependent(Collection<? extends NextEvent> coll);
    }

    public class NextDiffusion extends NextEvent {
        final int element2;
        final int sp;
        final double fdiff;

        NextDiffusion(int element, int element2, int sp, double fdiff) {
            super(element);
            this.element2 = element2;
            this.sp = sp;
            this.fdiff = fdiff;

            this._calculate();
        }

        void execute() {
            particles[this.element()][this.sp] -= 1;
            particles[this.element2][this.sp] += 1;
        }

        @Override
        public double propensity() {
            return this.fdiff * particles[this.element][this.sp];
        }

        public int[] reactants() {
            return new int[] {this.sp};
        }

        public void addDependent(Collection<? extends NextEvent> coll) {
            ArrayList<NextEvent> d = inst.newArrayList();
            for (NextEvent e: coll)
                if ((e.element() == this.element() ||
                     e.element() == this.element2) &&
                    ArrayUtil.intersect(e.reactants(), this.sp))
                    this.dependent.add(e);
        }
    }

    public class NextReaction extends NextEvent {
        final int[]
            reactants, products,
            reactant_stochiometry, product_stochiometry,
            reactant_powers;
        final double rate, volume;

        NextReaction(int element, int[] reactants, int[] products,
                     int[] reactant_stochiometry, int[] product_stochiometry,
                     int[] reactant_powers,
                     double rate, double volume) {
            super(element);
            this.reactants = reactants;
            this.products = products;
            this.reactant_stochiometry = reactant_stochiometry;
            this.product_stochiometry = product_stochiometry;
            this.reactant_powers = reactant_powers;

            this.rate = rate;
            this.volume = volume;

            this._calculate();
        }

        public int[] reactants() {
            return this.reactants;
        }

        public void addDependent(Collection<? extends NextEvent> coll) {
            for (NextEvent e: coll) {
                if (e.element() == this.element() &&
                    (ArrayUtil.intersect(e.reactants(), this.reactants) ||
                     ArrayUtil.intersect(e.reactants(), this.products)))
                    this.dependent.add(e);
            }
        }

        void execute() {
            for (int i = 0; i < this.reactants.length; i++)
                particles[this.element()][this.reactants[i]] -= this.reactant_stochiometry[i];
            for (int i = 0; i < this.products.length; i++)
                particles[this.element()][this.products[i]] += this.product_stochiometry[i];
        }

        @Override
        public double propensity() {
            Object[] java_sucks = GridCalc.calculatePropensity(this.reactants, this.products,
                                                               this.reactant_stochiometry,
                                                               this.product_stochiometry,
                                                               this.reactant_powers,
                                                               Math.log(this.rate),
                                                               Math.log(this.volume),
                                                               particles[this.element]);
            return (Double)java_sucks[1] > 0 ? (Double)java_sucks[0] : Double.NEGATIVE_INFINITY;
        }
    }

    public static class Sooner implements Comparator<NextEvent> {
        @Override
        public int compare(NextEvent a, NextEvent b) {
            return Double.compare(a.time, b.time);
        }
    }

    final RandomGenerator random = new MersenneTwister();

    /**
     * Particle counts: [voxels × species]
     */
    final int[][] particles;
    final PriorityQueue<NextEvent> queue;

    public NextReactionQueue(int[][] particles) {
        this.particles = particles;

        this.queue = new PriorityQueue(particles.length*3, new Sooner());
    }

    void updateEvent(NextEvent event, double time) {
        this.queue.remove(event);
        event.time = time;
        this.queue.add(event);
    }

    public void createDiffusions(VolumeGrid grid, ReactionTable rtab) {

        int[][] neighbors = grid.getPerElementNeighbors();
        double[][] couplings = grid.getPerElementCouplingConstants();
        double[] fdiff = rtab.getDiffusionConstants();

        for (int el = 0; el < neighbors.length; el++)
            for (int j = 0; j < neighbors[el].length; j++) {
                int el2 = neighbors[el][j];
                double cc = couplings[el][j];
                for (int sp = 0; sp < fdiff.length; sp++)
                    this.queue.add(new NextDiffusion(el, el2, sp, fdiff[sp] * cc));
            }
    }

    public void createReactions(VolumeGrid grid, ReactionTable rtab) {
        double[] volumes = grid.getElementVolumes();
        int n = rtab.getNReaction() * volumes.length;
        int[][]
            RI = rtab.getReactantIndices(),
            PI = rtab.getProductIndices(),
            RS = rtab.getReactantStochiometry(),
            PS = rtab.getProductStochiometry(),
            RP = rtab.getReactantPowers();

        for (int r = 0; r < rtab.getNReaction(); r++) {
            int[] ri = RI[r], pi = PI[r], rs = RS[r], ps = PS[r], rp = RP[r];
            double rate = rtab.getRates()[r];

            for (int el = 0; el < volumes.length; el++)
                this.queue.add(new NextReaction(el, ri, pi, rs, ps, rp,
                                                rate, volumes[el]));
        }
    }

    public static NextReactionQueue create(VolumeGrid grid, ReactionTable rtab) {
        int[][] particles = new int[grid.size()][rtab.getNSpecies()];

        NextReactionQueue obj = new NextReactionQueue(particles);
        obj.createDiffusions(grid, rtab);
        obj.createReactions(grid, rtab);

        return obj;
    }
}
