package aitoa.searchSpaces.bitstrings;

import java.util.Random;
import java.util.function.Predicate;

import aitoa.structure.IUnarySearchOperator;
import aitoa.utils.RandomUtils;

/**
 * A unary operator for flipping one or two or three bits.
 */
public final class BitStringUnaryOperator3Flip
    implements IUnarySearchOperator<boolean[]> {
  /** the indexes */
  private final int[] mIndexes;

  /**
   * create the 3-bit flip unary operator
   *
   * @param pLength
   *          the _length
   */
  public BitStringUnaryOperator3Flip(final int pLength) {
    super();

    this.mIndexes = new int[BitStringSpace.checkLength(pLength)];
    for (int i = this.mIndexes.length; (--i) >= 0;) {
      this.mIndexes[i] = i;
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "3flip"; //$NON-NLS-1$
  }

  /**
   * Sample a point from the neighborhood of {@code x} by
   * flipping either one or two bits inside of {@code x}.
   *
   * @param x
   *          {@inheritDoc}
   * @param dest
   *          {@inheritDoc}
   * @param random
   *          {@inheritDoc}
   */
  @Override
  public void apply(final boolean[] x, final boolean[] dest,
      final Random random) {
    System.arraycopy(x, 0, dest, 0, x.length);
    final int i = random.nextInt(dest.length);
    dest[i] ^= true;
    final int j = random.nextInt(dest.length);
    if (i != j) {
      dest[j] ^= true;
    }
    final int k = random.nextInt(dest.length);
    if ((i != k) && (j != k)) {
      dest[k] ^= true;
    }
  }

  /**
   * We visit all points in the search space that could possibly
   * be reached by applying one
   * {@linkplain #apply(boolean[], boolean[], Random) search
   * move} to {@code x}. We therefore need to iteratively test
   * all possible index pairs {@code i}, {@code j}, and
   * {@code k}.
   *
   * @param random
   *          {@inheritDoc}
   * @param x
   *          {@inheritDoc}
   * @param dest
   *          {@inheritDoc}
   * @param visitor
   *          {@inheritDoc}
   */
  @Override
  public boolean enumerate(final Random random,
      final boolean[] x, final boolean[] dest,
      final Predicate<boolean[]> visitor) {
    final int[] indexes = this.mIndexes;
    // randomize the order in which indices are processed
    System.arraycopy(x, 0, dest, 0, x.length); // copy x to dest
    RandomUtils.shuffle(random, indexes, 0, indexes.length);

    for (int i = indexes.length; (--i) >= 0;) {
      final int ii = indexes[i];
      dest[ii] ^= true; // flip
      if (visitor.test(dest)) {
        return true; // visitor says: stop -> return true
      } // visitor did not say stop, so we need to
      for (int j = i; (--j) >= 0;) {
        final int ji = indexes[j];
        dest[ji] ^= true; // second flip
        if (visitor.test(dest)) {
          return true; // visitor says: stop -> return true
        }
        for (int k = j; (--k) >= 0;) {
          final int ki = indexes[k];
          dest[ki] ^= true; // third flip
          if (visitor.test(dest)) {
            return true; // visitor says: stop -> return true
          }
          dest[ki] ^= true; // revert the third flip
        }
        dest[ji] ^= true; // revert the second flip
      }
      dest[ii] ^= true; // revert the first flip
    }
    return false; // we have enumerated the complete neighborhood
  }

  /** {@inheritDoc} */
  @Override
  public boolean canEnumerate() {
    return true;
  }
}
