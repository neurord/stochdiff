package org.textensor.stochdiff.numeric.grid;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.List;

import org.textensor.stochdiff.numeric.math.RandomGenerator;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable.Stimulation;
import static org.textensor.stochdiff.numeric.chem.ReactionTable.getReactionSignature;
import org.textensor.util.Settings;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;
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

    final static boolean update_times = Settings.getProperty("stochdiff.neq.update_times", true);
    final static boolean only_init = Settings.getProperty("stochdiff.neq.only_init", false);

    final static boolean log_queue = Settings.getProperty("stochdiff.neq.log_queue", false);
    final static boolean log_propensity = Settings.getProperty("stochdiff.neq.log_propensity", false);

    public static class Numbering {
        int count = 0;
        public int get() {
            return count++;
        }
    }

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
            log.info("Sorting {} nodes", nodes.length);
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
            if (log_queue)
                log.debug("{}: moving {} t={} parent={}",
                          prefix, node, node.time(), parent);

            if (parent != null && parent.time() > node.time()) {
                this.swap(parent, node); // original parent first
                this.reposition(prefix, node);
            } else {
                T littlest = this.littlestChild(node);
                if (log_queue)
                    log.debug("littlest child is {} t={}", littlest,
                              littlest != null ? littlest.time() : "-");
                if (littlest != null && node.time() > littlest.time()) {
                    this.swap(node, littlest); // original parent first
                    this.reposition(prefix + "-", node);
                }
            }
        }
    }

    int leaps = 0;
    int leap_extent = 0;
    int normal_waits = 0;

    public abstract class NextEvent implements Node, IGridCalc.Event {
        int index;

        final private int event_number;
        final private int element;
        final String signature;
        final private int[] reactants;

        /**
         * Utility table of coefficients in s_jk definition.
         *
         * s_jk = | sum_i (v_ij n_ik / X_i) |
         *                 ^^^^^^^^^
         * The first index goes over reactions, the second over species.
         * This means that
         *
         * s_jk for dependent[k] is
         *      = | sum_i ( scoeff_ki[k, i] / X_substrate[i] ) |
         */
        protected List<int[]> scoeff_ki = inst.newArrayList();

        /**
         * P_+ and P_- variables - the count of predependent reactions which sometimes increase
         * propensity, and the count of predependent reactions which sometimes decrease propensity
         * of this reaction.
         *
         * count_both is the number of reactions which are both + and -.
         */
        private int plus_count;
        private int minus_count;
        private int count_both;

        /**
         * wait_start: when the event was schedules. This is only used when logging
         * individual events.
         */
        private double wait_start;
        /**
         * time: when the event is scheduled to occur. Absolute time.
         */
        protected double time;
        /**
         * extent: how many "instances" of this event are scheduled to occur. Must
         * be greater than 0.
         */
        protected int extent;
        /**
         * leap: when the event was generated as an "exact" event (false), or "leap"
         * event (true). In the first case, extent must be 1.
         */
        protected boolean leap;

        protected NextEvent reverse;

        /*
         * We calculated the leap size including both forward and reverse propensities.
         * Reverse event is "taken care of".
         */
        protected boolean bidirectional_leap;

        /*
         * reverse_is_leaping: set when this Event is "taken care of" by the reverse Event.
         */
        protected boolean reverse_is_leaping;

        /**
         * propensity: speed with which this event occurs in unchanging conditions
         */
        double propensity;

        public abstract IGridCalc.EventType event_type();
        Happening happening;

        NextEvent(int event_number, int element, String signature, int... reactants) {
            this.event_number = event_number;
            this.element = element;
            this.signature = signature;
            this.reactants = reactants;
        }

        protected void setEvent(int extent, boolean leap, boolean bidirectional,
                                double wait_start, double time) {
            assert !this.reverse_is_leaping;
            this.extent = extent;
            this.leap = leap;
            this.bidirectional_leap = bidirectional;
            this.wait_start = wait_start;
            this.time = time;
        }

        @Override
        public int event_number() {
            return this.event_number;
        }

        public int index() {
            return this.index;
        }

        @Override
        public int element() {
            return this.element;
        }

        @Override
        public String description() {
            return this.toString();
        }

        @Override
        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public double time() {
            return this.time;
        }

        public abstract int[] substrates();
        public abstract int[] substrate_stoichiometry();

        public void addReverse(NextEvent other) {
            assert this.reverse == null;
            assert other.reverse == null;

            this.reverse = other;
            other.reverse = this;
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
        abstract double calcPropensity();

        /**
         * Calculate the time for which <b>this reaction</b> changes the population
         * of <b>products</b> by ɛ. Propensity is not recalculated, so must be brought
         * up-to-date externally.
         *
         * @answer is time relative to @current.
         */
        abstract double leap_time(double current, double tolerance);

        /**
         * Calculate the <b>expected</b> time of a single exact execution.
         * Propensity is not recalculated, so must be brought up-to-date externally.
         *
         * @answer is time relative to @current.
         */
        double exact_time(double current) {
            return 1 / this.propensity;
        }

        /**
         * Calculate the (randomized) extent of the reaction based in the time given.
         */
        abstract int leap_count(double current, double time, boolean bidirectional);

        /**
         * Calculate the <b>putative</b> time of a single exact execution.
         * Propensity is not recalculated, so must be brought up-to-date externally.
         *
         * @answer is absolute time.
         */
        double _new_time(double current) {
            double exp = random.exponential(this.propensity);
            if (this.propensity > 0)
                log.debug("exponential time for prop={} → time={}", this.propensity, exp);
            return current + exp;
        }

        /**
         * Reculculate propensity. Return old.
         */
        int[] old_pop;
        double _update_propensity(boolean warn) {
            double old = this.propensity;
            int[] pop = this.reactantPopulation();
            this.propensity = this.calcPropensity();
            if (warn && this.propensity != 0 && this.propensity == old) {
                boolean higher = false;
                boolean lower = false;
                for (int i = 0; i < pop.length; i++) {
                    if (pop[i] < old_pop[i])
                        lower = true;
                    if (pop[i] > old_pop[i])
                        higher = true;
                }
                log.log(higher && lower ? Level.DEBUG : Level.ERROR,
                        "{}: propensity changed {} → {} (n={} → {}), extent={}",
                        this, old, this.propensity, old_pop, pop, this.extent);
                if (!(higher && lower))
                    throw new RuntimeException();
            } else if (log_propensity) {
                log.debug("particles el.{}: {}", this.element(), particles[this.element()]);
                log.debug("{}: propensity changed {} → {} (n={} → {}), extent={}",
                          this, old, this.propensity, old_pop, pop, this.extent);
            }

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

        void pick_time(double current, double timelimit, double tolerance) {

            final double exact = this.exact_time(current);

            if (adaptive) {
                double leap = this.leap_time(current, tolerance);

                log.debug("deps: {}", this.dependent);
                log.debug("options: wait {}, leap {}", exact, leap);

                if (current + leap > timelimit) {
                    log.debug("Curtailing leap {}→{} to {}", current, current + leap, timelimit);
                    leap = timelimit - current;
                }

                if (leap > exact * leap_min_jump) {
                    assert update_times;

                    /**
                     * We make the leap bidirectional iff the reverse is fast enough
                     * to happen in the chosen period. If not, we keep events separate,
                     * and still use our calculation of leap time, becuase the reverse
                     * reaction is slow enough for this to be valid.
                     */
                    boolean bidirectional = this.reverse != null;

                    int count = this.leap_count(current, leap, bidirectional);

                    /**
                     * We had a reverse event scheduled. The probability that
                     * the generated count includes "that" event is P_cumul(t+leap)-P_cumul(t).
                     * So we add the reverse to event count with the complementary probability.
                     */
                    if (bidirectional &&
                        this.reverse.time >= current && this.reverse.time < current + leap) {

                        double prob = 1 -
                            Math.exp(-this.reverse.propensity * (current - this.reverse.wait_start))
                            * (1 - Math.exp(-this.reverse.propensity * leap));
                        if (prob > random.random())
                            count -= 1;
                    }

                    log.debug("{}: leaping {} {} ({}→{}), extent {}",
                              this,
                              bidirectional ? "bi" : "uni",
                              leap, current, current + leap, count);
                    this.setEvent(count, true, bidirectional, current, current + leap);
                    return;
                }
            }

            double normal =  this._new_time(current);

            log.debug("waiting {} {}→{}", normal - current, current, normal);
            this.setEvent(1, false, false, current, normal);
        }

        void update(int[][] reactionEvents,
                    int[][][] diffusionEvents,
                    int[][] stimulationEvents,
                    double current, double tstop, double timelimit,
                    List<IGridCalc.Happening> events) {

            assert this.extent >= 0: this.extent;

            final boolean changed = this.extent > 0;

            /* As an ugly optimization, this is only created when it will be used. */
            if (events != null)
                events.add(new Happening(this.event_number,
                                         this.leap ? IGridCalc.HappeningKind.LEAP : IGridCalc.HappeningKind.EXACT,
                                         this.extent, current, current - this.wait_start));

            if (changed)
                this.execute(reactionEvents != null ? reactionEvents[this.element()] : null,
                             diffusionEvents != null ? diffusionEvents[this.element()] : null,
                             stimulationEvents != null ? stimulationEvents[this.element()] : null,
                             this.extent);
            if (this.leap) {
                leaps += 1; /* We count a bidirectional leap as one */
                leap_extent += this.extent;
            } else
                normal_waits += 1;

            if (this.bidirectional_leap) {
                assert this.reverse.reverse_is_leaping;
                this.reverse.reverse_is_leaping = false;
                this.bidirectional_leap = false;
            }

            log.debug("Updating {}", this);

            /* In reactions of the type Da→Da+MaI the propensity does not change
             * after execution, but there's nothing to warn about. */
            this._update_propensity(false);

            this.pick_time(current, timelimit, tolerance);
            queue.reposition("update", this);
            if (this.bidirectional_leap) {
                this.reverse.propensity = 0;
                this.reverse.setEvent(1, false, false, current, Double.POSITIVE_INFINITY);
                this.reverse.reverse_is_leaping = true;
                queue.reposition("reverse", this.reverse);
            }

            /* dependent of this must be the same as dependent of reverse reaction
             * so no need to go over both. */
            for (NextEvent dep: this.dependent)
                if (!(dep == this.reverse && this.bidirectional_leap))
                    dep.update_and_reposition(current, changed);
        }

        void update_and_reposition(double current, boolean changed) {
            /* When reverse is leaping, we do not update the time or other
             * fields on this event. We push all updates of time and propensity
             * to the reverse. */

            log.debug("update_and_reposition: {}", this);
            if (this.reverse_is_leaping) {
                log.warn("reverse_is_leaping pushing: {} → {}", this, this.reverse);
                assert this.reverse.bidirectional_leap: this.reverse;
                assert !this.reverse.reverse_is_leaping: this.reverse;

                /* update_and_reposition might have already been called on the
                 * reverse reaction, if both directions are dependent on the
                 * reaction which just fired. So be safe and do not assume
                 * state changed. */
                this.reverse.update_and_reposition(current, false);
            } else {
                double old = this._update_propensity(changed);
                if (update_times && !Double.isInfinite(this.time))
                    this.time = (this.time - current) * old / this.propensity + current;
                else
                    this.time = this._new_time(current);
                queue.reposition("upd.dep", this);
            }
        }

        List<NextEvent>
            dependent = inst.newArrayList(),
            dependon = inst.newArrayList();

        @Override
        public Collection<IGridCalc.Event> dependent() {
            return new ArrayList<IGridCalc.Event>(this.dependent);
        }

        public int[] reactants() {
            return this.reactants;
        }

        protected void addDependent(NextEvent ev, boolean plus, boolean minus) {
            assert !this.dependent.contains(ev): this;

            this.dependent.add(ev);
            ev.dependon.add(this);

            if (plus)
                ev.plus_count ++;
            if (minus)
                ev.minus_count ++;
            if (plus && minus)
                ev.count_both ++;

            this.scoeff_ki.add(scoeff_ki(this.substrates(), this.substrate_stoichiometry(),
                                         ev.substrates(), ev.substrate_stoichiometry()));
            assert this.scoeff_ki.size() == this.dependent.size();
        }

        public abstract void addRelations(Collection<? extends NextEvent> coll);
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
        NextDiffusion(int event_number,
                      int element, int element2, int index2,
                      int sp, String signature, double fdiff) {
            super(event_number, element, signature, sp);
            this.element2 = element2;
            this.index2 = index2;
            this.sp = sp;
            this.fdiff = fdiff;

            this.propensity = this.calcPropensity();
            this.setEvent(1, false, false, 0.0,
                          this.propensity > 0 ? this._new_time(0) : Double.POSITIVE_INFINITY);

            log.debug("Created {}: t={}", this, this.time);
        }

        @Override
        public IGridCalc.EventType event_type() {
            return IGridCalc.EventType.DIFFUSION;
        }

        @Override
        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents,
                     int count) {
            int done = updatePopulation(this.element(), this.sp, -count, this);
            updatePopulation(this.element2, this.sp, -done, this);

            if (diffusionEvents != null)
                diffusionEvents[this.sp][this.index2] += -done;
        }

        @Override
        public double calcPropensity() {
            double ans = this.fdiff * particles[this.element()][this.sp];
            assert ans >= 0: ans;
            return ans;
        }

        @Override
        public int[] substrates() {
            return new int[]{ this.sp };
        }

        @Override
        public int[] substrate_stoichiometry() {
            return new int[]{ -1 };
        }


        /**
         * Calculate leap_time based on the limit on variance and expected extents.
         *
         *
         * As a temporary workaround for problem of updating deterministic solutions
         * for large times, use the following formulas:
         *
         *   y = (N/2 - Xm) p
         *   V = N p/2 (1 - p/2)
         *   p = 1 - e^{-rt}
         *
         * But assume linearity in y:
         *
         *  y' = (N/2 - Xm) rt
         *
         * This gives:
         *  y' ≤ ε Xm
         *  t  ≤ ε/r Xm/(N/2 - Xm)
         *
         *  V ≤ ε^2 Xm^2
         *  p ≤ ε^2 Xm^2 2/N (approx)
         *  t ≤ - log (1-ε^2Xm^2 2/N) / r
         *
         * @returns time step relative to @current.
         */
        @Override
        public double leap_time(double current, double tolerance) {
            final int
                X1 = particles[this.element()][this.sp],
                X2 = particles[this.element2][this.sp],
                Xm = Math.min(X1, X2),
                Xtotal = X1 + X2;

            final double t1 = tolerance * Xm / this.fdiff / (Xtotal/2 - Xm);

            final double arg = 1 - tolerance*tolerance * Xm*Xm * 2 / Xtotal;
            final double ans;
            if (arg > 0) {
                final double t2 = Math.log(arg) / -this.fdiff;
                ans = Math.min(t1, t2);
                log.debug("leap time: min({}, {}, E→{}, V→{}) → {}", X1, X2, t1, t2, ans);
            } else {
                ans = t1;
                log.debug("leap time: min({}, {}, E→{}, V→inf) → {}", X1, X2, t1, ans);
            }
            return ans;
            /*
             static const int[] ONE = new int[]{ 1 };
             SecondOrderSolver solver =
                 SecondOrderSolver.make_equation(this.fdiff, this.fdiff,
                                                 ONE, ONE, new int[]{ X1 },
                                                 ONE, ONE, new int[]{ X2 },
                                                 this.substrate_stoichiometry(), new int[]{X1, X2},
                                                 this.signature);
             */
        }

        @Override
        public int leap_count(double current, double time, boolean bidirectional) {
            /* Diffusion is a first order reaction, governed by the
             * sum of binomial distributions. */
            int X1 = particles[this.element()][this.sp];
            int n1 = stepper.versatile_ngo("neq diffusion", X1, this.fdiff * time);
            if (!bidirectional)
                return n1;
            int X2 = particles[this.element2][this.sp];
            int n2 = stepper.versatile_ngo("neq diffusion", X2, this.fdiff * time);
            return n1 - n2;
        }

        public void addRelations(Collection<? extends NextEvent> coll) {
            for (NextEvent e: coll)
                if (e != this &&
                    (e.element() == this.element() ||
                     e.element() == this.element2) &&
                    ArrayUtil.intersect(e.reactants(), this.sp)) {

                    /* To avoid duplication, diffusion is added to the P numbers
                     * for the direction from lesser to bigger element only.
                     */
                    boolean add = this.element() < this.element2;
                    this.addDependent(e, add, add);
                }
        }

        @Override
        public String toString() {
            return String.format("%s %s el.%d→%d",
                                 getClass().getSimpleName(),
                                 signature, element(), element2);
        }
    }

    /**
     * Calculates a joint array of stoichiometries from reactants @ri, @rs and products @pi, @ps.
     * @returns a pair of arrays: the indices and the stoichiometries.
     */
    public static int[][] stoichiometry(int[] ri, int[] rs, int[] pi, int[] ps) {
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
            } else if(rs[i] != ps[j]) { // stoichiometry coefficient is nonzero
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

    /**
     * Calculate a row of scoeff_ki table, for dependent reaction k described
     * by substrates2 and substrate_stoichiometry2.
     */
    public static int[] scoeff_ki(int[] substrates, int[] substrate_stoichiometry,
                                  int[] substrates2, int[] substrate_stoichiometry2)
    {
        assert substrates.length == substrate_stoichiometry.length;
        assert substrates2.length == substrate_stoichiometry2.length;

        int[] ans = new int[substrates.length];

        for (int i = 0; i < substrates.length; i++)
            /* if we find no match, we leave 0 in the array */
            for (int ii = 0; ii < substrates2.length; ii++)
                if (substrates2[ii] == substrates[i]) {
                    ans[i] = substrate_stoichiometry[i] * substrate_stoichiometry2[ii];
                    break;
                }

        return ans;
    }

    public class NextReaction extends NextEvent {
        final int[]
            products,
            reactant_stoichiometry, product_stoichiometry,
            reactant_powers,
            substrates, substrate_stoichiometry;
        final int index;
        final double rate, volume;

        /**
         * @param index the index of this reaction in reactions array
         * @param element voxel number
         * @param reactants indices of reactants
         * @param products indices of products
         * @param reactant_stoichiometry stoichiometry of reactants
         * @param product_stoichiometry stoichiometry of products
         * @param reactant_powers coefficients of reactants
         * @param signature string to use in logging
         * @param rate rate of reaction
         * @param volume voxel volume
         */
        NextReaction(int event_number,
                     int index, int element, int[] reactants, int[] products,
                     int[] reactant_stoichiometry, int[] product_stoichiometry,
                     int[] reactant_powers, String signature,
                     double rate, double volume) {
            super(event_number, element, signature, reactants);
            this.index = index;
            this.products = products;
            this.reactant_stoichiometry = reactant_stoichiometry;
            this.product_stoichiometry = product_stoichiometry;
            this.reactant_powers = reactant_powers;

            this.rate = rate;
            this.volume = volume;

            int[][] tmp = stoichiometry(reactants, reactant_stoichiometry,
                                       products, product_stoichiometry);
            this.substrates = tmp[0];
            this.substrate_stoichiometry = tmp[1];

            this.propensity = this.calcPropensity();
            this.setEvent(1, false, false, 0.0,
                          this.propensity > 0 ? this._new_time(0) : Double.POSITIVE_INFINITY);

            log.debug("Created {} rate={} vol={} time={}", this,
                      this.rate, this.volume, this.time);
            assert this.time >= 0;
        }

        @Override
        public IGridCalc.EventType event_type() {
            return IGridCalc.EventType.REACTION;
        }

        @Override
        public double leap_time(double current, double tolerance) {
            int[] X = particles[this.element()];
            double time = Double.POSITIVE_INFINITY;

            for (int i = 0; i < this.reactants().length; i++)
                time = Math.min(time,
                                tolerance * X[this.reactants()[i]] /
                                    this.propensity / this.reactant_stoichiometry[i]);
            for (int i = 0; i < this.products.length; i++)
                time = Math.min(time,
                                tolerance * X[this.products[i]] /
                                    this.propensity / this.product_stoichiometry[i]);

            log.debug("{}: leap time: subs {}×{}, ɛ={}, pop={} → leap={}",
                      this,
                      this.substrates, this.substrate_stoichiometry,
                      tolerance, X, time);

            /* Make sure time is NaN or >= 0. */
            assert !(time < 0): time;

            return time;
        }

        @Override
        public int leap_count(double current, double time, boolean bidirectional) {
            int[] X = particles[this.element()];

            int n = Integer.MAX_VALUE;
            for (int i = 0; i < this.reactants().length; i++)
                n = Math.min(n, X[this.reactants()[i]] / this.reactant_stoichiometry[i]);

            return stepper.versatile_ngo("neq 1st order", n, this.propensity * time / n);

            // FIXME: update to bidirectional reactions

            // FIXME: update for second order reactions
        }

        private void maybeAddRelation(NextEvent e) {
            for (int r1: e.reactants())
                for (int i = 0; i < this.substrates.length; i++)
                    if (this.substrates[i] == r1) {
                        boolean minus = ArrayUtil.intersect(e.reactants(), this.reactants());
                        boolean plus = ArrayUtil.intersect(e.reactants(), this.products);
                        assert minus || plus : this;

                        /* To avoid duplication, for reversible reactions,
                         * reaction is added to the P numbers for the direction
                         * from lesser to bigger index only.
                         */
                        boolean add = this.reverse == null ||
                            this.index < this.reverse.index;
                        this.addDependent(e, add && plus, add && minus);

                        return;
                    }
        }

        public void addRelations(Collection<? extends NextEvent> coll) {
            for (NextEvent e: coll)
                if (e != this && e.element() == this.element())
                    this.maybeAddRelation(e);
        }

        @Override
        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents,
                     int count) {
            for (int i = 0; i < this.reactants().length; i++)
                if (particles[this.element()][this.reactants()[i]]
                    < this.reactant_stoichiometry[i] * count) {
                    log.error("{} prop={} {}→{} pow={} extent={}: {}", this, this.propensity,
                              this.reactants(), this.products, this.reactant_powers,
                              count, particles[this.element()]);
                    log.info("reaculated prop={}", this.calcPropensity());
                }

            for (int i = 0; i < this.reactants().length; i++)
                updatePopulation(this.element(), this.reactants()[i],
                                 this.reactant_stoichiometry[i] * -count, this);
            for (int i = 0; i < this.products.length; i++)
                updatePopulation(this.element(), this.products[i],
                                 this.product_stoichiometry[i] * count, this);

            if (reactionEvents != null)
                reactionEvents[this.index] += count;
        }

        @Override
        public double calcPropensity() {
            double ans = AdaptiveGridCalc.calculatePropensity(this.reactants(), this.products,
                                                              this.reactant_stoichiometry,
                                                              this.product_stoichiometry,
                                                              this.reactant_powers,
                                                              this.rate,
                                                              this.volume,
                                                              particles[this.element()]);
            //  log.debug("{}: rate={} vol={} propensity={}",
            //        this, this.rate, this.volume, ans);
            assert ans >= 0: ans;
            return ans;
        }

        @Override
        public int[] substrates() {
            assert this.substrates != null;
            return this.substrates;
        }

        @Override
        public int[] substrate_stoichiometry() {
            assert this.substrate_stoichiometry != null;
            return this.substrate_stoichiometry;
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
        NextStimulation(int event_number,
                        int element, int neighbors, int sp, String signature,
                        Stimulation stim) {
            super(event_number, element, signature);
            this.sp = sp;
            this.neighbors = neighbors;
            this.stim = stim;

            this.propensity = this.calcPropensity();
            this.setEvent(1, false, false, 0.0, this._new_time(0));

            log.info("Created {}: t={} [{}]", this, this.time, this.stim);
        }

        @Override
        public IGridCalc.EventType event_type() {
            return IGridCalc.EventType.STIMULATION;
        }

        void execute(int[] reactionEvents,
                     int[][] diffusionEvents,
                     int[] stimulationEvents,
                     int count) {
            updatePopulation(this.element(), this.sp, count, this);

            if (stimulationEvents != null)
                stimulationEvents[this.sp] += count;
        }

        /**
         * @param current: starting real time
         * @param delta: continous time interval
         * @param insideDuration: if real and continous time delta were different, return
         *               the moment of divergence. Returned value might be negative in
         *               this case.
         */
        private double _continous_delta_to_real_time(double current, double delta,
                                                     boolean insideDuration)
        {
            final double tc;
            double tp;

            if (Double.isNaN(this.stim.period)) {
                tc = this.stim.onset;                         /* beginning of the relevant period,
                                                                 expressed in real time */
                tp = Math.max(current - this.stim.onset, 0);  /* real time since the beggining of
                                                                 the relevant period */
            } else {
                double nc = (current - this.stim.onset) / this.stim.period;
                if (nc < 0)
                    nc = 0;

                tp = nc % 1 * this.stim.period;
                assert current > this.stim.onset || tp == 0;

                if (tp < this.stim.duration)
                    tc = this.stim.onset + Math.floor(nc) * this.stim.period;
                else {
                    tc = this.stim.onset + Math.ceil(nc) * this.stim.period;
                    tp = 0;
                }
            }

            double t1 = tp + delta;

            if (insideDuration && t1 > this.stim.duration)
                return tc + this.stim.duration;

            if (Double.isNaN(this.stim.period))
                t1 += tc;
            else {
                int n = (int)(t1 / this.stim.duration);
                t1 = tc + n * this.stim.period + t1 % this.stim.duration;
            }

            double t2 = t1 < this.stim.end ? t1 : Double.POSITIVE_INFINITY;
            assert insideDuration || t2 + 1e-6 >= current + delta:
                  "t1=" + t1  + " t2=" + t2 + " current=" + current + " delta=" + delta +
                  " current+delta=" + (current+delta);
            /* FIXME: problem with rounding can happen. But we don't want to go negative */
            if (!insideDuration && t2 < current)
                return current;
            else
                return t2;
        }

        @Override
        double exact_time(double current) {
            double cont = super.exact_time(current);
            double real = _continous_delta_to_real_time(current, cont, false);
            // log.debug("exact_time: current={} + {} → {} ({} advance)", current, cont, real, real - current);
            return real - current;
        }

        @Override
        double _new_time(double current) {
            return _continous_delta_to_real_time(current, super._new_time(0), false);
        }

        @Override
        public double calcPropensity() {
            double ans = this.stim.rates[this.sp] / this.neighbors;
            assert ans >= 0: ans;
            return ans;
        }

        @Override
        public int[] substrates() {
            return new int[]{ this.sp };
        }

        @Override
        public int[] substrate_stoichiometry() {
            return new int[]{ 1 };
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
            assert cont_leap_time >= 0;

            double until = _continous_delta_to_real_time(current, cont_leap_time, true);
            log.debug("{}: leap time: {}×{}/{} → {} cont, {} real until {}",
                      this,
                      tolerance, particles[this.element()][this.sp], this.propensity,
                      cont_leap_time, until - current, until);

            /* When we are after the end of the stimulation duration,
             * there might be no "next" time. */
            if (until <= current)
                return 0;
            return until - current;
        }

        @Override
        public int leap_count(double current, double time, boolean bidirectional) {
            /* There should be no reverse reaction, hence no bidirectional leaps */
            assert !bidirectional;
            return stepper.poissonStep(this.propensity * time);
        }

        public void addRelations(Collection<? extends NextEvent> coll) {
            for (NextEvent e: coll)
                if (e != this &&
                    e.element() == this.element() &&
                    ArrayUtil.intersect(e.reactants(), this.sp))

                    /* We know that this only ever adds molecules, so is always in plus group.
                     */
                    this.addDependent(e, true, false);
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
     * When false, exact stochastic simulation is performed.
     */
    boolean adaptive;

    /**
     * How many times our calculated allowed leap must be longer than
     * normal event waiting time, for us to choose leaping.
     * C.f. SDRun.leap_min_jump.
     */
    final double leap_min_jump;

    final PriorityTree<NextEvent> queue = new PriorityTree<NextEvent>();

    public int updatePopulation(int element, int specie, int count, NextEvent event) {
        final int done;
        if (count < 0 && this.particles[element][specie] < -count) {
            log.warn("{}: population would become negative for element {} sp {}: changing {} by {} {}",
                     event, element, specie,
                     this.particles[element][specie], count,
                     this.particles);
            done = -this.particles[element][specie];
            this.particles[element][specie] = 0;
        } else {
            this.particles[element][specie] += count;
            done = count;
        }
        return done;
    }

    /**
     * Use create() instead, this is public only for testing.
     */
    public NextEventQueue(RandomGenerator random,
                          StepGenerator stepper,
                          int[][] particles,
                          boolean adaptive,
                          double tolerance,
                          double leap_min_jump) {
        this.random = random != null ? random : new MersenneTwister();
        this.stepper = stepper != null ? stepper :
            new InterpolatingStepGenerator(BINOMIAL, this.random);
        this.particles = particles;

        assert 0 <= tolerance && tolerance <= 1: tolerance;
        this.tolerance = tolerance;
        this.adaptive = adaptive;
        this.leap_min_jump = leap_min_jump;

        if (this.adaptive)
            log.info("Using {} as leap tolerance, jumping when {} times longer",
                     tolerance, leap_min_jump);
        else
            log.info("Leaping disabled");
    }

    ArrayList<NextDiffusion> createDiffusions(Numbering numbering, VolumeGrid grid, ReactionTable rtab) {
        int[][] neighbors = grid.getPerElementNeighbors();
        double[][] couplings = grid.getPerElementCouplingConstants();
        double[] fdiff = rtab.getDiffusionConstants();
        String[] species = rtab.getSpecies();

        ArrayList<NextDiffusion> ans = inst.newArrayList(5 * neighbors.length);

        int nel = grid.getNElements();
        NextDiffusion[][][] rev = new NextDiffusion[nel][nel][fdiff.length];

        for (int el = 0; el < neighbors.length; el++)
            for (int j = 0; j < neighbors[el].length; j++) {
                int el2 = neighbors[el][j];
                double cc = couplings[el][j];
                if (cc > 0)
                    for (int sp = 0; sp < fdiff.length; sp++)
                        if (fdiff[sp] > 0) {
                            NextDiffusion diff = new NextDiffusion(numbering.get(),
                                                                   el, el2, j, sp, species[sp],
                                                                   fdiff[sp] * cc);

                            ans.add(diff);

                            /* Here we take advantage of the fact that either
                             * the "forward" or "backward" diffusion must be added
                             * earlier. */
                            if (rev[el2][el][sp] != null)
                                diff.addReverse(rev[el2][el][sp]);
                            else
                                rev[el][el2][sp] = diff;
                        }
            }

        log.info("Created {} diffusion events", ans.size());

        return ans;
    }

    ArrayList<NextReaction> createReactions(Numbering numbering, VolumeGrid grid, ReactionTable rtab) {
        double[] volumes = grid.getElementVolumes();
        int n = rtab.getNReaction() * volumes.length;
        int[][]
            RI = rtab.getReactantIndices(),
            PI = rtab.getProductIndices(),
            RS = rtab.getReactantStoichiometry(),
            PS = rtab.getProductStoichiometry(),
            RP = rtab.getReactantPowers();
        int[] reversible_pairs = rtab.getReversiblePairs();

        log.debug("reversible_pairs: {}", reversible_pairs);

        String[] species = rtab.getSpecies();

        ArrayList<NextReaction> ans = inst.newArrayList(RI.length * volumes.length);

        for (int r = 0; r < rtab.getNReaction(); r++) {
            int[] ri = RI[r], pi = PI[r], rs = RS[r], ps = PS[r], rp = RP[r];
            double rate = rtab.getRates()[r];

            for (int el = 0; el < volumes.length; el++) {
                String signature = getReactionSignature(ri, rs, pi, ps, species);
                ans.add(new NextReaction(numbering.get(),
                                         r, el, ri, pi, rs, ps, rp,
                                         signature,
                                         rate, volumes[el]));
            }
        }

        for (int r = 0; r < rtab.getNReaction(); r++)
            if (reversible_pairs[r] >= 0)
                for (int el = 0; el < volumes.length; el++) {
                    NextReaction one = ans.get(r * volumes.length + el);
                    NextReaction two = ans.get(reversible_pairs[r] * volumes.length + el);
                    one.addReverse(two);
                }

        log.info("Created {} reaction events", ans.size());

        return ans;
    }

    ArrayList<NextStimulation> createStimulations(Numbering numbering,
                                                  VolumeGrid grid,
                                                  ReactionTable rtab,
                                                  StimulationTable stimtab,
                                                  int[][] stimtargets) {
        String[] species = rtab.getSpecies();
        ArrayList<NextStimulation> ans = inst.newArrayList(stimtargets.length * 3);

        for (int i = 0; i < stimtab.getStimulations().size(); i++) {
            Stimulation stim = stimtab.getStimulations().get(i);
            int[] targets = stimtargets[i];

            for (int sp = 0; sp < stim.rates.length; sp++)
                if (stim.rates[sp] > 0) {
                    for (int el: targets)
                        ans.add(new NextStimulation(numbering.get(),
                                                    el, targets.length,
                                                    sp,
                                                    species[sp],
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
                                        boolean adaptive,
                                        double tolerance,
                                        double leap_min_jump,
                                        boolean verbose) {
        NextEventQueue obj = new NextEventQueue(random, stepper, particles, adaptive, tolerance, leap_min_jump);

        ArrayList<NextEvent> e = inst.newArrayList();
        Numbering numbering = new Numbering();
        e.addAll(obj.createDiffusions(numbering, grid, rtab));
        e.addAll(obj.createReactions(numbering, grid, rtab));
        e.addAll(obj.createStimulations(numbering, grid, rtab, stimtab, stimtargets));
        obj.queue.build(e.toArray(new NextEvent[0]));

        log.info("Creating dependency graphs");
        for (NextEvent ev: e) {
            ev.addRelations(e);

            /* skip diffusion by default, since it's mostly boring */
            if (verbose && !(ev instanceof NextDiffusion)) {
                int boring = 0;

                log.debug("dependent {}:{}", ev.index(), ev);
                for (int i = 0; i < ev.dependent.size(); i++) {
                    NextEvent dep = ev.dependent.get(i);
                    int[] coeff = ev.scoeff_ki.get(i);
                    int sum = ArrayUtil.abssum(coeff);
                    if (ev.reverse == dep)
                        log.debug("      → {}reverse {}", sum==1?"boring ":"", coeff);
                    else if (sum == 1)
                        boring ++;
                    else
                        log.debug("      → {} {}", dep, coeff);
                }

                if (boring > 0)
                    log.debug("      → + {} boring single-specie first-order deps", boring);
            }
        }

        if (verbose) {
            log.info("{} events at the beginning:", obj.queue.nodes.length);

            int total_plus = 0, total_minus = 0, total_both = 0;
            boolean in_infinity = false;

            for (NextEvent ev: obj.queue.nodes) {
                total_plus += ev.plus_count;
                total_minus += ev.minus_count;
                total_both += ev.count_both;

                if (!in_infinity) {
                    log.info("{} → {} prop={} t={} P₊={} P₋={} (P±={})", ev.index(),
                             ev, ev.propensity, ev.time(),
                             ev.plus_count, ev.minus_count, ev.count_both);

                    if (Double.isInfinite(ev.time()) && ev.index() + 1 < obj.queue.nodes.length) {
                        log.info("{} — {} will happen at infinity",
                                 ev.index() + 1, obj.queue.nodes.length-1);
                        in_infinity = true;
                    }
                }
            }

            log.info("ΣP₊={} ΣP₋={} (ΣP±={})", total_plus, total_minus, total_both);
        }

        log_dependency_edges(e);

        if (only_init)
            System.exit(0);
        try {
            Thread.sleep(1000);
        } catch(InterruptedException exc) {
        }

        return obj;
    }

    /**
     * Execute an event if the next event is before tstop.
     * @param timelimit is the maximum time that leap events are allowed to extend to.
     * Normally this would either be either tstop or the simulation time.
     * @param reactionEvents is an array to store reaction event counts in.
     * If null, events will not be counted.
     * @param diffusionEvents similar, but for diffusion events.
     * @param stimulationEvents similar, but for stimulation events.
     * @param events will be used to store all Hapennings, unless null.
     *
     * @returns Time of soonest event.
     */
    public double advance(double time, double tstop, double timelimit,
                          int[][] reactionEvents,
                          int[][][] diffusionEvents,
                          int[][] stimulationEvents,
                          List<IGridCalc.Happening> events) {
        NextEvent ev = this.queue.first();
        assert ev != null;
        double now = ev.time;

        if (now > tstop) {
            log.debug("Next event is {} time {}, past stop at {}", ev, now, tstop);
            return tstop;
        } else
            log.debug("Advanced {}→{} with event {} {} extent={}",
                      time, now, ev, ev.leap ? "leap" : "", ev.extent);

        ev.update(reactionEvents,
                  diffusionEvents,
                  stimulationEvents,
                  now, tstop, timelimit,
                  events);

        return now;
    }

    public Collection<IGridCalc.Event> getEvents() {
        return new ArrayList<IGridCalc.Event>(Arrays.asList(this.queue.nodes));
    }

    public class Happening implements IGridCalc.Happening {
        final int event_number;
        final IGridCalc.HappeningKind kind;
        final int extent;
        final double time, waited;

        public Happening(int event_number,
                         IGridCalc.HappeningKind kind,
                         int extent,
                         double time,
                         double waited) {
            this.event_number = event_number;
            this.kind = kind;
            this.extent = extent;
            this.time = time;
            this.waited = waited;
        }

        @Override
        public int event_number() {
            return this.event_number;
        }

        @Override
        public IGridCalc.HappeningKind kind() {
            return this.kind;
        }

        @Override
        public int extent() {
            return this.extent;
        }

        @Override
        public double time() {
            return this.time;
        }

        @Override
        public double waited() {
            return this.waited;
        }
    }
}
