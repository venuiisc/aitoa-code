package aitoa.utils.logs;

import java.util.Arrays;

import aitoa.utils.Statistics;

/** a store of longs */
final class _Longs extends _Statistic {

  /** the internal data */
  private long[] m_data;

  /** the size */
  private int m_size;

  /** create */
  _Longs() {
    this.m_data = new long[10];
  }

  /**
   * create
   *
   * @param data
   *          the data
   */
  _Longs(final long[] data) {
    this.m_data = data;
    this.m_size = data.length;
  }

  /** {@inheritDoc} */
  @Override
  final void _add(final long value) {
    final int size = this.m_size;
    long[] data = this.m_data;

    if (value < 0L) {
      throw new IllegalArgumentException(
          "all long values must be >= 0, but encountered " //$NON-NLS-1$
              + value);
    }

    if (size >= data.length) {
      this.m_data =
          data = Arrays.copyOf(data, _Statistic._incSize(size));
    }

    data[size] = value;
    this.m_size = (size + 1);
  }

  /** {@inheritDoc} */
  @Override
  final _Longs _finalize() {
    if (this.m_size <= 0) {
      throw new IllegalStateException("empty data array?"); //$NON-NLS-1$
    }
    this.m_data = Arrays.copyOf(this.m_data, this.m_size);
    Arrays.sort(this.m_data);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  final int size() {
    return this.m_size;
  }

  /** {@inheritDoc} */
  @Override
  final Number _quantile(final double p) {
    return Statistics.quantile(p, this.m_data);
  }

  /** {@inheritDoc} */
  @Override
  final Number[] _meanAndStdDev() {
    return Statistics
        .sampleMeanAndStandardDeviation(this.m_data);
  }

  /** {@inheritDoc} */
  @Override
  final Number _divideSumBy(final int by) {
    return Statistics.divideExact(Statistics.sum(this.m_data),
        by);
  }
}