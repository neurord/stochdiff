package neurord.numeric.grid;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import neurord.numeric.math.RandomGenerator;
import neurord.numeric.math.MersenneTwister;
import neurord.numeric.chem.ReactionTable;
import neurord.numeric.chem.StimulationTable;
import neurord.numeric.chem.StimulationTable.Stimulation;
import static neurord.numeric.chem.ReactionTable.getReactionSignature;
import neurord.util.Settings;
import neurord.util.ArrayUtil;
import neurord.numeric.morph.VolumeGrid;
import static neurord.numeric.grid.GridCalc.intlog;
import neurord.numeric.stochastic.StepGenerator;
import neurord.numeric.stochastic.InterpolatingStepGenerator;
import static neurord.numeric.BaseCalc.distribution_t.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;

public class NextEventQueue {
    static final Logger log = LogManager.getLogger();

    final static boolean update_times = Settings.getProperty("neurord.neq.update_times", true);
    final static boolean only_init = Settings.getProperty("neurord.neq.only_init", false);
    final static boolean check_updates = Settings.getProperty("neurord.neq.check_updates", false);

    final static boolean log_queue = Settings.getProperty("neurord.neq.log_queue", false);
    final static boolean log_propensity = Settings.getProperty("neurord.neq.log_propensity", false);
    final static boolean log_reposition = Settings.getProperty("neurord.neq.log_reposition", false);

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
                log.info("neurord.neq.update_times is false, will regenerate times");

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

    long leaps = 0;
    long leap_extent = 0;
    long normal_waits = 0;

    /**
     * Utility table of coefficients to calculate propensity change
     * of dependent reaction k when reaction j executes.
     *
     * s_jk = | sum_i (v_ij n_ik / X_i) |
     *                 ^^^^^^^^^
     * The first index goes over reactions, the second over species.
     * This means that
     *
     * s_jk for dependent[k] is
     *      = | sum_i ( scoeff_ki[k, i] / X_substrate[i] ) |
     *
     * This class contains the row scoeff_ki[k].
     */
    static class ScoeffElem {
        final int element;
        final int single_coeff;
        final int single_sub;
        final int[] coeff;
        ScoeffElem(int element, int[] coeff, int[] substrates) {
            this.element = element;
            this.coeff = coeff;

            /* If there's just one coefficient, we can save time,
             * and instead of calculating c = 1/Σ(1/x + 1/y + ...),
             * just do c = x. We also take the absolute, to avoid
             * having to do it later in size1_leap_extent().
             */
            int single_coeff = 0;
            int single_sub = -1;
            for (int i = 0; i < coeff.length; i++)
                if (coeff[i] != 0)
                    if (single_coeff == 0) {
                        single_coeff = Math.abs(coeff[i]);
                        single_sub = substrates[i];
                    } else {
                        /* more than one, we cannot use this optimization */
                        single_coeff = 0;
                        single_sub = -1;
                        break;
                    }
            this.single_coeff = single_coeff;
            this.single_sub = single_sub;
        }

        public boolean sameAs(ScoeffElem other) {
            return this.element == other.element &&
                (Arrays.equals(this.coeff, other.coeff) ||
                 Arrays.equals(this.coeff, ArrayUtil.negate(other.coeff)));
        }

        public String toString(int[] subs, String[] species) {
            assert subs.length == this.coeff.length;

            StringBuilder b = new StringBuilder();
            for (int i = 0; i < subs.length; i++)
                if (this.coeff[i] != 0) {
                    if (b.length() > 0)
                        b.append(" + ");
                    b.append("" + this.coeff[i] + "/" + species[subs[i]]);
                }
            if (this.single_coeff > 0)
                b.append(" 😊");
            b.append(" el." + this.element);
            return b.toString();
        }

