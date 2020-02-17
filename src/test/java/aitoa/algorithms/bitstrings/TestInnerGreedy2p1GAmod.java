package aitoa.algorithms.bitstrings;

import aitoa.structure.IMetaheuristic;

/** Test a the Greedy2p1GAmod algorithm */
public class TestInnerGreedy2p1GAmod
    extends TestBitStringMetaheuristic {

  /** {@inheritDoc} */
  @Override
  protected IMetaheuristic<boolean[], boolean[]>
      createMetaheuristic(final int n, final int UB) {
    return new InnerGreedy2p1GAmod<>(1);
  }
}