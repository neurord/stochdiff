package org.textensor.stochdiff.numeric.grid;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import org.textensor.stochdiff.numeric.math.RandomGenerator;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable.Stimulation;
import static org.textensor.stochdiff.numeric.chem.ReactionTable.getReactionSignature;
import org.textensor.util.Settings;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;
import org.textensor.stochdiff.numeric.grid.GridCalc;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import static org.textensor.stochdiff.numeric.grid.GridCalc.intlog;
import org.textensor.stochdiff.numeric.stochastic.StepGenerator;
import org.textensor.stochdiff.numeric.stochastic.InterpolatingStepGenerator;
import static org.textensor.stochdiff.numeric.BaseCalc.distribution_t.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;

public class NextEventQueue {
    static final Logger log = LogManager.getLogger(NextEventQueue.class);

    final boolean update_times = Settings.getProperty("stochdiff.neq.update_times", true);

    public interface Node {
        int index();
        void setIndex(int index);
        double time();
    }

    public class PriorityTree<T extends Node> {
        T[] nodes;
        long swaps = 0;

        protected T child(T a, int which) {
            assert which < 2;
            int ch = (a.index()+1)*2 - 1 + which;
            return ch < this.nodes.length ? this.nodes[ch] : null;
        }

        protected T parent(T a) {
            int ch = (a.index()+1)/2 - 1;
            if (ch < 0)
                return null;
            return this.nodes[ch];
        }

        protected T littlestChild(T a) {
            T child = this.child(a, 0);
            if (child == null)
                return null;
            T child2 = this.child(a, 1);
            if (child2 == null)
                return child;
            if (child.time() <= child2.time())
                return child;
            return child2;
        }

        void swap(T a, T b) {
            assert this.parent(b) == a;
            int ai = a.index(),
                bi = b.index();
            this.nodes[ai] = b;
            this.nodes[bi] = a;
            a.setIndex(bi);
            b.setIndex(ai);

            this.swaps += 1;
        }

        void build(T[] nodes) {
            if (!update_times)
                log.info("stochdiff.neq.update_times is false, will regenerate times");

            Comparator<T> c = new Comparator<T>() {
                @Override
                public int compare(T a, T b) {
                    return Double.compare(a.time(), b.time());
                }
            };
            Arrays.sort(nodes, c);

            for (int i = 0; i < nodes.length; i++)
                nodes[i].setIndex(i);

            this.nodes = nodes;
        }

        T first() {
            return this.nodes[0];
        }

        void reposition(String prefix, T node) {
            assert node != null;
            T parent = this.parent(node);
            log.debug("{}: updating position of {} t={} parent={}",
                      prefix, node, node.time(), parent);

            if (parent != null && parent.time() > node.time()) {
                this.swap(parent, node); // original parent first
                this.reposition(prefix, node);
            } else {
                T littlest = this.littlestChild(node);
                log.debug("littlest Child is {} t={}", littlest,
                          littlest != null ? littlest.time() : "-");
                if (littlest != null && node.time() > littlest.time()) {
                    this.swap(node, littlest); // original parent first
                    this.reposition(prefix + "-", node);
                }
            }
        }
    }

    public abstract class NextEvent implements Node {
        int index;

        final private int element;
        final String signature;
        final private int[] reactants;

        protected double time;
        protected int extent;

        double propensity;

        NextEvent(int element, String signature, int... reactants) {
            this.element = element;
            this.signature = signature;
            this.reactants = reactants;
            this.extent = 1;
        }

        @Override
        public int index() {
            return this.index;
        }

        @Override
        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public double time() {
            return this.time;
        }

        /**
         * Add and remove particles as appropriate for this event type.
         */
        abstract void execute(int[] reactionEvents,
                              int[][] diffusionEvents,
                              int[] stimulationEvents,
                              int count);

        /**
         * Calculate propensity of this event.
         */
        abstract double _propensity();