        /**
         * Calculate a row of scoeff_ki table, for dependent reaction k described
         * by reactants and reactant_stoichiometry.
         */
        public static int[] scoeff_ki(int[] substrates, int[] substrate_stoichiometry,
                                      int[] reactants, int[] reactant_stoichiometry)
        {
            assert substrates.length == substrate_stoichiometry.length;
            assert reactants.length == reactant_stoichiometry.length;

            int[] ans = new int[substrates.length];

            for (int i = 0; i < substrates.length; i++)
                /* if we find no match, we leave 0 in the array */
                for (int ii = 0; ii < reactants.length; ii++)
                    if (reactants[ii] == substrates[i]) {
                        ans[i] = substrate_stoichiometry[i] * reactant_stoichiometry[ii];
                        break;
                    }

            return ans;
        }

        public static ScoeffElem create(int element,
                                        int[] substrates, int[] substrate_stoichiometry,
                                        int[] reactants, int[] reactant_stoichiometry) {
            return new ScoeffElem(element,
                                  scoeff_ki(substrates, substrate_stoichiometry,
                                            reactants, reactant_stoichiometry),
                                  substrates);
        }
    }

    public abstract class NextEvent implements Node, IGridCalc.Event {
        int index;

        final private int event_number;
        final private int element;
        final String signature;
        final private int[] reactants;
        final private int[] reactant_stoichiometry;

        protected List<ScoeffElem> scoeff_ki = new ArrayList<>();

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
         * time: original wait time. This is only used when logging
         * individual events.
         */
        protected double original_wait;
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

        NextEvent(int event_number, int element, String signature, int[] reactants, int[] reactant_stoichiometry) {
            for (int i: reactant_stoichiometry)
                assert i > 0;

            this.event_number = event_number;
            this.element = element;
            this.signature = signature;
            this.reactants = reactants;
            this.reactant_stoichiometry = reactant_stoichiometry;
        }

        protected void setEvent(int extent, boolean leap, boolean bidirectional,
                                double wait_start, double time) {
            assert !this.reverse_is_leaping;
            this.extent = extent;
            this.leap = leap;
            this.bidirectional_leap = bidirectional;
            this.wait_start = wait_start;
            this.time = time;
            this.original_wait = time - wait_start;
            assert time >= 0: this;
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

        public abstract Map<Integer, int[]> substrates_by_voxel();

        public void addReverse(NextEvent other) {
            assert this.reverse == null;
            assert other.reverse == null;

            this.reverse = other;
            other.reverse = this;
        }

        /**
         * Add and remove particles as appropriate for this event type.
         *
         * @return the extent of performed reaction. (Always positive for
         * forward reactions. May be negative for combined forward and
         * backward reactions.)
         */
        abstract int execute(int[] reactionEvents,
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
        abstract double leap_time(double current);


        /**
         * Any reaction j must modify all dependent propensities k only at most
         * by ɛa_k, including both mean change and standard deviation of the
         * change.
         *
         * This function calculates the extent that would change the propensity of
         * all dependent reactions by 100% (in the linear approximation).
         * This will have to be multiplied by the tolerance to get the allowed
         * leap extent.
         */
        double size1_leap_extent() {
            int[] subs = this.substrates();
            double min_value = Double.POSITIVE_INFINITY;

            /* First we calculate how propensity k depends on the extent
             * of reaction j:
             *     α = da_k/d_y / a_k
             *     Δa_k / a_k < ɛ
             *     α y < ɛ
             *     y < ɛ / α
             *
             * First we calculate α...
             */

            for (ScoeffElem scoeff: this.scoeff_ki)
                if (scoeff.single_coeff > 0) {
                    double val = (double) particles[scoeff.element][scoeff.single_sub] / scoeff.single_coeff;
                    min_value = Math.min(min_value, val);
                } else {
                    double change = 0;
                    for (int n = 0; n < subs.length; n++)
                        change += (double) scoeff.coeff[n] / particles[scoeff.element][subs[n]];

                    change = Math.abs(change);
                    min_value = Math.min(min_value, 1 / change);
                }

            /* ... then the answer is 1 / α */
            return min_value;
        }

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
         * @current is "now" (only relevant for stimulations), and @time
         * a time delta. Iff @bidirectional is true, the leap should encompass
         * the reverse reaction.
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
            this.propensity = this.calcPropensity();

            if (check_updates) {
                int[] pop = this.reactantPopulation();

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
                            "{}: propensity changed {} → {} ({}, n={} → {}), extent={}",
                            this, old, this.propensity,
                            (this.propensity - old) / old,
                            old_pop, pop, this.extent);
                    if (!(higher && lower))
                        throw new RuntimeException();
                } else if (log_propensity)
                    log.debug("{}: propensity changed {} → {} ({}, n={} → {}), extent={}",
                              this, old, this.propensity,
                              (this.propensity - old) / old,
                              old_pop, pop, this.extent);

                this.old_pop = pop;
            }

