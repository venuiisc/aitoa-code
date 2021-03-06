package aitoa.algorithms;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import aitoa.structure.IBlackBoxProcess;
import aitoa.structure.IModel;
import aitoa.structure.INullarySearchOperator;
import aitoa.structure.ISpace;
import aitoa.structure.LogFormat;
import aitoa.structure.Metaheuristic0;
import aitoa.utils.Experiment;

/**
 * An {@linkplain aitoa.algorithms.EDA estimation of distribution
 * algorithm} applying a fitness assignment process.
 *
 * @param <X>
 *          the search space
 * @param <Y>
 *          the solution space
 */
public final class EDAWithFitness<X, Y>
    extends Metaheuristic0<X, Y> {

  /** the number of solution to be selected */
  public final int mu;
  /** the number of new points per generation */
  public final int lambda;
  /** the model */
  public final IModel<X> model;
  /** the fitness assignment process */
  public final FitnessAssignmentProcess<? super X> fitness;

  /**
   * Create a new instance of the estimation of distribution
   *
   * @param pNullary
   *          the nullary search operator.
   * @param pMu
   *          the number of solution to be selected
   * @param pLambda
   *          the number of new points per generation
   * @param pModel
   *          the model
   * @param pFitness
   *          the fitness assignment process
   */
  public EDAWithFitness(final INullarySearchOperator<X> pNullary,
      final int pMu, final int pLambda, final IModel<X> pModel,
      final FitnessAssignmentProcess<? super X> pFitness) {
    super(pNullary);
    if ((pLambda < 1) || (pLambda > 100_000_000)) {
      throw new IllegalArgumentException(
          "Invalid lambda: " + pLambda); //$NON-NLS-1$
    }
    this.lambda = pLambda;

    if ((pMu < 1) || (pMu > this.lambda)) {
      throw new IllegalArgumentException("Invalid mu: " + pMu //$NON-NLS-1$
          + " must be in 1..lambda and lambda=" //$NON-NLS-1$
          + this.lambda);
    }
    this.mu = pMu;

    this.model = Objects.requireNonNull(pModel);
    this.fitness = Objects.requireNonNull(pFitness);
  }

  /** {@inheritDoc} */
  @Override
  public void printSetup(final Writer output)
      throws IOException {
    output.write(LogFormat.mapEntry(//
        LogFormat.SETUP_BASE_ALGORITHM, "eda")); //$NON-NLS-1$
    output.write(System.lineSeparator());
    super.printSetup(output);
    output.write(LogFormat.mapEntry("mu", this.mu));///$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("lambda", this.lambda));//$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("model", this.model));//$NON-NLS-1$
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("fitness", //$NON-NLS-1$
        this.fitness));
    output.write(System.lineSeparator());
    output.write(LogFormat.mapEntry("clearing", false));//$NON-NLS-1$
    output.write(System.lineSeparator());
    if ((this.model != this.nullary)) {
      this.model.printSetup(output);
    }
    if ((this.fitness != this.nullary)
        && (this.fitness != this.model)) {
      this.fitness.printSetup(output);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return Experiment.nameFromObjectsMerge("eda", //$NON-NLS-1$
        this.model, this.fitness,
        String.valueOf(this.mu) + '+' + this.lambda);
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  public void solve(final IBlackBoxProcess<X, Y> process) {
// create local variables
    final Random random = process.getRandom();
    final ISpace<X> searchSpace = process.getSearchSpace();
    final IModel<X> M = this.model;

    final FitnessRecord<X>[] P = new FitnessRecord[this.lambda];
    this.fitness.initialize();

// end relevant
    restart: while (!process.shouldTerminate()) {
// start relevant
// local variable initialization omitted for brevity
      M.initialize(); // initialize to uniform distribution
// first generation: fill population with random solutions
      for (int i = P.length; (--i) >= 0;) {
        final X x = searchSpace.create();
        this.nullary.apply(x, random);
        P[i] = new FitnessRecord<>(x, process.evaluate(x));
        if (process.shouldTerminate()) { // we return
          return; // best solution is stored in process
        }
      }

      for (;;) { // each iteration: update model, sample model
// end relevant
        if (this.mu < M.minimumSamplesNeededForUpdate()) {
          continue restart;
        }
// start relevant
        this.fitness.assignFitness(P);
        Arrays.sort(P, FitnessRecord.BY_FITNESS);
// update model with mu<lambda best solutions
        M.update(IModel.use(P, 0, this.mu));

// sample new population
        for (final FitnessRecord<X> dest : P) {
          M.apply(dest.x, random); // create new solution
          dest.quality = process.evaluate(dest.x);
          if (process.shouldTerminate()) { // we return
            return; // best solution is stored in process
          }
        } // the end of the solution generation
      } // the end of the main loop
    }
// end relevant
  }
// start relevant
}
