package aitoa.structure;

import java.util.Random;

/**
 * This interface encapsulates a nullary search operator, which
 * can sample one new point in the search space without any other
 * information.
 *
 * @param <X>
 *          the search space
 */
@FunctionalInterface
// start relevant
public interface INullarySearchOperator<X>
    extends ISetupPrintable {

  /**
   * Apply the search operator to sample a new point in the
   * search space.
   *
   * @param dest
   *          the destination object to be overwritten with the
   *          newly sampled point
   * @param random
   *          a random number generator
   */
  void apply(X dest, Random random);
}
// end relevant