            return old;
        }

        protected int[] reactantPopulation() {
            return ArrayUtil.pick(particles[this.element()], this.reactants());
        }

        void pick_time(double current, double timelimit) {

            final double exact = this.exact_time(current);

            if (adaptive) {
                double leap = this.leap_time(current);

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

            assert !Double.isNaN(current);
            assert !Double.isNaN(normal);

            log.debug("waiting {} {}→{}", normal - current, current, normal);
            this.setEvent(1, false, false, current, normal);
        }

        void update(int[][] reactionEvents,
                    int[][][] diffusionEvents,
                    int[][] stimulationEvents,
                    double current, double tstop, double timelimit,
                    List<IGridCalc.Happening> events) {

            assert this.bidirectional_leap || this.extent >= 0: this.extent;
            assert !(this.bidirectional_leap && !this.leap);

            boolean changed = this.extent != 0;
            final boolean was_leap = this.leap;
            final int done;

            /* As an ugly optimization, this is only created when it will be used. */
            if (events != null)
                events.add(new Happening(this.event_number,
                                         this.leap ? IGridCalc.HappeningKind.LEAP : IGridCalc.HappeningKind.EXACT,
                                         this.extent, current, current - this.wait_start, this.original_wait));

            if (changed) {
                /* Sometimes we attempt to execute something but the necessary
                 * reactants are already gone. The extent of executed reaction
                 * might be smaller, or even 0. */
                done = this.execute(reactionEvents != null ? reactionEvents[this.element()] : null,
                                    diffusionEvents != null ? diffusionEvents[this.element()] : null,
                                    stimulationEvents != null ? stimulationEvents[this.element()] : null,
                                    this.extent);
                if (done == 0)
                    changed = false;
            } else
                done = 0;

            if (was_leap) {
                leaps += 1; /* We count a bidirectional leap as one */
                leap_extent += Math.abs(done);
            } else
                normal_waits += 1;


            log.debug("Advanced to {} with {} {}extent={}{}",
                      time, this,
                      was_leap ? "leap " : "",
                      done,
                      done == this.extent ? "" : " (planned " + this.extent + ")");

            /* In reactions of the type Da→Da+MaI the propensity does not change
             * after execution, but there's nothing to warn about. */
            this._update_propensity(false);

            if (this.bidirectional_leap) {
                assert this.reverse.reverse_is_leaping;
                this.reverse.reverse_is_leaping = false;
                this.bidirectional_leap = false;

                this.reverse._update_propensity(false);
            }

            this.pick_time(current, timelimit);
            queue.reposition("update", this);
            if (this.bidirectional_leap) {
                this.reverse.setEvent(1, false, false, current, Double.POSITIVE_INFINITY);
                this.reverse.reverse_is_leaping = true;
                queue.reposition("reverse", this.reverse);
            } else if (this.reverse != null)
                this.reverse.update_and_reposition(current, false);

            double max_fraction = 0;
            NextEvent worst = null;

            /* dependent of this must be the same as dependent of reverse reaction
             * so no need to go over both. */
            for (NextEvent dep: this.dependent)
                if (dep != this.reverse) {
                    double fraction = dep.update_and_reposition(current, changed);
                    if (fraction > max_fraction) {
                        max_fraction = fraction;
                        worst = dep;
                    }
                }
            if (was_leap && max_fraction >= 5 * tolerance)
                log.warn("{}, extent {}: max {} change fraction {} for {}",
                         this, done, was_leap ? "leap" : "exact", max_fraction, worst);
        }

        /**
         * Returns the fraction of modification of propensity:
         *      (new - old) / old
         * iff the time was updated based on ratio propensities.
         * Returns 0 if a new time was generated.
         * For a reaction subjugate to the reverse, it returns the ratio
         * for the reverse reaction.
         */
        double update_and_reposition(double current, boolean changed) {
            /* When reverse is leaping, we do not update the time or other
             * fields on this event. We push all updates of time and propensity
             * to the reverse. */

            /* For reactions which have a reverse, update_and_reposition might have
             * already been called on the reverse reaction, if both directions are
             * dependent on the reaction which just fired. Usually the "forward"
             * reaction will be called first, but not always ("forward" is the one
             * which has higher propensity when the leap is queued). So be safe and
             * do not assume propensity changed for those reactions.. */
            if (this.reverse_is_leaping) {
                log.debug("update_and_reposition: {}, doing reverse", this);
                assert this.reverse.bidirectional_leap: this.reverse;
                assert !this.reverse.reverse_is_leaping: this.reverse;

                return this.reverse.update_and_reposition(current, false);
            } else {
                if (log_reposition)
                    log.debug("update_and_reposition: {}", this);
                boolean expect_changed = changed && !this.bidirectional_leap;
                double old = this._update_propensity(expect_changed);
                boolean inf = Double.isInfinite(this.time) || this.propensity == 0;
                final double ans;
                if (update_times && !inf) {
                    this.time = (this.time - current) * old / this.propensity + current;
                    ans = (this.propensity - old) / old;
                } else {
                    this.time = this._new_time(current);
                    if (inf)
                        this.original_wait = this.time - current;
                    ans = 0;
                }
                assert this.time >= 0: this;

                queue.reposition("upd.dep", this);
                return ans;
            }
        }

        List<NextEvent>
            dependent = new ArrayList<>(),
            dependon = new ArrayList<>();

        @Override
        public Collection<IGridCalc.Event> dependent() {
            return new ArrayList<IGridCalc.Event>(this.dependent);
        }

        public int[] reactants() {
            return this.reactants;
        }

        public int[] reactant_stoichiometry() {
            return this.reactant_stoichiometry;
        }

        protected void addDependent(NextEvent ev, String[] species, boolean verbose) {
            assert !this.dependent.contains(ev): this;

            this.dependent.add(ev);
            ev.dependon.add(this);

            if (ev == this.reverse) {
                /* If we leap, we take the reverse with us, so no need to
                 * include the reverse in this calculation. */
                if (verbose)
                    log.debug("      → {}:{} [reverse el.{}]",
                              ev.index(),
                              ev,
                              ev.element());
                return;
            }

            final int[] subs = this.substrates();

            for (Map.Entry<Integer, int[]> entry : this.substrates_by_voxel().entrySet()) {
                int elem = entry.getKey();
                if (elem == ev.element()) {
                    ScoeffElem scoeff = ScoeffElem.create(elem,
                                                          subs, entry.getValue(),
                                                          ev.reactants(), ev.reactant_stoichiometry());
                    boolean same = false;
                    for (ScoeffElem other: this.scoeff_ki)
                        if (other.sameAs(scoeff)) {
                            same = true;
                            break;
                        }
                    if (!same)
                        this.scoeff_ki.add(scoeff);

                    if (verbose) {
                        int sum = ArrayUtil.abssum(scoeff.coeff);
                        String formula = scoeff.toString(subs, species);
                        log.debug("      → {}:{} [{}{}]",
                                  ev.index(), ev, formula,
                                  same ? " duplicate" : "");
                    }
                    return;
                }
            }

            for (Map.Entry<Integer, int[]> entry : this.substrates_by_voxel().entrySet())
                log.info("subs_by_voxel: {} → {}", entry.getKey(), entry.getValue());
            log.error("Dependency not found: {} dep. {}", this, ev);
            throw new RuntimeException("wtf?");
        }

        public abstract void addRelations(Collection<? extends NextEvent> coll, String[] species, boolean verbose);
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
            super(event_number, element, signature,
                  new int[]{ sp }, new int[] { 1 });
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
        int execute(int[] reactionEvents,
                    int[][] diffusionEvents,
                    int[] stimulationEvents,
                    int count) {
            int done = updatePopulation(this.element(), this.sp, -count, this);
            updatePopulation(this.element2, this.sp, -done, this);

            if (diffusionEvents != null)
                diffusionEvents[this.sp][this.index2] += -done;

            return -done;
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

        @Override
        public Map<Integer, int[]> substrates_by_voxel() {
            HashMap<Integer, int[]> map = new HashMap<>();
            map.put(this.element(), this.substrate_stoichiometry());
            map.put(this.element2, new int[]{ +1 });
            return map;
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
         *   p = 1 - e^{-2rt}
         *
         * But assume linearity in y:
         *
         *  y' = (N/2 - Xm) 2rt
         *
         * This gives:
         *  y' ≤ ε Xm
         *  t  ≤ ε/2r Xm/(N/2 - Xm)
         *
         *  V ≤ ε^2 Xm^2
         *  p ≤ ε^2 Xm^2 2/N (approx)
         *  t ≤ - log (1-ε^2Xm^2 2/N) / r
         *
         * @returns time step relative to @current.
         */
        @Override
        public double leap_time(double current) {
            /* This is based on all dependent reactions, except for the reverse one.
             * Both mean extent of the reaction and sdev should be smaller than this
             * limit. */
            final double limit1 = this.size1_leap_extent();
            if (limit1 < 1 / tolerance) {
                /* Do not bother with leaping in that case */
                log.debug("leap time: maximum size1 extent {}, not leaping", limit1);
                return 0;
            }

            final int
                X1 = particles[this.element()][this.sp],
                X2 = particles[this.element2][this.sp],
                Xm = Math.min(X1, X2),
                Xtotal = X1 + X2;

            final double limit = tolerance * Math.min(limit1, Xm);

            final double t1 = limit / this.fdiff / 2 / (Xtotal/2 - Xm);

            final double arg = 1 - limit * limit * 2 / Xtotal;
            final double ans;
            if (arg > 0) {
                final double t2 = Math.log(arg) / -this.fdiff;
                ans = Math.min(t1, t2);
                log.debug("leap time: min({}, {}, limit {}, {}: E→{}, V→{}) → {}",
                          X1, X2, limit1, tolerance * Xm, t1, t2, ans);
            } else {
                ans = t1;
                log.debug("leap time: min({}, {}, limit {}, {}: E→{}, V→inf) → {}",
                          X1, X2, limit1, tolerance * Xm, t1, ans);
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
            int n1 = stepper.versatile_ngo("neq diffusion", X1, time * this.fdiff);
            if (!bidirectional)
                return n1;
            int X2 = particles[this.element2][this.sp];
            double reverse_fdiff = ((NextDiffusion) this.reverse).fdiff;
            int n2 = stepper.versatile_ngo("neq diffusion", X2, time * reverse_fdiff);
            return n1 - n2;
        }

        public void addRelations(Collection<? extends NextEvent> coll, String[] species, boolean verbose) {
            for (NextEvent e: coll)
                if (e != this &&
                    (e.element() == this.element() ||
                     e.element() == this.element2) &&
                    ArrayUtil.intersect(e.reactants(), this.sp))

                    this.addDependent(e, species, verbose);
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
            si = new ArrayList<>(),
            ss = new ArrayList<>();
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

    public class NextReaction extends NextEvent {
        final int[]
            products,
            product_stoichiometry,
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
            super(event_number, element, signature,
                  reactants, reactant_stoichiometry);
            this.index = index;
            this.products = products;
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

        protected int[] productPopulation() {
            return ArrayUtil.pick(particles[this.element()], this.products);
        }

        /**
         * Make sure that the propensity of *this* reaction does not change
         * too much. Based on the equation:
         *   da/dX_i / a = n_i / X_i
         * @returns the leap length that would change propensity by 1
         *          in the linear approximation
         *
         * a = r A^nA B B^nB ...
         * Δa = a y nA / A + a y nB / B + ...
         * Δa/a = (nA/A + nB/B + ...) y = 1
         * y = 1 / (nA/A + nB/B + ...)
         */
        protected double self_leap_limit(int[] X) {
            final int[] reactants = this.reactants();
            if (reactants.length == 0)
                return Double.POSITIVE_INFINITY;
            else if (reactants.length == 1)
                return (double) X[reactants[0]] / (this.reactant_powers[0] * this.reactant_stoichiometry()[0]);
            else {
                double mult = 0;
                for (int i = 0; i < reactants.length; i++)
                    mult += (double) this.reactant_powers[i] * this.reactant_stoichiometry()[0] / X[reactants[i]];
                return 1 / mult;
            }
        }

        @Override
        public double leap_time(double current) {
            /* This is based on all dependent reactions, except for the reverse one.
             * Both mean extent of the reaction and sdev should be smaller than this
             * limit.
             *
             * The result is the same for the forward and reverse reactions
             * (stoichiometry is the same except for the sign), so this needs to be
             * calculated only once.
             */
            final double limit1 = this.size1_leap_extent();
            if (limit1 < 1 / tolerance) {
                /* Do not bother with leaping in that case */
                log.debug("leap time: maximum size1 extent {}, not leaping", limit1);
                return 0;
            }

            int[] X = particles[this.element()];
            final double limit2 = this.self_leap_limit(X);
            double time = limit2 / this.propensity;
            double limit3 = -1;
            final double effective_propensity;

            if (this.reverse == null)
                effective_propensity = this.propensity;
            else {
                limit3 = ((NextReaction) this.reverse).self_leap_limit(X);
                time = Math.min(time, limit3 / this.reverse.propensity);
                effective_propensity = Math.abs(this.propensity - this.reverse.propensity);
            }

            /* The result is the minimum of the three limits:
               - tolerance * limit1 / effective_propensity:
                     the dependent reactions, which only care about the effective rate
               - tolerance * limit2 / this.propensity:
                     the forward reaction, which cares about the forward rate
               - tolerance * limit3 / this.reverse.propensity:
                     the reverse reaction, which cares about the reverse rate
            */
            time = tolerance * Math.min(limit1 / effective_propensity, time);

            log.debug("{}: leap time: subs {}×{}, ɛ={}, pop.{}→{} → limit {},{},{} → leap={}",
                      this,
                      this.substrates, this.substrate_stoichiometry,
                      tolerance, this.reactantPopulation(), this.productPopulation(),
                      limit1, limit2, limit3 == -1 ? "-" : limit3,
                      time);

            /* Make sure time is NaN or >= 0. */
            assert !(time < 0): time;

            return time;
        }

        private int leap_count_uni(int[] X, double time) {
            int n = Integer.MAX_VALUE;
            for (int i = 0; i < this.reactants().length; i++)
                n = Math.min(n, X[this.reactants()[i]] / this.reactant_stoichiometry()[i]);

            return stepper.versatile_ngo("neq 1st order", n, this.propensity * time / n);
        }

        @Override
        public int leap_count(double current, double time, boolean bidirectional) {
            int[] X = particles[this.element()];
            int n1 = this.leap_count_uni(X, time);
            if (!bidirectional)
                return n1;
            assert this.reverse != null;
            assert this.element() == this.reverse.element();
            int n2 = ((NextReaction) this.reverse).leap_count_uni(X, time);
            return n1 - n2;

            // FIXME: update variance for second order reactions
        }

        private void maybeAddRelation(NextEvent e, String[] species, boolean verbose) {
            for (int r1: e.reactants())
                for (int i = 0; i < this.substrates.length; i++)
                    if (this.substrates[i] == r1) {
                        this.addDependent(e, species, verbose);

                        return;
                    }
        }

        public void addRelations(Collection<? extends NextEvent> coll, String[] species, boolean verbose) {
            for (NextEvent e: coll)
                if (e != this && e.element() == this.element())
                    this.maybeAddRelation(e, species, verbose);
        }

        @Override
        int execute(int[] reactionEvents,
                    int[][] diffusionEvents,
                    int[] stimulationEvents,
                    int count) {
            for (int i = 0; i < this.reactants().length; i++)
                if (particles[this.element()][this.reactants()[i]] < this.reactant_stoichiometry()[i] * count) {

                    int oldcount = count;
                    count = particles[this.element()][this.reactants()[i]] / this.reactant_stoichiometry()[i];
                    log.warn("{}: population would go below zero with prop={} reactants {}×{} extent={} (using {})",
                             this, this.propensity,
                             this.reactantPopulation(), this.reactant_powers,
                             oldcount, count);
                }

            for (int i = 0; i < this.reactants().length; i++)
                updatePopulation(this.element(), this.reactants()[i],
                                 this.reactant_stoichiometry()[i] * -count, this);
            for (int i = 0; i < this.products.length; i++)
                updatePopulation(this.element(), this.products[i],
                                 this.product_stoichiometry[i] * count, this);

            if (reactionEvents != null)
                reactionEvents[this.index] += count;
            return count;
        }

        @Override
        public double calcPropensity() {
            double ans = AdaptiveGridCalc.calculatePropensity(this.reactants(), this.products,
                                                              this.reactant_stoichiometry(),
                                                              this.product_stoichiometry,
                                                              this.reactant_powers,
                                                              this.rate,
                                                              this.volume,
                                                              particles[this.element()]);
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
        public Map<Integer, int[]> substrates_by_voxel() {
            HashMap<Integer, int[]> map = new HashMap<>();
            map.put(this.element(), this.substrate_stoichiometry());
            return map;
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
            super(event_number, element, signature,
                  new int[]{}, new int[]{});
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

        int execute(int[] reactionEvents,
                    int[][] diffusionEvents,
                    int[] stimulationEvents,
                    int count) {
            updatePopulation(this.element(), this.sp, count, this);

            if (stimulationEvents != null)
                stimulationEvents[this.sp] += count;
            return count;
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
            return real - current;
        }

        @Override
        double _new_time(double current) {
            final double time;
            switch (this.stim.distribution) {
            case POISSON:
                time = super._new_time(0);
                break;
            case EXACT:
                /* One event every in every 1/propensity interval */
                time = 1 / this.propensity;
                break;
            default:
                throw new RuntimeException("not implemented");
            }

            return _continous_delta_to_real_time(current, time, false);
        }

        @Override
        public double calcPropensity() {
            assert this.sp == this.stim.species;
            double ans = this.stim.rate / this.neighbors;
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
        public Map<Integer, int[]> substrates_by_voxel() {
            HashMap<Integer, int[]> map = new HashMap<>();
            map.put(this.element(), this.substrate_stoichiometry());
            return map;
        }

        @Override
        public double _update_propensity(boolean warn) {
            // does not change
            return this.propensity;
        }

        @Override
        public double leap_time(double current) {
            /* This is based on the dependent reactions. Both mean extent of
             * the stimulation and sdev should be smaller than this limit. */
            final double limit1 = this.size1_leap_extent();
            if (limit1 < 1 / tolerance) {
                /* Do not bother with leaping in that case */
                log.debug("leap time: maximum size1 extent {}, not leaping", limit1);
                return 0;
            }

            final int limit2 = particles[this.element()][this.sp];
            double cont_leap_time = tolerance * Math.min(limit1, limit2) / this.propensity;
            assert !(cont_leap_time < 0);

            double until = _continous_delta_to_real_time(current, cont_leap_time, true);
            log.debug("{}: leap time: {}×min({}, {})/{} → {} cont, {} real until {}",
                      this,
                      tolerance, limit1, limit2, this.propensity,
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
            switch (this.stim.distribution) {
            case POISSON:
                return random.poisson(this.propensity * time);
            case EXACT:
                return random.round(this.propensity * time);
            default:
                throw new RuntimeException("not implemented");
            }
        }

        public void addRelations(Collection<? extends NextEvent> coll, String[] species, boolean verbose) {
            for (NextEvent e: coll)
                if (e != this &&
                    e.element() == this.element() &&
                    ArrayUtil.intersect(e.reactants(), this.sp))

                    this.addDependent(e, species, verbose);
        }

        @Override
        public String toString() {
            return String.format("%s el.%d %s",
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
            log.debug("{}: population would become negative for element {} sp {}: changing {} by {}",
                      event, element, specie,
                      this.particles[element][specie], count);
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

        if (log_propensity && !check_updates)
            log.warn("neurord.neq.log_propensity has no effect without neurord.neq.check_updates");
    }

    ArrayList<NextDiffusion> createDiffusions(Numbering numbering, VolumeGrid grid, ReactionTable rtab) {
        double[] volumes = grid.getElementVolumes();
        int[][] neighbors = grid.getPerElementNeighbors();
        double[][] couplings = grid.getPerElementCouplingConstants();
        double[] fdiff = rtab.getDiffusionConstants();
        String[] species = rtab.getSpecies();

        ArrayList<NextDiffusion> ans = new ArrayList<>(5 * neighbors.length);

        int nel = grid.size();
        NextDiffusion[][][] rev = new NextDiffusion[nel][nel][fdiff.length];

        for (int el = 0; el < neighbors.length; el++)
            for (int j = 0; j < neighbors[el].length; j++) {
                int el2 = neighbors[el][j];
                double cc = couplings[el][j];
                if (cc > 0)
                    for (int sp = 0; sp < fdiff.length; sp++)
                        if (fdiff[sp] > 0) {
                            // probability is dt * K_diff * contact_area /
                            // (center_to_center_distance * source_volume)
                            NextDiffusion diff = new NextDiffusion(numbering.get(),
                                                                   el, el2, j, sp, species[sp],
                                                                   fdiff[sp] * cc / volumes[el]);

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

        ArrayList<NextReaction> ans = new ArrayList<>(RI.length * volumes.length);

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
        ArrayList<NextStimulation> ans = new ArrayList<>(stimtargets.length * 3);

        for (int i = 0; i < stimtab.getStimulations().size(); i++) {
            Stimulation stim = stimtab.getStimulations().get(i);
            int[] targets = stimtargets[i];

            for (int el: targets)
                // FIXME: check how the splitting between targets works
                ans.add(new NextStimulation(numbering.get(),
                                            el, targets.length,
                                            stim.species,
                                            species[stim.species],
                                            stim));
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

        ArrayList<NextEvent> e = new ArrayList<>();
        Numbering numbering = new Numbering();
        e.addAll(obj.createDiffusions(numbering, grid, rtab));
        e.addAll(obj.createReactions(numbering, grid, rtab));
        e.addAll(obj.createStimulations(numbering, grid, rtab, stimtab, stimtargets));
        obj.queue.build(e.toArray(new NextEvent[0]));

        log.info("Creating dependency graph");
        for (NextEvent ev: e) {
            if (verbose)
                log.debug("{}:{}", ev.index(), ev);
            ev.addRelations(e, rtab.getSpecies(), verbose);
        }

        if (verbose) {
            log.info("{} events at the beginning:", obj.queue.nodes.length);

            for (NextEvent ev: obj.queue.nodes) {
                log.info("{} → {} prop={} t={}", ev.index(),
                         ev, ev.propensity, ev.time());

                if (Double.isInfinite(ev.time()) && ev.index() + 1 < obj.queue.nodes.length) {
                    log.info("{} — {} will happen at infinity",
                             ev.index() + 1, obj.queue.nodes.length-1);
                    break;
                }
            }
        }

        log_dependency_edges(e);

        if (only_init)
            System.exit(0);

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
        assert now >= time: ev;

        if (now > tstop) {
            log.debug("Next event is {} time {}, past stop at {}", ev, now, tstop);
            return tstop;
        }

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
        final double time, waited, original_wait;

        public Happening(int event_number,
                         IGridCalc.HappeningKind kind,
                         int extent,
                         double time,
                         double waited,
                         double original_wait) {
            this.event_number = event_number;
            this.kind = kind;
            this.extent = extent;
            this.time = time;
            this.waited = waited;
            this.original_wait = original_wait;
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

        @Override
        public double original_wait() {
            return this.original_wait;
        }
    }
}
