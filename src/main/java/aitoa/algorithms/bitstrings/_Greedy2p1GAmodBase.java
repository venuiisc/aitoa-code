package aitoa.algorithms.bitstrings;

import java.io.BufferedWriter;
import java.io.IOException;

import aitoa.structure.IMetaheuristic;
import aitoa.structure.LogFormat;

/**
 * The Greedy (2+1) GA mod, as defined in Algorithm 6 of E.
 * Carvalho Pinto and C. Doerr, "Towards a more practice-aware
 * runtime analysis of evolutionary algorithms," July 2017,
 * arXiv:1812.00493v1 [cs.NE] 3 Dec 2018. [Online]. Available:
 * http://arxiv.org/pdf/1812.00493.pdf
 *
 * @param <Y>
 *          the solution space
 */
abstract class _Greedy2p1GAmodBase<Y>
    implements IMetaheuristic<boolean[], Y> {

  /** The default value {@code 1.618033989} for {@link #c} */
  public static final double DEFAULT_C =
      0.5d + (0.5d * Math.sqrt(5d));

  /** the constant above n */
  public final double c;

  /**
   * create
   *
   * @param _c
   *          the constant above n to define the mutation
   *          probability
   */
  _Greedy2p1GAmodBase(final double _c) {
    super();
    if ((!(Double.isFinite(_c))) || (_c <= 0d) || (_c > 1e5d)) {
      throw new IllegalArgumentException(
          "m must be in (0, 1e5], but you specified " //$NON-NLS-1$
              + _c);
    }
    this.c = _c;
  }

  /** {@inheritDoc} */
  @Override
  public void printSetup(final BufferedWriter output)
      throws IOException {
    IMetaheuristic.super.printSetup(output);
    output.write(LogFormat.mapEntry("mu", 2));///$NON-NLS-1$
    output.newLine();
    output.write(LogFormat.mapEntry("lambda", 1));//$NON-NLS-1$
    output.newLine();
    output.write(LogFormat.mapEntry("cr", 1));//$NON-NLS-1$
    output.newLine();
    output.write(LogFormat.mapEntry("pruning", true)); //$NON-NLS-1$
    output.newLine();
    output.write(LogFormat.mapEntry("restarts", false)); //$NON-NLS-1$
    output.newLine();
    output.write(LogFormat.mapEntry("c", this.c)); //$NON-NLS-1$
    output.newLine();
    output.write(LogFormat.mapEntry("cIsDefault", //$NON-NLS-1$
        this.c == _Greedy2p1GAmodBase.DEFAULT_C));
    output.newLine();
  }
}