        /**
         * Calculate the time for which <b>this reaction</b> changes the population
         * of <b>products</b> by ɛ. Propensity is not recalculated, so must be brought
         * up-to-date externally.
         */
        abstract double leap_time(double current, double tolerance);

        /**
         * Calculate the (randomized) extent of the reaction based in the time given.
         */
        abstract int leap_count(double current, double time);

        double _new_time(double current) {
            double exp = random.exponential(this.propensity);
            log.debug("generating exponential time for prop={} → time={}", this.propensity, exp);
            return current + exp;
        }

        /**
         * Reculculate propensity. Return old.
         */
        int[] old_pop;
        double _update_propensity(boolean warn) {
            double old = this.propensity;
            int[] pop = this.reactantPopulation();
            this.propensity = this._propensity();
            log.log(warn && this.propensity != 0 && this.propensity == old ? Level.WARN : Level.DEBUG,
                    "{}: propensity changed {} → {} (n={} → {})",
                    this, old, this.propensity, old_pop, pop);
            this.old_pop = pop;
            return old;
        }

        private int[] reactantPopulation() {
            int[] react = this.reactants();
            int[] pop = new int[react.length];
            for (int i = 0; i < react.length; i++)
                pop[i] = particles[this.element()][react[i]];
            return pop;
        }

        void update(int[][] reactionEvents,
                    int[][][] diffusionEvents,
                    int[][] stimulationEvents,
                    double current) {

            assert this.extent >= 0: this.extent;

            final boolean changed = this.extent > 0;
            if (changed)
                this.execute(reactionEvents[this.element()],
                             diffusionEvents[this.element()],
                             stimulationEvents[this.element()],
                             this.extent);

            // In reactions of the type Da→Da+MaI the propensity does not change
            // after execution, but there's nothing to warn about.
            this._update_propensity(false);

            final double normal = this._new_time(current);
            final double leap = leap_min_jump == 0 ? Double.NaN
                : this.leap_time(current, tolerance);

            log.debug("Updating {}", this);
            log.debug("deps: {}", this.dependent);
            log.debug("options: {} → {} wait {}, leap {}",
                      current, normal,
                      normal - current, leap);

            if (leap_min_jump != 0 && leap > (normal - current) * leap_min_jump) {
                assert update_times;

                int count = this.leap_count(current, leap);

                log.debug("leaping {} extent {} until time={}", leap, count, current + leap);
                this.time = current + leap;
                this.extent = count;
            } else {
                log.debug("waiting {} until time={}", normal - current, normal);
                this.time = normal;
                this.extent = 1;
            }

            queue.reposition("update", this);

            for (NextEvent dep: this.dependent) {
                double old = dep._update_propensity(changed);
                if (update_times && !Double.isInfinite(dep.time))
                    dep.time = (dep.time - current) * old / dep.propensity + current;
                else
                    dep.time = dep._new_time(current);
                queue.reposition("upd.dep", dep);
            }
        }

        Collection<NextEvent>
            dependent = inst.newArrayList(),
            dependon = inst.newArrayList();

        public int[] reactants() {
            return this.reactants;
        }

        public int element() {
            return element;
        }

        public abstract void addRelations(Collection<? extends NextEvent> coll);

        protected void addDependent(NextEvent ev) {
            if (this.dependent.contains(ev))
                return;

            this.dependent.add(ev);
            ev.dependon.add(this);
        }
    }

    static void log_dependency_edges(ArrayList<NextEvent> events) {
        int all = 0, active = 0;
        for (NextEvent ev: events) {
            all += ev.dependent.size();
            if (ev.propensity > 0)
                active += ev.dependent.size();
        }

        log.info("{} dependency edges, {} active", all, active);
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
            super(element, signature, sp);
            this.element2 = element2;
            this.index2 = index2;
            this.sp = sp;
            this.fdiff = fdiff;

            this.propensity = this._propensity();
            this.time = this.propensity > 0 ? this._new_time(0) : Double.POSITIVE_INFINITY;

            log.debug("Created {}: t={}", this, this.time);
        }

