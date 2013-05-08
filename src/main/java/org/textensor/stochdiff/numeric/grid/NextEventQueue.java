package org.textensor.stochdiff.numeric.grid;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.PriorityQueue;

import org.textensor.stochdiff.numeric.math.RandomGenerator;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import static org.textensor.stochdiff.numeric.chem.ReactionTable.getReactionSignature;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;
import org.textensor.stochdiff.numeric.grid.GridCalc;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import static org.textensor.stochdiff.numeric.grid.GridCalc.intlog;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class NextEventQueue {
    static final Logger log = LogManager.getLogger(NextEventQueue.class);

    public abstract class NextEvent {
        final int element;
        final String signature;

        double time;
        double propensity;

        NextEvent(int element, String signature) {
            this.element = element;
            this.signature = signature;
        }

        /**
         * Add and remove particles as appropriate for this event type.
         */
        abstract void execute(int[] reactionEvents,
                              int[][] diffusionEvents,
                              int[] stimulationEvents);

        /**
         * Calculate propensity of this event.
         */
        public abstract double _propensity();

        /**
         * Reculculate propensity and the next time.
         */
        double _calculate() {
            double old = this.propensity;
            this.propensity = this._propensity();
            log.debug("{}: propensity changed {} → {}",
                      this, old, this.propensity);
            return old;
        }

        void update(double current) {
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
        final int element2, index2;
        final int sp;
        final double fdiff;

        /**
         * @param element index of source element in particles array
         * @param element2 index of target element in particles array
         * @param index2 number of the target neighbor in list of neighbors
         * @param specie specie index
         * @param signature string to use in reporting
         * @param fdiff diffusion constant
         */
        NextDiffusion(int element, int element2, int index2,
                      int sp, String signature, double fdiff) {
            super(element, signature);
            this.element2 = element2;
            this.index2 = index2;
            this.sp = sp;
            this.fdiff = fdiff;

            this._calculate();
            updateEvent(this, random.exponential(this.propensity));

            log.debug("Created {}: t={}", this, this.time);
        }

        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents) {
            particles[this.element()][this.sp] -= 1;
            particles[this.element2][this.sp] += 1;

            assert particles[this.element()][this.sp] >= 0;

            diffusionEvents[this.sp][this.index2] += 1;
        }

        @Override
        public double _propensity() {
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

        @Override
        public String toString() {
            return String.format("%s el. %d→%d {}",
                                 getClass().getSimpleName(),
                                 element(), element2, signature);
        }
    }

    public class NextReaction extends NextEvent {
        final int[]
            reactants, products,
            reactant_stochiometry, product_stochiometry,
            reactant_powers;
        final int index;
        final double rate, volume;

        /**
         * @param index the index of this reaction in reactions array
         * @param element voxel number
         * @param reactants indices of reactants
         * @param products indices of products
         * @param reactant_stochiometry stochiometry of reactants
         * @param product_stochiometry stochiometry of products
         * @param reactant_powers coefficients of reactants
         * @param signature string to use in logging
         * @param rate rate of reaction
         * @param volume voxel volume
         */
        NextReaction(int index, int element, int[] reactants, int[] products,
                     int[] reactant_stochiometry, int[] product_stochiometry,
                     int[] reactant_powers, String signature,
                     double rate, double volume) {
            super(element, signature);
            this.index = index;
            this.reactants = reactants;
            this.products = products;
            this.reactant_stochiometry = reactant_stochiometry;
            this.product_stochiometry = product_stochiometry;
            this.reactant_powers = reactant_powers;

            this.rate = rate;
            this.volume = volume;

            this._calculate();
            updateEvent(this, random.exponential(this.propensity));

            log.debug("Created {} rate={} vol={} time={}", this,
                      this.rate, this.volume, this.time);
            assert this.time > 0;
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

        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents) {
            for (int i = 0; i < this.reactants.length; i++) {
                particles[this.element()][this.reactants[i]] -= this.reactant_stochiometry[i];
                assert particles[this.element()][this.reactants[i]] >= 0;
            }
            for (int i = 0; i < this.products.length; i++)
                particles[this.element()][this.products[i]] += this.product_stochiometry[i];
            reactionEvents[this.index] += 1;
        }

        @Override
        public double _propensity() {
            double prop = ExactStochasticGridCalc.calculatePropensity(this.reactants, this.products,
                                                                      this.reactant_stochiometry,
                                                                      this.product_stochiometry,
                                                                      this.reactant_powers,
                                                                      this.rate,
                                                                      this.volume,
                                                                      particles[this.element]);
            log.debug("{}: rate={} vol={} from {} propensity={}",
                      this, this.rate, this.volume,
                      particles[this.element],
                      prop);

            return prop;
        }

        @Override
        public String toString() {
            return String.format("%s el. %d %s",
                                 getClass().getSimpleName(),
                                 element(),
                                 signature);
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

    protected NextEventQueue(int[][] particles) {
        this.particles = particles;

        this.queue = new PriorityQueue(particles.length*3, new Sooner());
    }

    protected void updateEvent(NextEvent event, double time) {
        /* this should be optimized */
        this.queue.remove(event);
        event.time = time;
        this.queue.add(event);
    }

    void createDiffusions(VolumeGrid grid, ReactionTable rtab) {

        int[][] neighbors = grid.getPerElementNeighbors();
        double[][] couplings = grid.getPerElementCouplingConstants();
        double[] fdiff = rtab.getDiffusionConstants();
        String[] species = rtab.getSpecieIDs();

        for (int el = 0; el < neighbors.length; el++)
            for (int j = 0; j < neighbors[el].length; j++) {
                int el2 = neighbors[el][j];
                double cc = couplings[el][j];
                for (int sp = 0; sp < fdiff.length; sp++)
                    this.queue.add(new NextDiffusion(el, el2, j, sp, species[sp],
                                                     fdiff[sp] * cc));
            }
    }

    void createReactions(VolumeGrid grid, ReactionTable rtab) {
        double[] volumes = grid.getElementVolumes();
        int n = rtab.getNReaction() * volumes.length;
        int[][]
            RI = rtab.getReactantIndices(),
            PI = rtab.getProductIndices(),
            RS = rtab.getReactantStochiometry(),
            PS = rtab.getProductStochiometry(),
            RP = rtab.getReactantPowers();
        String[] species = rtab.getSpecieIDs();

        for (int r = 0; r < rtab.getNReaction(); r++) {
            int[] ri = RI[r], pi = PI[r], rs = RS[r], ps = PS[r], rp = RP[r];
            double rate = rtab.getRates()[r];

            for (int el = 0; el < volumes.length; el++) {
                String signature = getReactionSignature(ri, rs, pi, ps, species);
                this.queue.add(new NextReaction(r, el, ri, pi, rs, ps, rp,
                                                signature,
                                                rate, volumes[el]));
            }
        }
    }

    public static NextEventQueue create(int[][] particles,
                                        VolumeGrid grid, ReactionTable rtab) {
        NextEventQueue obj = new NextEventQueue(particles);
        obj.createDiffusions(grid, rtab);
        obj.createReactions(grid, rtab);

        return obj;
    }

    /**
     * Execute an event if the next event is before tstop.
     *
     * @returns Time of event.
     */
    public double advance(double tstop,
                          int[][] reactionEvents,
                          int[][][] diffusionEvents,
                          int[][] stimulationEvents) {
        NextEvent ev = this.queue.peek();
        assert ev != null;

        log.debug("Picked event {} t={}", ev, ev.time);

        if (ev.time > tstop)
            return tstop;

        double time = ev.time;
        ev.execute(reactionEvents[ev.element()],
                   diffusionEvents[ev.element()],
                   stimulationEvents[ev.element()]);
        ev.update(time);
        return time;
    }
}
