// start relevant
package aitoa.examples.jssp;

import java.util.Arrays;

import aitoa.structure.IRepresentationMapping;

/**
 * The representation mapping: translate a one-dimensional
 * integer array to a candidate solution for the JSSP.
 */
public final class JSSPRepresentationMapping implements
    IRepresentationMapping<int[], JSSPCandidateSolution> {

  /**
   * a thread local with temporary variables: allow the use of
   * the objective function in multiple threads
   */
// the definition of the inner class __TemporaryVariables is
// omitted for the sake of brevity in the book
  private final ThreadLocal<__TemporaryVariables> m_temp;

  /**
   * the instance data: for each job, the sequence of machines
   * and times
   */
  private final int[][] m_jobs;

  /**
   * create the representation
   *
   * @param instance
   *          the problem instance
   */
  public JSSPRepresentationMapping(final JSSPInstance instance) {
    super();
    this.m_jobs = instance.jobs;
// thread-safe local variables (exact definition omitted in book)
    this.m_temp = ThreadLocal.withInitial(
        () -> new __TemporaryVariables(instance.m, instance.n));
  }

  /**
   * Map a point {@code x} from the search space to a candidate
   * solution {@code y} in the solution space.
   *
   * @param x
   *          the point in the search space
   * @param y
   *          the solution record, i.e., the Gantt chart
   */
  @Override
  public void map(final int[] x, final JSSPCandidateSolution y) {
// get/create temporary variables
    final __TemporaryVariables temp = this.m_temp.get();
// setup and initialize the temporary variables
    final int[] machineState = temp.m_machineState;
    Arrays.fill(machineState, -1);
    final int[] machineTime = temp.m_machineTime;
    Arrays.fill(machineTime, 0);
    final int[] jobState = temp.m_jobState;
    Arrays.fill(jobState, -1);
    final int[] jobTime = temp.m_jobTime;
    Arrays.fill(jobTime, 0);

// iterate over the jobs in the solution
    for (final int nextJob : x) {
// get the definition of the steps that we need to take for
// nextJob
      final int[] jobSteps = this.m_jobs[nextJob];
// jobState tells us the index in this list for the next step to
// do, but since the list contains machine/time pairs, we
// multiply by 2 (by left-shifting by 1)
      final int jobStep = (++jobState[nextJob]) << 1;

// so we know the machine where the job needs to go next
      final int machine = jobSteps[jobStep]; // get machine

// compute start time: it is either the next time when the
// machine becomes idle or the time we have already spent on the
// job - whatever happens later
      final int start =
          Math.max(machineTime[machine], jobTime[nextJob]);
// the end time is simply the start time plus the time the job
// needs to spend on the machine
      final int end = start + jobSteps[jobStep + 1]; // end time
// this holds for both the machine (it will become idle again
// after end) and the job (it can go to the next station after
// end)
      jobTime[nextJob] = machineTime[machine] = end;

// update the schedule with the data we have just computed
      final int[] schedule = y.schedule[machine];
      schedule[++machineState[machine]] = nextJob;
      schedule[++machineState[machine]] = start;
      schedule[++machineState[machine]] = end;
    }
  }
// end relevant

  /** an internal class for temporary variables */
  private static final class __TemporaryVariables {
    /**
     * the current time at a given machine
     */
    final int[] m_machineTime;
    /** the current step index at a given machine */
    final int[] m_machineState;
    /** the step index of the current job */
    final int[] m_jobState;
    /** the time of the current job */
    final int[] m_jobTime;

    /**
     * create an instance of the temporary variables
     *
     * @param m
     *          the number of machines
     * @param n
     *          the number of jobs
     */
    __TemporaryVariables(final int m, final int n) {
      super();
      this.m_jobState = new int[n];
      this.m_jobTime = new int[n];
      this.m_machineTime = new int[m];
      this.m_machineState = new int[m];
    }
  }
// start relevant
}
// end relevant