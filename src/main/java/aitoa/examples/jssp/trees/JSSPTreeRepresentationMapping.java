package aitoa.examples.jssp.trees;

import java.util.Arrays;
import java.util.Random;

import aitoa.examples.jssp.JSSPCandidateSolution;
import aitoa.examples.jssp.JSSPInstance;
import aitoa.searchSpaces.trees.Node;
import aitoa.searchSpaces.trees.math.MathFunction;
import aitoa.structure.IRepresentationMapping;

/**
 * The representation mapping for scheduling formulas to Gantt
 * charts. The idea is that the input of this mapping is a
 * formula which is used to step-by-step construct a Gantt chart.
 * In each step, the formula receives information about the
 * current construction state of the Gantt chart. It then
 * computes one rating value for each job that may be scheduled.
 * The job with the smallest rating value is then scheduled. The
 * Gantt chart is updated. This is repeated again and again,
 * until the Gantt chart has been filled and no job is left that
 * might be scheduled. If more than one job receive the smallest
 * rating, we randomly choose one of them.
 */
// start relevant
public final class JSSPTreeRepresentationMapping implements
    IRepresentationMapping<Node[], JSSPCandidateSolution> {
// end relevant

  /** the job id */
  static final int JOB_ID = 0;
  /** the next sub-job of the job */
  static final int JOB_COMPLETED_SUBJOBS =
      (JSSPTreeRepresentationMapping.JOB_ID + 1);
  /** the index of the working time of the next sub job */
  static final int JOB_NEXT_SUBJOB_WORK_TIME =
      JSSPTreeRepresentationMapping.JOB_COMPLETED_SUBJOBS + 1;
  /**
   * the time index when the last sub-job of the current job was
   * finished
   */
  static final int JOB_LAST_SUBJOB_FINISHED_TIME =
      JSSPTreeRepresentationMapping.JOB_NEXT_SUBJOB_WORK_TIME
          + 1;
  /** the work time already invested into the job */
  static final int JOB_FINISHED_WORKTIME =
      JSSPTreeRepresentationMapping.JOB_LAST_SUBJOB_FINISHED_TIME
          + 1;
  /** the total work time that needs to be invested in the job */
  static final int JOB_TOTAL_WORKTIME =
      JSSPTreeRepresentationMapping.JOB_FINISHED_WORKTIME + 1;
  /**
   * the machine id
   */
  static final int MACHINE_ID =
      JSSPTreeRepresentationMapping.JOB_TOTAL_WORKTIME + 1;
  /**
   * the time index when the last sub-job was finished on the
   * current machine
   */
  static final int MACHINE_LAST_SUBJOB_FINISHED_TIME =
      JSSPTreeRepresentationMapping.MACHINE_ID + 1;
  /** the number of sub-jobs a machine has completed */
  static final int MACHINE_COMPLETED_SUBJOBS =
      JSSPTreeRepresentationMapping.MACHINE_LAST_SUBJOB_FINISHED_TIME
          + 1;
  /** the work time already performed by the machine */
  static final int MACHINE_FINISHED_WORKTIME =
      JSSPTreeRepresentationMapping.MACHINE_COMPLETED_SUBJOBS
          + 1;
  /**
   * the total work time that needs to be spent on this machine
   */
  static final int MACHINE_TOTAL_WORKTIME =
      JSSPTreeRepresentationMapping.MACHINE_FINISHED_WORKTIME
          + 1;

  /** the values dimensions */
  static final int DIM_VALUES =
      JSSPTreeRepresentationMapping.MACHINE_TOTAL_WORKTIME + 1;

  /** the state used by the mathematical functions */
  final double[] m_state;

  /** the jobs data from the instance */
  final int[][] m_jobs;
  /** the total work time required by the job */
  final int[] m_jobTotalTime;
  /** the total work time required by the machine */
  final int[] m_machineTotalTime;

  /** the job ids */
  final int[] m_jobIDs;
  /** the best jobs ids */
  final int[] m_bestJobIDs;
  /** the best jobs indexes */
  final int[] m_bestJobIndexes;
  /** the steo if the job */
  final int[] m_jobCompletedSubjobs;
  /** the next job machine */
  final int[] m_jobNextMachine;
  /** the last time a sub-job of this job was finished */
  final int[] m_jobLastSubjobFinishedTime;
  /** the the work time already performed by the job */
  final int[] m_jobFinishedWorkTime;

  /** the time the machine has spent on processing jobs */
  final int[] m_machineFinishedWorkTime;
  /** the index of the machine into the solution */
  final int[] m_machineLastSubjobFinishedTime;
  /** the number of subjobs processed by the machine */
  final int[] m_machineCompletedSubjobs;

  /**
   * create the representation
   *
   * @param instance
   *          the problem instance
   */
  public JSSPTreeRepresentationMapping(
      final JSSPInstance instance) {
    super();

    this.m_jobs = instance.jobs;
    this.m_state =
        new double[JSSPTreeRepresentationMapping.DIM_VALUES];

    this.m_jobTotalTime = new int[instance.n];
    this.m_machineTotalTime = new int[instance.m];
    for (int i = instance.n; (--i) >= 0;) {
      final int[] job = this.m_jobs[i];
      for (int k = 0; k < instance.m;) {
        final int machine = job[k++];
        final int time = job[k++];
        this.m_jobTotalTime[i] += time;
        this.m_machineTotalTime[machine] += time;
      }
    }

    this.m_jobIDs = new int[instance.n];
    this.m_bestJobIDs = new int[instance.n];
    this.m_bestJobIndexes = new int[instance.n];
    this.m_jobCompletedSubjobs = new int[instance.n];
    this.m_jobNextMachine = new int[instance.n];
    this.m_jobLastSubjobFinishedTime = new int[instance.n];
    this.m_jobFinishedWorkTime = new int[instance.n];

    this.m_machineFinishedWorkTime = new int[instance.m];
    this.m_machineLastSubjobFinishedTime = new int[instance.m];
    this.m_machineCompletedSubjobs = new int[instance.m];
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return this.getClass().getCanonicalName();
  }

// start relevant
  /**
   * Map a point {@code x} from the search space (here a formula
   * describing how job should be prioritized based on the
   * current scheduling state) to a candidate solution {@code y}
   * in the solution space (here a Gantt chart).
   *
   * @param random
   *          a random number generator
   * @param x
   *          the point in the search space
   * @param y
   *          the solution record, i.e., the Gantt chart
   */
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void map(final Random random, final Node[] x,
      final JSSPCandidateSolution y) {
    final MathFunction<double[]> func = ((MathFunction) (x[0]));

    final int n = this.m_jobIDs.length;
    final int m = this.m_machineLastSubjobFinishedTime.length;

    for (int i = n; (--i) >= 0;) {
      this.m_jobIDs[i] = i;
    }
    Arrays.fill(this.m_jobCompletedSubjobs, 0);
    Arrays.fill(this.m_jobNextMachine, 0);
    Arrays.fill(this.m_jobLastSubjobFinishedTime, 0);
    Arrays.fill(this.m_jobFinishedWorkTime, 0);
    Arrays.fill(this.m_machineFinishedWorkTime, 0);
    Arrays.fill(this.m_machineLastSubjobFinishedTime, 0);
    Arrays.fill(this.m_machineCompletedSubjobs, 0);
    final double[] state = this.m_state;

    for (int count = n; count > 0;) {
// now we have computed all necessary statistics, so we can
// actually apply the formula and select the job to schedule next
      double bestScore = Double.POSITIVE_INFINITY;
      int bestCount = 0;

      if (count > 1) {
        // now we need to find the job index to execute
        for (int i = count; (--i) >= 0;) {
          final int job = this.m_jobIDs[i];
          final int next = this.m_jobCompletedSubjobs[job];
          final int machine = this.m_jobs[job][next << 1];
          final int time = this.m_jobs[job][1 + (next << 1)];

          state[JSSPTreeRepresentationMapping.JOB_ID] = job;
          state[JSSPTreeRepresentationMapping.JOB_COMPLETED_SUBJOBS] =
              next;
          state[JSSPTreeRepresentationMapping.JOB_NEXT_SUBJOB_WORK_TIME] =
              time;
          state[JSSPTreeRepresentationMapping.JOB_LAST_SUBJOB_FINISHED_TIME] =
              this.m_jobLastSubjobFinishedTime[job];
          state[JSSPTreeRepresentationMapping.JOB_FINISHED_WORKTIME] =
              this.m_jobFinishedWorkTime[job];
          state[JSSPTreeRepresentationMapping.JOB_TOTAL_WORKTIME] =
              this.m_jobTotalTime[job];
          state[JSSPTreeRepresentationMapping.MACHINE_ID] =
              machine;
          state[JSSPTreeRepresentationMapping.MACHINE_LAST_SUBJOB_FINISHED_TIME] =
              this.m_machineLastSubjobFinishedTime[machine];
          state[JSSPTreeRepresentationMapping.MACHINE_COMPLETED_SUBJOBS] =
              this.m_machineCompletedSubjobs[machine];
          state[JSSPTreeRepresentationMapping.MACHINE_FINISHED_WORKTIME] =
              this.m_machineFinishedWorkTime[machine];
          state[JSSPTreeRepresentationMapping.MACHINE_TOTAL_WORKTIME] =
              this.m_machineTotalTime[machine];

          // compute the score for the job
          final double score = func.applyAsDouble(this.m_state);
          if (score < bestScore) {
            bestCount = 1;
            this.m_bestJobIDs[0] = job;
            this.m_bestJobIndexes[0] = i;
            bestScore = score;
          } else {
            if (score == bestScore) {
              this.m_bestJobIDs[bestCount] = job;
              this.m_bestJobIndexes[bestCount] = i;
              ++bestCount;
            }
          }
        }
      }

      if (bestCount == 0) {
        this.m_bestJobIndexes[0] = random.nextInt(count);
        this.m_bestJobIDs[0] =
            this.m_jobIDs[this.m_bestJobIndexes[0]];
      } else {
        bestCount = random.nextInt(bestCount);
      }

      // ok, we have selected a job to execute, now we need to
      // execute it and update the data
      final int bestJob = this.m_bestJobIDs[bestCount];
      int next = this.m_jobCompletedSubjobs[bestJob];
      final int machine = this.m_jobs[bestJob][next << 1];
      final int time = this.m_jobs[bestJob][1 + (next << 1)];

      this.m_jobCompletedSubjobs[bestJob] = (++next);
      if (next >= m) {
        final int bestJobIndex =
            this.m_bestJobIndexes[bestCount];
        this.m_jobIDs[bestJobIndex] = this.m_jobIDs[--count];
        this.m_jobIDs[count] = -1;
      }

      final int beginTime =
          Math.max(this.m_jobLastSubjobFinishedTime[bestJob],
              this.m_machineLastSubjobFinishedTime[machine]);
      final int endTime = beginTime + time;
      this.m_machineLastSubjobFinishedTime[machine] = endTime;
      this.m_jobLastSubjobFinishedTime[bestJob] = endTime;
      this.m_jobFinishedWorkTime[bestJob] += time;
      int machineNext =
          this.m_machineCompletedSubjobs[machine]++;
      this.m_machineFinishedWorkTime[machine] += time;

      // store everything in the schedule
      machineNext *= 3;
      y.schedule[machine][machineNext++] = bestJob;
      y.schedule[machine][machineNext++] = beginTime;
      y.schedule[machine][machineNext++] = endTime;
    }
  }
}
// end relevant
