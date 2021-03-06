package aitoa.examples.jssp;

import java.util.Arrays;
import java.util.Random;

import aitoa.structure.IBinarySearchOperator;

/**
 * An implementation of a binary search operator for the JSSP
 * representation, where jobs are appended from both parents. We
 * randomly pick one of the two parents, take its first
 * operation, and add it to the (initially empty) child. We then
 * mark this operation (i.e., the first occurrence of the job id)
 * as done in both parents. We then, in each step, randomly pick
 * one of the parents and take the next, not-yet-done operation
 * from it and add it to the child. We mark the operation (i.e.,
 * the first unmarked occurrence of the job id) as done in both
 * parents. We do this until the child schedule representation
 * has been filled, at which point all operations from all
 * parents must have been completed.
 */
// start relevant
public final class JSSPBinaryOperatorSequence
    implements IBinarySearchOperator<int[]> {
// end relevant

  /** the done elements from x0 */
  private final boolean[] mDoneX0;
  /** the done elements from x1 */
  private final boolean[] mDoneX1;

  /**
   * create the sequence crossover operator
   *
   * @param pInstance
   *          the JSSP instance
   */
  public JSSPBinaryOperatorSequence(
      final JSSPInstance pInstance) {
    super();
    final int length = pInstance.n * pInstance.m;
    this.mDoneX0 = new boolean[length];
    this.mDoneX1 = new boolean[length];
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "sequence"; //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @Override
// start relevant
  public void apply(final int[] x0, final int[] x1,
      final int[] dest, final Random random) {
// omitted: initialization of arrays doneX0 and doneX1 (that
// remember the already-assigned operations from x0 and x1) of
// length=m*n to all false; and indices desti, x0i, x10 to 0
// end relevant

    final boolean[] doneX0 = this.mDoneX0;
    Arrays.fill(doneX0, false); // nothing used from x0 yet
    final boolean[] doneX1 = this.mDoneX1;
    Arrays.fill(doneX1, false); // nothing used from xy yet

    final int length = doneX0.length; // length = m*n
    int desti = 0; // all array indexes = 0
    int x0i = 0;
    int x1i = 0;
// start relevant
    for (;;) { // repeat until dest is filled, i.e., desti=length
// randomly chose a source point and pick next operation from it
      final int add = random.nextBoolean() ? x0[x0i] : x1[x1i];
      dest[desti++] = add; // we picked a operation and added it
      if (desti >= length) { // if desti==length, we are finished
        return; // in this case, desti is filled and we can exit
      }

      for (int i = x0i;; i++) { // mark operation as done in x0
        if ((x0[i] == add) && (!doneX0[i])) { // find added job
          doneX0[i] = true;// found it and marked it
          break; // quit operation finding loop
        }
      }
      while (doneX0[x0i]) { // now we move the index x0i to the
        x0i++; // next, not-yet completed operation in x0
      }

      for (int i = x1i;; i++) { // mark operation as done in x1
        if ((x1[i] == add) && (!doneX1[i])) { // find added job
          doneX1[i] = true; // found it and marked it
          break; // quit operation finding loop
        }
      }
      while (doneX1[x1i]) { // now we move the index x1i to the
        x1i++; // next, not-yet completed operation in x0
      }
    } // loop back to main loop and to add next operation
  } // end of function
}
// end relevant
