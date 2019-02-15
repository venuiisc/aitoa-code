package aitoa.examples.jssp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/** Print the solution space sizes for the JSSP instances */
public class JSSPSolutionSpaceSize {

  /**
   * compute the factorial
   *
   * @param input
   *          the input value
   * @return the factorial
   */
  static final BigInteger factorial(final BigInteger input) {
    BigInteger result = BigInteger.ONE;
    BigInteger n = input;

    while (!n.equals(BigInteger.ZERO)) {
      result = result.multiply(n);
      n = n.subtract(BigInteger.ONE);
    }

    return result;
  }

  /**
   * compute the search space size as string
   *
   * @param m
   *          the m
   * @param n
   *          the n
   * @return the size
   */
  static final BigInteger solutionSpaceSize(final int m, final int n) {
    return factorial(BigInteger.valueOf(n)).pow(m);
  }

  /**
   * format a big integer to a string
   *
   * @param v
   *          the big integer
   * @return the value
   */
  static final String toString(final BigInteger v) {
    final String s = v.toString();
    final StringBuilder sb = new StringBuilder();

    final int length = s.length();

    if (length > 15) {
      final BigDecimal bd = new BigDecimal(v);
      final String vv[] = new DecimalFormat("0.000E00")//$NON-NLS-1$
          .format(bd).split("E"); //$NON-NLS-1$

      return ("$\\approx$ " + //$NON-NLS-1$
          vv[0] + "*10^" + //$NON-NLS-1$
          Integer.parseInt(vv[1]) + "^");//$NON-NLS-1$
    }

    for (int i = length, j = 0; (--i) >= 0;) {
      sb.insert(0, s.charAt(i));
      if (((++j) % 3) == 0) {
        if (j < length) {
          sb.insert(0, '\'');
        }
      }
    }
    return sb.toString();
  }

  /**
   * The main routine
   *
   * @param args
   *          ignore
   */

  public static final void main(final String[] args) {
    final ArrayList<String> printFor = new ArrayList<>();
    printFor.add("demo"); //$NON-NLS-1$
    printFor.addAll(Arrays.asList(JSSPExperiment.INSTANCES));

    System.out.println(
        "|name|$\\jsspJobs$|$\\jsspMachines$|$\\left|\\solutionSpace\\right|$|"); //$NON-NLS-1$
    System.out.println("|:--|--:|--:|--:|"); //$NON-NLS-1$
    for (final int n : new int[] { 3, 4, 5 }) {
      for (final int m : new int[] { 2, 3, 4, 5 }) {
        System.out.print('|');
        System.out.print('|');
        System.out.print(n);
        System.out.print('|');
        System.out.print(m);
        System.out.print('|');
        System.out.print(JSSPSolutionSpaceSize
            .toString(JSSPSolutionSpaceSize.solutionSpaceSize(m, n)));
        System.out.println();
      }
    }

    for (final String s : printFor) {
      final JSSPInstance inst = new JSSPInstance(s);

      System.out.print(inst.id);
      System.out.print('|');
      System.out.print(inst.n);
      System.out.print('|');
      System.out.print(inst.m);
      System.out.print('|');
      System.out.print(JSSPSolutionSpaceSize.toString(
          JSSPSolutionSpaceSize.solutionSpaceSize(inst.m, inst.n)));
      System.out.println();
    }
  }
}
