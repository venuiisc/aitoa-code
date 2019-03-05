package aitoa.algorithms.jssp;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import aitoa.algorithms.MA;
import aitoa.structure.IMetaheuristic;

/**
 * Test the {@linkplain aitoa.algorithms.MA memetic algorithm} on
 * the JSSP
 */
public class TestMAOnJSSP extends TestMetaheuristicOnJSSP {

  /** {@inheritDoc} */
  @Override
  protected IMetaheuristic getAlgorithm() {
    final Random rand = ThreadLocalRandom.current();
    final int mu = 2 + rand.nextInt(64);
    final int lambda = 1 + rand.nextInt(64);
    return new MA(mu, lambda);
  }
}