        @Override
        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents,
                     int count) {
            particles[this.element()][this.sp] -= count;
            particles[this.element2][this.sp] += count;

            assert particles[this.element()][this.sp] >= 0;

            diffusionEvents[this.sp][this.index2] += count;
        }

        @Override
        public double _propensity() {
            return this.fdiff * particles[this.element()][this.sp];
        }

        @Override
        public double leap_time(double current, double tolerance) {
            double
                t1 = tolerance * particles[this.element()][this.sp] / this.propensity,
                t2 = tolerance * particles[this.element2][this.sp] / this.propensity;
            return Math.min(t1, t2);
            // update here please
        }

        @Override
        public int leap_count(double current, double time) {
            // Diffusion is a first order reaction, governed by the
            // sum of binomial distributions.

            // FIXME: replace two diffusion events by just one and
            //        implement skellam or sum of binomials directly.
            // https://github.com/scipy/scipy/blob/master/scipy/stats/distributions.py#L7861

            int n = particles[this.element()][this.sp];
            return stepper.versatile_ngo("neq diffusion", n, this.propensity * time / n);
        }

        public void addRelations(Collection<? extends NextEvent> coll) {
            for (NextEvent e: coll)
                if (e != this &&
                    (e.element() == this.element() ||
                     e.element() == this.element2) &&
                    ArrayUtil.intersect(e.reactants(), this.sp))
                    this.addDependent(e);
        }

        @Override
        public String toString() {
            return String.format("%s el.%d→%d %s",
                                 getClass().getSimpleName(),
                                 element(), element2, signature);
        }
    }

    public static int[][] stochiometry(int[] ri, int[] rs, int[] pi, int[] ps) {
        ArrayList<Integer>
            si = inst.newArrayList(),
            ss = inst.newArrayList();
        boolean[] pconsidered = new boolean[pi.length];

        for (int i = 0; i < ri.length; i++) {
            int j;
            for (j = 0; j < pi.length; j++)
                if (ri[i] == pi[j]) {
                    pconsidered[j] = true;
                    break;
                }
            if (j == pi.length) {       // product not found
                si.add(ri[i]);
                ss.add(-rs[i]);
            } else if(rs[i] != ps[j]) { // stoichimetry coefficient is nonzero
                assert ri[i] == pi[j];
                si.add(ri[i]);
                ss.add(ps[j] - rs[i]);
            }
        }

        for (int j = 0; j < pi.length; j++)
            if (!pconsidered[j]) {      // reactant not found
                si.add(pi[j]);
                ss.add(ps[j]);
            }

        return new int[][] {ArrayUtil.toArray(si), ArrayUtil.toArray(ss)};
    }

    public class NextReaction extends NextEvent {
        final int[]
            products,
            reactant_stochiometry, product_stochiometry,
            reactant_powers,
            substrates, substrate_stochiometry;
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
            super(element, signature, reactants);
            this.index = index;
            this.products = products;
            this.reactant_stochiometry = reactant_stochiometry;
            this.product_stochiometry = product_stochiometry;
            this.reactant_powers = reactant_powers;

            this.rate = rate;
            this.volume = volume;

            this.propensity = this._propensity();
            this.time = this.propensity > 0 ? this._new_time(0) : Double.POSITIVE_INFINITY;

            int[][] tmp = stochiometry(reactants, reactant_stochiometry,
                                       products, product_stochiometry);
            this.substrates = tmp[0];
            this.substrate_stochiometry = tmp[1];

            log.debug("Created {} rate={} vol={} time={}", this,
                      this.rate, this.volume, this.time);
            assert this.time >= 0;
        }

        @Override
        public double leap_time(double current, double tolerance) {
            int[] X = particles[this.element()];
            double time = Double.POSITIVE_INFINITY;

            for (int i = 0; i < this.substrates.length; i++)
                time = Math.min(time,
                                tolerance * X[this.substrates[i]] /
                                this.propensity / Math.abs(this.substrate_stochiometry[i]));

            log.debug("leap time: subs {}×{}, ɛ={}, pop={} → leap={}",
                      this.substrates, this.substrate_stochiometry,
                      tolerance, X, time);

            return time;
        }

        @Override
        public int leap_count(double current, double time) {
            int[] X = particles[this.element()];

            int n = Integer.MAX_VALUE;
            for (int i = 0; i < this.reactants().length; i++)
                n = Math.min(n, X[this.reactants()[i]] / this.reactant_stochiometry[i]);

            return stepper.versatile_ngo("neq 1st order", n, this.propensity * time / n);

            // FIXME: update for second order reactions
        }


        public void addRelations(Collection<? extends NextEvent> coll) {
            for (NextEvent e: coll)
                if (e != this && e.element() == this.element())
                    for (int r1: e.reactants())
                        for (int i = 0; i < this.substrates.length; i++)
                            if (this.substrates[i] == r1) {
                                this.addDependent(e);

                                assert ArrayUtil.intersect(e.reactants(), this.reactants())
                                    || ArrayUtil.intersect(e.reactants(), this.products): this;

                                break;
                            }
        }

        @Override
        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents,
                     int count) {
            for (int i = 0; i < this.reactants().length; i++)
                if (particles[this.element()][this.reactants()[i]]
                    < this.reactant_stochiometry[i] * count) {
                    log.error("{} prop={} {}→{} pow={} count={}: {}", this, this.propensity,
                              this.reactants(), this.products, this.reactant_powers,
                              count, particles[this.element()]);
                    log.info("reaculated prop={}", this._propensity());
                }

            for (int i = 0; i < this.reactants().length; i++) {
                particles[this.element()][this.reactants()[i]] -=
                    this.reactant_stochiometry[i] * count;
                assert particles[this.element()][this.reactants()[i]] >= 0: this;
            }
            for (int i = 0; i < this.products.length; i++)
                particles[this.element()][this.products[i]] +=
                    this.product_stochiometry[i] * count;
            reactionEvents[this.index] += count;
        }

        @Override
        public double _propensity() {
            double prop = ExactStochasticGridCalc.calculatePropensity(this.reactants(), this.products,
                                                                      this.reactant_stochiometry,
                                                                      this.product_stochiometry,
                                                                      this.reactant_powers,
                                                                      this.rate,
                                                                      this.volume,
                                                                      particles[this.element()]);
            //  log.debug("{}: rate={} vol={} propensity={}",
            //        this, this.rate, this.volume, prop);

            return prop;
        }

        @Override
        public String toString() {
            return String.format("%s el.%d %s",
                                 getClass().getSimpleName(),
                                 element(),
                                 signature);
        }
    }

    public class NextStimulation extends NextEvent {
        final int neighbors;
        final int sp;
        final Stimulation stim;

        /**
         * @param element element to stimulate
         * @param neighbors rate divisor (over how many neighbors the
         *        stimulation rate is split)
         * @param sp the species
         * @param signature description
         * @param stim stimulation parameters
         */
        NextStimulation(int element, int neighbors, int sp, String signature, Stimulation stim) {
            super(element, signature);
            this.sp = sp;
            this.neighbors = neighbors;
            this.stim = stim;

            this.propensity = this._propensity();
            this.time = this._new_time(0);

            log.info("Created {}: t={} [{}]", this, this.time, this.stim);
        }

        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents,
                     int count) {
            particles[this.element()][this.sp] += count;

            stimulationEvents[this.sp] += count;
        }

        /**
         * @param current: starting time
         * @param delta: time interval
         * @param insideWindow: whether to limit the returned value to current
         *        stimulation window.
         */
        private double _continous_delta_to_real_time(double current, double delta,
                                                     boolean insideWindow) {
            final double tc, tp;

            if (Double.isNaN(this.stim.period)) {
                tc = Math.max(current, this.stim.onset);
                tp = this.stim.onset;
            } else {
                double nc = (current - this.stim.onset) / this.stim.period;
                if (nc < 0)
                    nc = 0;

                tp = nc % 1 * this.stim.period;
                assert current > this.stim.onset || tp == 0;

                // current time converted to constant time:
                tc = tp < this.stim.duration ?
                    Math.floor(nc) * this.stim.duration + tp :
                    Math.ceil(nc) * this.stim.duration;
            }

            double t1;
            if (insideWindow)
                t1 = Math.max(tc + delta, tp + this.stim.duration);
            else
                t1 = tc + delta;

            if (!Double.isNaN(this.stim.period)) {
                int n = (int)(t1 / this.stim.duration);
                t1 = this.stim.onset + n * this.stim.period + t1 % this.stim.duration;
            }

            return t1 < this.stim.end ? t1 : Double.POSITIVE_INFINITY;
        }

        @Override
        double _new_time(double current) {
            return _continous_delta_to_real_time(current, super._new_time(0), false);
        }

        @Override
        public double _propensity() {
            return this.stim.rates[this.sp] / this.neighbors;
        }

        @Override
        public double _update_propensity(boolean warn) {
            // does not change
            return this.propensity;
        }

        @Override
        public double leap_time(double current, double tolerance) {
            double cont_leap_time =
                tolerance * particles[this.element()][this.sp] / this.propensity;

            double until = _continous_delta_to_real_time(current, cont_leap_time, true);
            assert until >= current: until;
            assert until < current + this.stim.duration;
            return until - current;
        }

        @Override
        public int leap_count(double current, double time) {
            return stepper.poissonStep(this.propensity * time);
        }

        public void addRelations(Collection<? extends NextEvent> coll) {
            for (NextEvent e: coll)
                if (e != this &&
                    e.element() == this.element() &&
                    ArrayUtil.intersect(e.reactants(), this.sp))
                    this.addDependent(e);
        }

        @Override
        public String toString() {
            return String.format("%s el.%d stim[%s]",
                                 getClass().getSimpleName(),
                                 element(), signature);
        }
    }

    final RandomGenerator random;
    final StepGenerator stepper;

    /**
     * Particle counts: [voxels × species]
     */
    final int[][] particles;
    final double tolerance;

    /**
     * How many times our calculated allowed leap must be longer than
     * normal event waiting time, for us to choose leaping.
     * C.f. SDRun.leap_min_jump.
     */
    final double leap_min_jump;

    final PriorityTree<NextEvent> queue = new PriorityTree<NextEvent>();

    /**
     * Use create() instead, this is public only for testing.
     */
    public NextEventQueue(RandomGenerator random,
                          StepGenerator stepper,
                          int[][] particles,
                          double tolerance,
                          double leap_min_jump) {
        this.random = random != null ? random : new MersenneTwister();
        this.stepper = stepper != null ? stepper :
            new InterpolatingStepGenerator(BINOMIAL, this.random);
        this.particles = particles;

        assert 0 <= tolerance && tolerance <= 1: tolerance;
        this.tolerance = tolerance;

        this.leap_min_jump = leap_min_jump;

        if (leap_min_jump == 0)
            log.info("Leaping disabled");
        else
            log.info("Using {} as leap tolerance, jumping when {} times longer",
                     tolerance, leap_min_jump);
    }

    ArrayList<NextDiffusion> createDiffusions(VolumeGrid grid, ReactionTable rtab) {
        int[][] neighbors = grid.getPerElementNeighbors();
        double[][] couplings = grid.getPerElementCouplingConstants();
        double[] fdiff = rtab.getDiffusionConstants();
        String[] species = rtab.getSpecieIDs();

        ArrayList<NextDiffusion> ans = inst.newArrayList(3 * neighbors.length);

        for (int el = 0; el < neighbors.length; el++)
            for (int j = 0; j < neighbors[el].length; j++) {
                int el2 = neighbors[el][j];
                double cc = couplings[el][j];
                for (int sp = 0; sp < fdiff.length; sp++)
                    ans.add(new NextDiffusion(el, el2, j, sp, species[sp],
                                              fdiff[sp] * cc));
            }

        log.info("Created {} diffusion events", ans.size());

        return ans;
    }

    ArrayList<NextReaction> createReactions(VolumeGrid grid, ReactionTable rtab) {
        double[] volumes = grid.getElementVolumes();
        int n = rtab.getNReaction() * volumes.length;
        int[][]
            RI = rtab.getReactantIndices(),
            PI = rtab.getProductIndices(),
            RS = rtab.getReactantStochiometry(),
            PS = rtab.getProductStochiometry(),
            RP = rtab.getReactantPowers();
        String[] species = rtab.getSpecieIDs();

        ArrayList<NextReaction> ans = inst.newArrayList(RI.length * volumes.length);

        for (int r = 0; r < rtab.getNReaction(); r++) {
            int[] ri = RI[r], pi = PI[r], rs = RS[r], ps = PS[r], rp = RP[r];
            double rate = rtab.getRates()[r];

            for (int el = 0; el < volumes.length; el++) {
                String signature = getReactionSignature(ri, rs, pi, ps, species);
                ans.add(new NextReaction(r, el, ri, pi, rs, ps, rp,
                                         signature,
                                         rate, volumes[el]));
            }
        }

        log.info("Created {} reaction events", ans.size());

        return ans;
    }

    ArrayList<NextStimulation> createStimulations(VolumeGrid grid,
                                                  StimulationTable stimtab,
                                                  int[][] stimtargets) {
        ArrayList<NextStimulation> ans = inst.newArrayList(stimtargets.length * 3);

        for (int i = 0; i < stimtab.getStimulations().size(); i++) {
            Stimulation stim = stimtab.getStimulations().get(i);
            int[] targets = stimtargets[i];

            for (int sp = 0; sp < stim.rates.length; sp++)
                if (stim.rates[sp] > 0) {
                    for (int el: targets)
                        ans.add(new NextStimulation(el, targets.length,
                                                    sp,
                                                    "#" + sp,
                                                    stim));
                }
        }

        log.info("Created {} stimulation events", ans.size());

        return ans;
    }

    public static NextEventQueue create(int[][] particles,
                                        RandomGenerator random,
                                        StepGenerator stepper,
                                        VolumeGrid grid,
                                        ReactionTable rtab,
                                        StimulationTable stimtab,
                                        int[][] stimtargets,
                                        double tolerance,
                                        double leap_min_jump) {
        NextEventQueue obj = new NextEventQueue(random, stepper, particles, tolerance, leap_min_jump);

        ArrayList<NextEvent> e = inst.newArrayList();
        e.addAll(obj.createDiffusions(grid, rtab));
        e.addAll(obj.createReactions(grid, rtab));
        e.addAll(obj.createStimulations(grid, stimtab, stimtargets));
        obj.queue.build(e.toArray(new NextEvent[0]));

        log.info("{} events at the beginning:", obj.queue.nodes.length);
        for (NextEvent ev: obj.queue.nodes)
            log.info("{} → {} prop={} t={}", ev.index(),
                     ev, ev.propensity, ev.time());

        for (NextEvent ev: e) {
            ev.addRelations(e);
            log.debug("dependent {}:{} → {}", ev.index(), ev, ev.dependent);
        }

        log_dependency_edges(e);

        return obj;
    }

    /**
     * Execute an event if the next event is before tstop.
     *
     * @returns Time of event.
     */
    public double advance(double time, double tstop,
                          int[][] reactionEvents,
                          int[][][] diffusionEvents,
                          int[][] stimulationEvents) {
        NextEvent ev = this.queue.first();
        assert ev != null;
        double now = ev.time;

        log.debug("Advanced {}→{},{} with event {}", time, now, tstop, ev);

        if (now > tstop)
            return tstop;

        ev.update(reactionEvents,
                  diffusionEvents,
                  stimulationEvents,
                  now);
        return now;
    }
}
