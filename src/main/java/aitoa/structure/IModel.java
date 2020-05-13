package aitoa.structure;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Create a statistical model for use inside of an Estimation of
 * Distribution Algorithm
 *
 * @param <X>
 *          the search space
 */
// start relevant
public interface IModel<X> {

  /** initialize the model */
  void initialize();

  /**
   * Update the model. The internal data structures of the model
   * will be updated based on the information of the selected
   * elements.
   *
   * @param selected
   *          the array with the points in the search space that
   *          have been selected
   */
  void update(final Iterable<X> selected);

  /**
   * Sample the model and fill the destination point in the
   * search space with new, valid content
   *
   * @param dest
   *          the point in the search space to be overwritten
   *          with the new content
   * @param random
   *          the random number generator
   */
  void sample(final X dest, final Random random);

  /**
   * The minimum number of samples needed to perform an update
   *
   * @return the minimum number of samples needed for an update
   */
  default int minimumSamplesNeededForUpdate() {
    return 1;
  }
// end relevant

  /**
   * Create an iterable over a given range of the specified
   * array. The range must be non-empty.
   *
   * @param array
   *          the array
   * @param start
   *          the inclusive start index
   * @param end
   *          the exclusive end index
   * @return the iterable
   * @param <X>
   *          the element type
   * @throws IllegalArgumentException
   *           if the range is empty or exceeds the array length
   */
  static <X> Iterable<X> use(final X[] array, final int start,
      final int end) {
    _Iterator._checkRange(array, start, end);
    return () -> new _ArrayIterator<>(array, start, end);
  }

  /**
   * Create an iterable over a given range of the specified
   * array, where each element is actually a supplier for the
   * wanted type. From each supplier, the {@link Supplier#get()}
   * method will then be invoked at most once during the
   * iteration. The range must be non-empty.
   *
   * @param array
   *          the array
   * @param start
   *          the inclusive start index
   * @param end
   *          the exclusive end index
   * @return the iterable
   * @param <X>
   *          the element type
   * @throws IllegalArgumentException
   *           if the range is empty or exceeds the array length
   */
  static <X> Iterable<X> use(final Supplier<X>[] array,
      final int start, final int end) {
    _Iterator._checkRange(array, start, end);
    return () -> new _SupplierArrayIterator<>(array, start, end);
  }
// start relevant
}
// end relevant
