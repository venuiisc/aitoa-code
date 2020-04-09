package aitoa.algorithms;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import aitoa.structure.IBinarySearchOperator;
import aitoa.structure.IBlackBoxProcess;
import aitoa.structure.IMetaheuristic;
import aitoa.structure.INullarySearchOperator;
import aitoa.structure.ISpace;
import aitoa.structure.IUnarySearchOperator;
import aitoa.structure.LogFormat;
import aitoa.utils.RandomUtils;

/**
 * A {@linkplain aitoa.algorithms.MA memetic algorithm} is a
 * combination of a {@linkplain aitoa.algorithms.EA evolutionary
 * algorithm} with a local search. Our type of memetic algorithm
 * always applies the binary operator to find new points in the
 * search space and then refines them with a
 * {@linkplain aitoa.algorithms.HillClimber2 first-improvement
 * local search} based on a unary operator. This MA with clearing
 * here also, well, clears the population from candidate
 * solutions with identical objective values before the
 * reproduction step.
 * <p>
 * All candidate solutions represented in the population will
 * always be local optima before entering the binary operators.
 * If these operators work well, they may jump close to other
 * local optima. The clearing ensures that all "parents" from
 * which new points in the search space are derived have a
 * different solution quality. This, in turn, implies that they
 * are different candidate solutions. These, in turn, must be the
 * result of representation mappings applied to different points
 * in the search space.
 * <p>
 * Please notice that the direct, simple comparisons of the
 * objective values applied here in form of
 * {@code ind.quality > P[u - 1].quality} only make sense in
 * combinatorial problems where these values are integers. In
 * numerical problems, we could, e.g., base the comparisons on
 * some similarity thresholds.
 *
 * @param <X>
 *          the search space
 * @param <Y>
 *          the solution space
 */
// start relevant
public final class MAWithClearing<X, Y>
    implements IMetaheuristic<X, Y> {
// end relevant

  /** the number of selected parents */
  public final int mu;
  /** the number of offsprings per generation */
  public final int lambda;
  /** the maximum number of local search steps */
  public final int maxLSSteps;

  /**
   * Create a new instance of the evolutionary algorithm
   *
   * @param _mu
   *          the number of parents to be selected
   * @param _lambda
   *          the number of offspring to be created
   * @param _maxLSSteps
   *          the maximum number of local search steps
   */
  public MAWithClearing(final int _mu, final int _lambda,
      final int _maxLSSteps) {
    super();
    if ((_mu <= 1) || (_mu > 1_000_000)) {
      throw new IllegalArgumentException("Invalid mu: " + _mu); //$NON-NLS-1$
    }
    this.mu = _mu;
    if ((_lambda < 1) || (_lambda > 1_000_000)) {
      throw new IllegalArgumentException(
          "Invalid lambda: " + _lambda); //$NON-NLS-1$
    }
    this.lambda = _lambda;
    if (_maxLSSteps <= 0) {
      throw new IllegalArgumentException(
          "Invalid number of maximum local search steps: " //$NON-NLS-1$
              + _maxLSSteps);
    }
    this.maxLSSteps = _maxLSSteps;
  }

  /** {@inheritDoc} */
  @Override
  public final void printSetup(final Writer output)
      throws IOException {
    output.write(LogFormat.mapEntry("base_algorithm", //$NON-NLS-1$
        "ma")); //$NON-NLS-1$
    output.write(System.lineSeparator());
    IMetaheuristic.super.printSetup(output);
    output.write(LogFormat.mapEntry("mu", this.mu));///$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("lambda", this.lambda));//$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("cr", 1d));//$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("clearing", true)); //$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("restarts", false)); //$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("maxLSSteps", //$NON-NLS-1$
        this.maxLSSteps));
    output.write(System.lineSeparator());
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    final String s = ((("mac_" + //$NON-NLS-1$
        this.mu) + '+') + this.lambda);
    if (this.maxLSSteps >= Integer.MAX_VALUE) {
      return s;
    }
    return (s + '_') + this.maxLSSteps;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
