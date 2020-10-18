package aitoa.algorithms;

import aitoa.structure.ISetupPrintable;

/**
 * The abstract base class for fitness assignment processes
 *
 * @param <X>
 *          the search space
 */
public abstract class FitnessAssignmentProcess<X>
    implements ISetupPrintable {

  /**
   * Assign the fitness a set of individuals. This process will
   * fill the {@link FitnessIndividual#fitness} variable with a
   * value based on the set of provided records.
   *
   * @param pop
   *          the array of records, each holding a point from the
   *          search space and a quality value.
   */
  public abstract void
      assignFitness(FitnessIndividual<? extends X>[] pop);

  /** initialize the fitness assignment process */
  public void initialize() {
    // do nothing
  }
}
