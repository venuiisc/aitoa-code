package aitoa.algorithms.jssp;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import aitoa.algorithms.EDA;
import aitoa.algorithms.TestMetaheuristic;
import aitoa.examples.jssp.JSSPInstance;
import aitoa.examples.jssp.aco.JSSPACOIndividual;
import aitoa.examples.jssp.aco.JSSPACOMakespanObjectiveFunction;
import aitoa.examples.jssp.aco.JSSPACOSpace;
import aitoa.examples.jssp.aco.JSSPPACOModelAge;
import aitoa.structure.IBlackBoxProcess;
import aitoa.structure.IMetaheuristic;
import aitoa.structure.ISpace;
import aitoa.structure.TestBlackBoxProcessBuilder;

/**
 * Test the {@linkplain aitoa.algorithms.EDA estimation of
 * distribution algorithm} using a
 * {@linkplain aitoa.algorithms.aco.PACOModelAge age-base PACO
 * model} on the JSSP
 */
public class TestPACOAgeOnJSSP extends
    TestMetaheuristic<JSSPACOIndividual, JSSPACOIndividual> {

  /**
   * Get the EDA algorithm
   *
   * @param instance
   *          the instance
   * @return the algorithm
   */
  @SuppressWarnings("static-method")
  protected EDA<JSSPACOIndividual, JSSPACOIndividual>
      getAlgorithm(final JSSPInstance instance) {
    final ThreadLocalRandom rand = ThreadLocalRandom.current();
    final int lambda = 1 + rand.nextInt(64);
    final int mu = 1 + rand.nextInt(lambda);
    final JSSPPACOModelAge model = new JSSPPACOModelAge(instance,
        rand.nextInt(3, 7), rand.nextDouble(0.1d, 0.9d),
        rand.nextDouble(1d, 7d), rand.nextDouble(0.8d, 2d));

    return new EDA<>(mu, lambda, model);
  }

  /** {@inheritDoc} */
  @Override
  protected IMetaheuristic<JSSPACOIndividual, JSSPACOIndividual>
      getInstance() {
    return this.getAlgorithm(new JSSPInstance("demo")); //$NON-NLS-1$
  }

  /**
   * Run a test
   *
   * @param instance
   *          the jssp instance
   * @param maxFEs
   *          the maximum FEs
   * @param maxTime
   *          the maximum time
   */
  protected void runTest(final JSSPInstance instance,
      final long maxFEs, final long maxTime) {
    final ISpace<JSSPACOIndividual> space =
        new JSSPACOSpace(instance);

    final EDA<JSSPACOIndividual, JSSPACOIndividual> algo =
        this.getAlgorithm(instance);

    try (final IBlackBoxProcess<JSSPACOIndividual,
        JSSPACOIndividual> p = new TestBlackBoxProcessBuilder<
            JSSPACOIndividual, JSSPACOIndividual>()//
                .setSearchSpace(space)//
                .setSolutionSpace(space)//
                .setObjectiveFunction(
                    new JSSPACOMakespanObjectiveFunction(
                        instance))//
                .setNullarySearchOperator(algo.model)//
                .setMaxFEs(maxFEs)//
                .setMaxTime(maxTime)//
                .get()) {
      algo.solve(p);
    } catch (final IOException ioe) {
      throw new AssertionError(ioe);
    }
  }

  /**
   * Run a test
   *
   * @param instance
   *          the jssp instance
   */
  protected void runTest(final JSSPInstance instance) {
    this.runTest(instance, 2048L, 2000L);
  }

  /**
   * test the application of the algorithm to the demo instance
   */
  @Test(timeout = 3600000)
  public final void testDemo() {
    this.runTest(new JSSPInstance("demo"), //$NON-NLS-1$
        4096L, 4000L);
  }

  /**
   * test the application of the algorithm to the abz7 instance
   */
  @Test(timeout = 3600000)
  public final void testABZ7() {
    this.runTest(new JSSPInstance("abz7")); //$NON-NLS-1$
  }
}
