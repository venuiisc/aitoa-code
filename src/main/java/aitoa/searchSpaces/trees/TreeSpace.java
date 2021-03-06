package aitoa.searchSpaces.trees;

import java.io.IOException;
import java.util.Objects;

import aitoa.structure.ISpace;

/** The space for trees */
public final class TreeSpace implements ISpace<Node[]> {

  /** the maximum permitted node depth */
  private final int mMaxDepth;

  /**
   * create the tree space
   *
   * @param pMaxDepth
   *          the maximum depth parameter
   */
  public TreeSpace(final int pMaxDepth) {
    super();
    this.mMaxDepth = TreeSpace.checkMaxDepth(pMaxDepth);
  }

  /**
   * check the maximum permitted depth
   *
   * @param md
   *          the maximum depth
   * @return the maximum depth to use
   */
  static int checkMaxDepth(final int md) {
    if (md < 2) {
      throw new IllegalArgumentException(
          "maximum depth must be at least 2, but is " //$NON-NLS-1$
              + md);
    }
    return md;
  }

  /** create the tree space */
  public TreeSpace() {
    this(Integer.MAX_VALUE);
  }

  /** {@inheritDoc} */
  @Override
  public Node[] create() {
    return new Node[1];
  }

  /** {@inheritDoc} */
  @Override
  public void copy(final Node[] from, final Node[] to) {
    to[0] = from[0];
  }

  /** {@inheritDoc} */
  @Override
  public void print(final Node[] z, final Appendable out)
      throws IOException {
    final Node n = z[0];
    if (n != null) {
      n.asText(out);
      out.append(System.lineSeparator());
      out.append(System.lineSeparator());
      n.asJavaPrintParameters(out);
    } else {
      out.append("null"); //$NON-NLS-1$
    }
  }

  /**
   * check a node
   *
   * @param n
   *          the node to check
   */
  static void checkNode(final Node n) {
    if (n == null) {
      throw new IllegalArgumentException(
          "Node array must contain a node.");//$NON-NLS-1$
    }

    final NodeType<?> t = n.getType();

    if (t == null) {
      throw new IllegalArgumentException(
          "Node must have node type set."); //$NON-NLS-1$
    }

    final NodeTypeSet<?> ts = t.getTypeSet();
    if (ts == null) {
      throw new IllegalArgumentException(
          "Node type set cannot be null."); //$NON-NLS-1$
    }
    if (!(ts.containsType(t))) {
      throw new IllegalArgumentException(
          "Node type set does not contain node type?"); //$NON-NLS-1$
    }

    if (Objects.equals(t, NodeType.dummy())) {
      throw new IllegalArgumentException(
          "Node type of a node in optimization code cannot be dummy type."); //$NON-NLS-1$
    }
    final int typeChildCount = t.getChildCount();
    final int nodeChildCount = n.getChildCount();
    if (typeChildCount != nodeChildCount) {
      throw new IllegalArgumentException(
          "Child-count disagreement: node says " + //$NON-NLS-1$
              nodeChildCount + ", while type says " + //$NON-NLS-1$
              typeChildCount);
    }

    int d = 0;
    int w = 1;

    for (int i = nodeChildCount; (--i) >= 0;) {
      final Node c = n.getChild(i);

      final NodeTypeSet<?> cts = c.getType().getTypeSet();
      final NodeTypeSet<?> tts = t.getChildTypes(i);
      if (cts != tts) {
        throw new IllegalArgumentException(
            "Child node not permitted at index "//$NON-NLS-1$
                + i + " due to type conflict.");//$NON-NLS-1$
      }

      d = Math.max(d, c.depth());
      w += c.weight();
      TreeSpace.checkNode(c);
    }

    ++d;
    final int nd = n.depth();
    if (d != nd) {
      throw new IllegalArgumentException(
          "Depth disagreement: node says " + //$NON-NLS-1$
              nd + ", while computation yields " + //$NON-NLS-1$
              d);
    }

    final int nw = n.weight();
    if (w != nw) {
      throw new IllegalArgumentException(
          "Weight disagreement: node says " + //$NON-NLS-1$
              nw + ", while computation yields " + //$NON-NLS-1$
              w);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void check(final Node[] z) {
    if ((z == null) || (z.length != 1)) {
      throw new IllegalArgumentException(
          "Node array must not be null and must be of length 1.");//$NON-NLS-1$
    }

    final Node n = z[0];
    if (n == null) {
      throw new IllegalArgumentException(
          "Root node cannot be null.");//$NON-NLS-1$
    }
    final int d = n.depth();
    if ((d <= 0) || (d > this.mMaxDepth)) {
      throw new IllegalArgumentException(
          "Invalid root node depth " //$NON-NLS-1$
              + d + ", must be in 1.." + //$NON-NLS-1$
              this.mMaxDepth);
    }
    TreeSpace.checkNode(n);
  }

  /** {@inheritDoc} */
  @Override
  public double getScale() {
    return this.mMaxDepth;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "trees:" + this.mMaxDepth;//$NON-NLS-1$
  }
}
