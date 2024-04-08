import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {

  private static final String SERVER_URL = YamlConfigLoader.getServerUrl();
  private static final int PHASE_ONE_THREADS_NUM = YamlConfigLoader.getPhaseOneThreadsNum();;
  private static  final int NUM_REQUESTS_PER_THREAD = YamlConfigLoader.getPhaseOneThreadsNum();;
  private static final int PHASE_TWO_THREADS_NUM = YamlConfigLoader.getPhaseTwoThreadsNum();
  private static final int RETRY_TIMES = YamlConfigLoader.getRetryTimes();
  private static final int TOTAL_EVENTS = 200000;
  private static final int DEFAULT_EVENT_POOL_SIZE = 300000;

  // variable for phase 2

  public static void main(String[] args) {
    BlockingQueue<LiftRideEvent> eventQueue = new LinkedBlockingQueue<>(DEFAULT_EVENT_POOL_SIZE);
    ExecutorService executor = Executors.newCachedThreadPool();

    long startTime = System.currentTimeMillis();

    CountDownLatch latch1 = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(PHASE_ONE_THREADS_NUM + PHASE_TWO_THREADS_NUM);
    Statistics statistics = new Statistics();

    // phase 1
    executor.submit(new EventProducerThread(eventQueue, TOTAL_EVENTS, PHASE_ONE_THREADS_NUM + PHASE_TWO_THREADS_NUM));

    for (int i = 0; i < PHASE_ONE_THREADS_NUM; i++) {
      executor.execute(new EventConsumerThread(NUM_REQUESTS_PER_THREAD, RETRY_TIMES, SERVER_URL, latch1, latch2,
          statistics, eventQueue));
    }

    try {
      latch1.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // phase 2
    for (int i = 0; i < PHASE_TWO_THREADS_NUM; i++) {
      executor.execute(new EventConsumerThread(Integer.MAX_VALUE, RETRY_TIMES, SERVER_URL, latch1, latch2,
          statistics, eventQueue));
    }

    try {
      latch2.await();

      long endTime = System.currentTimeMillis();
      System.out.println("*** Results ***");
      System.out.println("Phase2 Threads number (after one of the 32 threads finished in Phase1) : " + PHASE_TWO_THREADS_NUM + " threads");
      System.out.println("Number of successful requests sent: " + statistics.getSuccessRequestCount());
      System.out.println("Number of unsuccessful requests sent: " + statistics.getFailedRequestCount());
      System.out.println("Total run time: " + (endTime - startTime) + "ms");
      System.out.println("Total Throughput: " + (int) (TOTAL_EVENTS / ((endTime - startTime) / 1000.0)) + " requests per second");

      executor.shutdown();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }
}
