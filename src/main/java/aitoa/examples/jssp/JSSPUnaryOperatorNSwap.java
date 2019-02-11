package aitoa.examples.jssp;

import java.util.Random;

import aitoa.structure.IUnarySearchOperator;

/**
 * An implementation of the unary search operator for the JSSP
 * representation where a random number of jobs are swapped.
 */
// start relevant
public final class JSSPUnaryOperatorNSwap
    implements IUnarySearchOperator<int[]> {
// end relevant

  /** create the representation */
  public JSSPUnaryOperatorNSwap() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return "nswap"; //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @Override
// start relevant
  public final void apply(final int[] x, final int[] dest,
      final Random random) {
// copy the source point in search space to the dest
    System.arraycopy(x, 0, dest, 0, x.length);

// choose the index of the first sub-job to swap
    int i = random.nextInt(dest.length);
    final int first = dest[i];
    int last = first; // last stores the last job id to swap with

    boolean hasNext;
    do { // we repeat a geometrically distributed number of times
      hasNext = random.nextBoolean();
      inner: for (;;) {// find a location with a different job
        final int j = random.nextInt(dest.length);
        final int job_j = dest[j];
        if ((last != job_j) && // don't swap job with itself
            (hasNext || (first != job_j))) { // also not at end
          dest[i] = job_j; // overwrite job at index i with job_j
          i = j; // remember index j: we will overwrite it next
          last = job_j; // but not with the same value job_j...
          break inner;
        }
      }
    } while (hasNext); // Bernoulli process

    dest[i] = first; // write back first id to last copied index
  }
}
// end relevant