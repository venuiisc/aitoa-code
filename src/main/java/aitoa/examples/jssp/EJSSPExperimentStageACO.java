package aitoa.examples.jssp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import aitoa.algorithms.EDA;
import aitoa.examples.jssp.aco.JSSPACOMakespanObjectiveFunction;
import aitoa.examples.jssp.aco.JSSPACORecord;
import aitoa.examples.jssp.aco.JSSPACOSpace;
import aitoa.examples.jssp.aco.JSSPPACOModelAge;
import aitoa.structure.BlackBoxProcessBuilder;
import aitoa.structure.IMetaheuristic;
import aitoa.utils.Experiment.IExperimentStage;

/** the stages of the JSSP experiment */
public enum EJSSPExperimentStageACO implements
    IExperimentStage<JSSPACORecord, JSSPACORecord,
        JSSPACOMakespanObjectiveFunction,
        IMetaheuristic<JSSPACORecord, JSSPACORecord>> {

  /** the first stage: random sampling */
  STAGE_ACO_1 {

    /**
     * Get a stream of algorithm suppliers for a given problem
     *
     * @param problem
     *          the problem
     * @return the stream of suppliers
     */
    @Override
    public
        Stream<Supplier<
            IMetaheuristic<JSSPACORecord, JSSPACORecord>>>
        getAlgorithms(//
            final JSSPACOMakespanObjectiveFunction problem) {
      final ArrayList<Supplier<
          IMetaheuristic<JSSPACORecord, JSSPACORecord>>> list =
              new ArrayList<>();

      for (final int mu : new int[] { 1 }) {
        for (final int lambda : new int[] { 1024, 2048 }) {
          for (final int k : new int[] { 4, 5 }) {
            for (final double beta : new double[] { 2.5d }) {
              for (final double q0 : new double[] { 0.5d }) {
                for (final double tauMax : new double[] { 1d }) {
                  list.add(() -> {
                    final JSSPPACOModelAge model =
                        new JSSPPACOModelAge(
                            problem.getInstance(), //
                            k, q0, beta, tauMax);
                    return new EDA<>(model, mu, lambda, model);
                  });
                }
              }
            }
          }
        }
      }

      for (final int mu : new int[] { 1 }) {
        for (final int lambda : new int[] { 16384, 32768 }) {
          for (final int k : new int[] { 4 }) {
            for (final double beta : new double[] { 2.5d }) {
              for (final double q0 : new double[] { 0.5d }) {
                for (final double tauMax : new double[] { 1d }) {
                  final JSSPPACOModelAge model =
                      new JSSPPACOModelAge(problem.getInstance(), //
                          k, q0, beta, tauMax);
                  list.add(
                      () -> new EDA<>(model, mu, lambda, model));
                }
              }
            }
          }
        }
      }

      return list.stream();
    }
  };

  /**
   * create the problems
   *
   * @return the stream of problems
   */
  @Override
  public Stream<Supplier<JSSPACOMakespanObjectiveFunction>>
      getProblems() {
    return Arrays.stream(EJSSPExperimentStage.INSTANCES).map(//
        s -> () -> new JSSPACOMakespanObjectiveFunction(s));
  }

  /**
   * get the number of runs
   *
   * @param problem
   *          the problem
   * @return the runs
   */
  @Override
  public int
      getRuns(final JSSPACOMakespanObjectiveFunction problem) {
    return 101;
  }

  /**
   * Configure the black box process builder.
   *
   * @param builder
   *          the builder to configure
   */
  @Override
  public void configureBuilder(final BlackBoxProcessBuilder<
      JSSPACORecord, JSSPACORecord> builder) {
    builder.setMaxTime(TimeUnit.MINUTES.toMillis(3L));
  }

  /**
   * Configure the black box process builder for the given
   * problem.
   *
   * @param builder
   *          the builder to configure
   * @param problem
   *          the problem
   */
  @Override
  public void configureBuilderForProblem(
      final BlackBoxProcessBuilder<JSSPACORecord,
          JSSPACORecord> builder,
      final JSSPACOMakespanObjectiveFunction problem) {
    final JSSPInstance inst =
        Objects.requireNonNull(problem.getInstance());
    builder.setSearchSpace(new JSSPACOSpace(inst));
    builder.setObjectiveFunction(problem);
  }

  /**
   * Get the stream of the experiment stages defined here
   *
   * @return the stream
   */
  static final Stream<Supplier<IExperimentStage<?, ?, ?, ?>>>
      stream() {
    return Arrays.stream(EJSSPExperimentStageACO.values())
        .map(s -> () -> s);
  }
}