// start relevant
  public final void solve(final IBlackBoxProcess<X, Y> process) {
// omitted: initialize local variables random, searchSpace,
// nullary, unary, binary, and arrays P and P2 of length
// mu+lambda, and array T to null
// end relevant
// create local variables
    final Random random = process.getRandom();
    final ISpace<X> searchSpace = process.getSearchSpace();
    final INullarySearchOperator<X> nullary =
        process.getNullarySearchOperator();
    final IUnarySearchOperator<X> unary =
        process.getUnarySearchOperator();
    final IBinarySearchOperator<X> binary =
        process.getBinarySearchOperator();
    final X temp = searchSpace.create();
    Individual<X>[] T = null,
        P = new Individual[this.mu + this.lambda],
        P2 = new Individual[P.length];
    int p2;
    boolean improved = false;
// start relevant
    restart: while (!process.shouldTerminate()) {
// first generation: fill population with random individuals
      for (int i = P.length; (--i) >= 0;) {
        final X x = searchSpace.create();
        nullary.apply(x, random);
        P[i] = new Individual<>(x, process.evaluate(x));
// end relevant
        if (process.shouldTerminate()) {
          return;
        }
// start relevant
      }
      int localSearchStart = 0; // at first, apply ls to all

      while (!process.shouldTerminate()) { // main loop
        for (int i = P.length; (--i) >= localSearchStart;) {
          final Individual<X> ind = P[i];
          int steps = this.maxLSSteps;
          do { // local search in style of HillClimber2
            improved = unary.enumerate(random, ind.x, temp, //
                (point) -> {
                  final double newQuality =
                      process.evaluate(point);
                  if (newQuality < ind.quality) { // better?
                    ind.quality = newQuality; // store quality
                    searchSpace.copy(point, ind.x); // store
                    return true; // exit to next loop
                  } // if we get here, point is not better
                  return process.shouldTerminate();
                }); // repeat until no improvement or time up
            if (process.shouldTerminate()) { // we return
              return; // best solution is stored in process
            }
          } while (improved && ((--steps) > 0));
        } // end of 1 ls iteration: we have refined 1 solution

        RandomUtils.shuffle(random, P, 0, P.length); // make fair
        Arrays.sort(P); // best records at front
// we now want to keep only the solutions with unique fitness
        int u = 0, done = 0, end = P.length;
        T = P; // since array P is sorted, so we can do this by
        P = P2; // processing it from begin to end and copying
        P2 = T; // these individuals to the start of P
// we switch the two arrays here so the rest is the same as EA
        makeUnique: for (final Individual<X> r : P2) {
          ++done;
          if ((u <= 0) || (r.quality > P[u - 1].quality)) {
            P[u] = r;
            if ((++u) >= this.mu) { // we are done and can
              System.arraycopy(P2, done, P, u, P.length - done);
              break makeUnique; // directly, they do not need to
            } // be unique, as they will be overwritten anyway
          } else { // ind has an already-seen quality, so we copy
            P[--end] = r; // it to the end of the array, where
          } // it will eventually be overwritten
        }
        if (u <= 1) { // 1 <= u <= mu unique solutions
          continue restart; // if u==1, restart, because
        } // then recombination makes no sense
// now we have 2 <= u <= mu unique solutions
// shuffle the first unique solutions to ensure fairness
        RandomUtils.shuffle(random, P, 0, u);
        int p1 = -1; // index to iterate over first parent

// override the worse (mu+lambda-u) solutions
        for (int index = P.length; (--index) >= u;) {
          if (process.shouldTerminate()) { // we return
            return; // best solution is stored in process
          }
          final Individual<X> dest = P[index];
          p1 = (p1 + 1) % u;
          final Individual<X> sel = P[p1];
          // to hold index of second selected record
          do { // find a second, different record
            p2 = random.nextInt(u);
          } while (p2 == p1);
// perform recombination and compute quality
          binary.apply(sel.x, P[p2].x, dest.x, random);
          dest.quality = process.evaluate(dest.x);
        } // the end of the offspring generation
        localSearchStart = this.mu; // ls only for lambda new
      } // the end of the main loop
    } // end of the restart loop
  }
// end relevant

  /**
   * the individual record: hold one point in search space and
   * its quality
   *
   * @param <X>
   *          the data structure of the search space
   */
  private static final class Individual<X>
      implements Comparable<Individual<X>> {
    /** the point in the search space */
    final X x;
    /** the quality */
    double quality;

    /**
     * create the individual record
     *
     * @param _x
     *          the point in the search space
     * @param _q
     *          the quality
     */
    Individual(final X _x, final double _q) {
      super();
      this.x = Objects.requireNonNull(_x);
      this.quality = _q;
    }

    /**
     * compare two individuals: the one with smaller quality is
     * better.
     *
     * @return -1 if this record is better than {@code o}, 1 if
     *         it is worse, 0 otherwise
     */
    @Override
    public final int compareTo(final Individual<X> o) {
      return Double.compare(this.quality, o.quality);
    }
  }
// start relevant
}
// end relevant