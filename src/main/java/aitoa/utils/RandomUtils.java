package aitoa.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import aitoa.structure.LogFormat;

/** Utilities for random number generation */
public final class RandomUtils {

  /**
   * Generate {@code count} unique seeds for a random number
   * generator. This method is guaranteed to return the same
   * sequence for the same {@code string} string. The idea is
   * that we can have the name of a problem instance and, based
   * on that name, generate the same unique random seeds. We can
   * then use one seed for each independent run of an
   * optimization algorithm. Of course, the runs would not be
   * entirely independent, since they depend on the same original
   * random seed.
   *
   * @param string
   *          the string to be used to generate the random seeds
   * @param count
   *          the number of seeds to generate
   * @return an array of length {@code code} with the unique
   *         seeds
   */
  public static final long[]
      uniqueRandomSeeds(final String string, final int count) {
    final long a, b;

    if (count <= 0) {
      throw new IllegalArgumentException(
          "invalid count: " + count); //$NON-NLS-1$
    }

    // compute the 16 byte MD5 digest from the string, which will
    // give us two random seeds
    try {
      final MessageDigest digest =
          MessageDigest.getInstance("MD5"); //$NON-NLS-1$
      try (
          final ByteArrayInputStream bis =
              new ByteArrayInputStream(
                  digest.digest(string.getBytes()));
          final DataInputStream dis = new DataInputStream(bis)) {
        a = dis.readLong();
        b = dis.readLong();
      }
    } catch (final Throwable error) {
      throw new IllegalArgumentException(
          "Error when computing random seed from string '" + //$NON-NLS-1$
              string + "'.", //$NON-NLS-1$
          error);
    }

// if we only want at most 1 random seed, that may already be
// enough
    if (count <= 1) {// yes
      return new long[] { a };
    }

// to generate "count" unique seeds
    final long[] seeds = new long[count];
    int have;

// sort the two seeds we already have
    if (a < b) {
      seeds[0] = a;
      seeds[1] = b;
      if (count <= 2) {
        return seeds;
      }
      have = 2;
    } else {// a > b
      if (a > b) {
        seeds[1] = a;
        seeds[0] = b;
        if (count <= 2) {
          return seeds;
        }
        have = 2;
      } else {// a==b
        seeds[1] = a;
        have = 1;
      }
    }

    // create random number generators
    final Random[] random;
    if (a != b) {
      // we can create two random number generators which are
      // more or less independent
      random = new Random[] { new Random(a), //
          new Random(b) };
    } else {
      // we only have one unique seed
      random = new Random[] { new Random(a) };
    }

    int ridx = 0; // the currently used random number generator
    // do count times:
    for (; have < count; have++) {
      // find a new unique, not yet discovered seed
      findNextUnique: for (;;) {
        // generate the seed from current random number generator
        final long next = random[ridx].nextLong();
        ridx = (ridx + 1) % random.length;
        // check if we already had it
        for (int j = have; (--j) >= 0;) {
          if (seeds[j] == next) {
            // already seen, skip to next
            continue findNextUnique;
          }
        }
        // store seed
        seeds[have] = next;
        break findNextUnique;
      }
    }

    // sort the array such that longs whose hexadecimal
    // representation is smaller come first - this is useful to
    // make the order in which files are created is identical to
    // their alphabetical order, as long as the file names
    // contain the rand seed as hex stringF
    final Long[] temp = new Long[seeds.length];
    int i = 0;
    for (final long l : seeds) {
      temp[i++] = Long.valueOf(l);
    }

    Arrays.sort(temp, Long::compareUnsigned);
    i = 0;
    for (final Long l : temp) {
      seeds[i++] = l.longValue();
    }
    return seeds;
  }

