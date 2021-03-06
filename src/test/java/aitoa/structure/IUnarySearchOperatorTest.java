package aitoa.structure;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import aitoa.ObjectTest;
import aitoa.TestTools;

/**
 * This is a base class for testing unary search operators.
 *
 * @param <X>
 *          the data structure
 */
@Ignore
public abstract class IUnarySearchOperatorTest<X>
    extends ObjectTest<IUnarySearchOperator<X>> {

  /**
   * get an instance of the space backing the operator
   *
   * @return the space
   */
  protected abstract ISpace<X> getSpace();

  /**
   * Get the unary operator corresponding to the space
   *
   * @param space
   *          the space
   * @return the operator
   * @see #getSpace()
   */
  protected abstract IUnarySearchOperator<X>
      getOperator(final ISpace<X> space);

  /** {@inheritDoc} */
  @Override
  protected IUnarySearchOperator<X> getInstance() {
    return this.getOperator(this.getSpace());
  }

  /**
   * check if two instances of the data structure {@code X} are
   * equal or not
   *
   * @param a
   *          the first instance
   * @param b
   *          the second instance
   * @return {@code true} if they are equal, {@code false} if not
   */
  protected boolean equals(final X a, final X b) {
    return Objects.deepEquals(a, b);
  }

  /**
   * Create a valid point in the search space, i.e., instances of
   * the data structure {@code X} . Ideally, this method should
   * return a different a valid point in the search space every
   * time it is called.
   *
   * @return a valid instance
   */
  protected abstract X createValid();

  /**
   * test that the
   * {@link IUnarySearchOperator#apply(Object, Object, Random)}
   * method works and produces different, valid results while not
   * altering the source objects
   */
  @Test(timeout = 3600000)
  public void testApplyValidAndDifferent() {
    final ISpace<X> space = this.getSpace();
    final IUnarySearchOperator<X> op = this.getOperator(space);
    final Random random = ThreadLocalRandom.current();

    final X copy = space.create();
    final X dest = space.create();

    int count = 0;
    int different = 0;
    for (; (++count) <= 100;) {
      final X src = this.createValid();
      space.check(src);
      space.copy(src, copy);
      op.apply(src, dest, random);
      Assert.assertTrue(this.equals(src, copy));
      space.check(dest);
      if (!(this.equals(dest, src))) {
        different++;
      }
    }

    TestTools.assertGreaterOrEqual(count, 100);
    TestTools.assertGreaterOrEqual(different,
        (count - (count >>> 3)));
  }

  /**
   * test that the
   * {@link IUnarySearchOperator#enumerate(java.util.Random, Object, Object, java.util.function.Predicate)}
   * method works correctly and respects the return values of the
   * visitor
   */
  @SuppressWarnings("unchecked")
  @Test(timeout = 3600000)
  public void testEnumerate() {
    final ISpace<X> space = this.getSpace();
    final IUnarySearchOperator<X> op = this.getOperator(space);
    final Random random = ThreadLocalRandom.current();

    final X src = this.createValid();
    final X dest = space.create();
    final Object[] preserve = new Object[1024];

    if (op.canEnumerate()) {
      final X copy = space.create();
      space.copy(src, copy);

      final long[] count = new long[2];

      // perform one complete enumeration
      Assert.assertFalse(op.enumerate(random, src, dest, x -> {
        Assert.assertSame(dest, x);
        space.check(x);
        Assert.assertTrue(this.equals(src, copy));
        final long l = count[0]++;
        // check that elements do not appear twice
        for (int i = ((int) (Math.min(preserve.length, l)));
            (--i) >= 0;) {
          Assert.assertFalse(//
              this.equals(((X) (preserve[i])), x));
        }
        if (l < preserve.length) {
          final X cc = space.create();
          space.copy(x, cc);
          preserve[(int) l] = cc;
        }
        return false;
      }));
      Assert.assertTrue(this.equals(src, copy));
      TestTools.assertGreater(count[0], 0L);

      // test two enumerations have the same number of steps
      Assert.assertFalse(op.enumerate(random, src, dest, x -> {
        final long l = count[1]++;
        // check that elements do not appear twice
        for (int i = ((int) (Math.min(preserve.length, l)));
            (--i) >= 0;) {
          Assert.assertFalse(//
              this.equals(((X) (preserve[i])), x));
        }
        if (l < preserve.length) {
          space.copy(x, ((X) (preserve[(int) l])));
        }

        return false;
      }));
      Assert.assertTrue(this.equals(src, copy));
      Assert.assertEquals(count[0], count[1]);
      Arrays.fill(preserve, null);

      // assert that stopping works 1
      count[1] = 0L;
      Assert.assertTrue(op.enumerate(random, src, dest, x -> {
        ++count[1];
        return true;
      }));
      Assert.assertTrue(this.equals(src, copy));
      Assert.assertEquals(1L, count[1]);

      if (count[0] > 5L) {
// assert that stopping works 2
        count[1] = 0L;
        Assert.assertTrue(op.enumerate(random, src, dest,
            x -> ((++count[1]) == 5L)));
        Assert.assertTrue(this.equals(src, copy));
        Assert.assertEquals(5L, count[1]);
      }

    } else {
      boolean error = true;
      try {
        op.enumerate(random, src, dest, x -> false);
      } catch (@SuppressWarnings("unused") final UnsupportedOperationException ex) {
        error = false;
      }
      if (error) {
        Assert.fail(//
            "canEnumerate() returned false, but enumerate did not throw an UnsupportedOperationException"); //$NON-NLS-1$
      }
    }
  }
}