  /** the chars */
  private static final char[] CHOOSE = { '0', '1', '2', '3', '4',
      '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  /**
   * Convert a random seed to a character string
   *
   * @param seed
   *          the seed
   * @return the string
   */
  public static final String randSeedToString(final long seed) {
    final int l = LogFormat.RANDOM_SEED_PREFIX.length();
    final char[] str = new char[l + 16];
    LogFormat.RANDOM_SEED_PREFIX.getChars(0, l, str, 0);
    long cmp = seed;
    for (int i = (l + 16); (--i) >= l;) {
      str[i] = RandomUtils.CHOOSE[(int) (cmp & 0xfL)];
      cmp >>>= 4L;
    }
    return String.valueOf(str);
  }

  /**
   * Parse a random seed string to a {@code long}
   *
   * @param randSeedString
   *          the random seed string
   * @return the {@code long} representing the string
   */
  public static final long
      stringToRandSeed(final String randSeedString) {
    if ((!randSeedString
        .startsWith(LogFormat.RANDOM_SEED_PREFIX))
        || (randSeedString.length() < 3)) {
      throw new IllegalArgumentException(
          "Random seed must start with '" //$NON-NLS-1$
              + LogFormat.RANDOM_SEED_PREFIX
              + "' and contain at least one hexadecimal digit, but is "//$NON-NLS-1$
              + randSeedString);
    }
    try {
      return Long.parseUnsignedLong(randSeedString
          .substring(LogFormat.RANDOM_SEED_PREFIX.length()), 16);
    } catch (final NumberFormatException nfe) {
      throw new IllegalArgumentException(
          "Invalid random seed: " + //$NON-NLS-1$
              randSeedString,
          nfe);
    }
  }

  /**
   * Randomize a sub-sequence of an array or permutation of
   * {@code java.lang.Objects}. After this procedure, the
   * {@code count} elements of the array beginning at index
   * {@code start} are uniformly randomly distributed.
   *
   * @param array
   *          the array of {@code java.lang.Object}s whose
   *          sub-sequence to be randomized
   * @param start
   *          the start index
   * @param count
   *          the number of elements to be randomized
   * @param random
   *          the randomizer
   */
  public static final void shuffle(final Random random,
      final java.lang.Object[] array, final int start,
      final int count) {
    if (count > 0) {
      final int n = array.length;
      for (int i = count; i > 1;) {
        final int j = ((start + random.nextInt(i--)) % n);
        final int k = ((start + i) % n);
        final Object t = array[k];
        array[k] = array[j];
        array[j] = t;
      }
    }
  }

  /**
   * Randomize a sub-sequence of an array or permutation of
   * {@code java.lang.Objects}. After this procedure, the
   * {@code count} elements of the array beginning at index
   * {@code start} are uniformly randomly distributed.
   *
   * @param array
   *          the array of {@code java.lang.Object}s whose
   *          sub-sequence to be randomized
   * @param start
   *          the start index
   * @param count
   *          the number of elements to be randomized
   * @param random
   *          the randomizer
   */
  public static final void shuffle(final Random random,
      final long[] array, final int start, final int count) {
    if (count > 0) {
      final int n = array.length;
      for (int i = count; i > 1;) {
        final int j = ((start + random.nextInt(i--)) % n);
        final int k = ((start + i) % n);
        final long t = array[k];
        array[k] = array[j];
        array[j] = t;
      }
    }
  }

  /**
   * Randomize a sub-sequence of an array or permutation of
   * {@code java.lang.Objects}. After this procedure, the
   * {@code count} elements of the array beginning at index
   * {@code start} are uniformly randomly distributed.
   *
   * @param array
   *          the array of {@code java.lang.Object}s whose
   *          sub-sequence to be randomized
   * @param start
   *          the start index
   * @param count
   *          the number of elements to be randomized
   * @param random
   *          the randomizer
   */
  public static final void shuffle(final Random random,
      final int[] array, final int start, final int count) {
    if (count > 0) {
      final int n = array.length;
      for (int i = count; i > 1;) {
        final int j = ((start + random.nextInt(i--)) % n);
        final int k = ((start + i) % n);
        final int t = array[k];
        array[k] = array[j];
        array[j] = t;
      }
    }
  }

  /**
   * Create a uniformly distributed random {@code long} from
   * 0...(N-1). This is basically a version of
   * {@link java.util.Random#nextInt(int)} translated to
   * {@code long}.
   *
   * @param random
   *          the random number generator
   * @param N
   *          the exclusive upper bound
   * @return the long
   */
  public static final long
      uniformFrom0ToNminus1(final Random random, final long N) {
    long bits, val;
    do {
      bits = (random.nextLong() << 1L) >>> 1L;
      val = bits % N;
    } while (((bits - val) + (N - 1L)) < 0L);
    return val;
  }

  /**
   * Create a uniformly distributed random {@code long} from
   * 0...(N-1).
   *
   * @param random
   *          the random number generator
   * @param M
   *          the inclusive lower bound
   * @param N
   *          the inclusive upper bound
   * @return the long
   */
  public static final long uniformFromMtoN(final Random random,
      final long M, final long N) {

    long r = random.nextLong();
    final long m = N - M;
    final long n = m + 1L;
    if ((n & m) == 0L) { // power of two
      r = ((r & m) + M);
    } else {
      if (n > 0L) { // reject over-represented candidates
        for (long u = (r >>> 1); // ensure nonnegative
            ((u + m) - (r = (u % n))) < 0L; // rejection check
            u = (random.nextLong() >>> 1)) {
          /* */}
        r += M;
      } else { // range not representable as long
        while ((r < M) || (r > N)) {
          r = random.nextLong();
        }
      }
    }
    return r;
  }
}
