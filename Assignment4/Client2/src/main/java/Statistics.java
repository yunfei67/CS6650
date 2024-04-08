import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {
  private final AtomicInteger successRequestCount;
  private final AtomicInteger failedRequestCount;

  public Statistics() {
    this.successRequestCount = new AtomicInteger(0);
    this.failedRequestCount = new AtomicInteger(0);
  }

  public synchronized void incrementSuccessRequestCount() {
    successRequestCount.incrementAndGet();
  }
  public synchronized void decrementSuccessRequestCount() {
    successRequestCount.decrementAndGet();
  }

  public synchronized void incrementFailedRequestCount() {
    failedRequestCount.incrementAndGet();
  }

  public static void calculateAndPrintStats(List<RequestData> reqDataList) {

    long totalTime = reqDataList.get(reqDataList.size() - 1).getStartTime() - reqDataList.get(0).getStartTime();
    reqDataList.sort(Comparator.comparingLong(rd -> rd.getLatency()));

    long totalLatency = reqDataList.stream().mapToLong(rd -> rd.getLatency()).sum();
    long meanLatency = totalLatency / reqDataList.size();
    long medianLatency = reqDataList.get(reqDataList.size() / 2).getLatency();
    long p99Latency = reqDataList.get((int) (reqDataList.size() * 0.99)).getLatency();
    long minLatency = reqDataList.get(0).getLatency();
    long maxLatency = reqDataList.get(reqDataList.size() - 1).getLatency();
    long throughput = (long) reqDataList.size() * 1000 / totalTime;

    System.out.println("Mean response time: " + meanLatency + " ms");
    System.out.println("Median response time: " + medianLatency + " ms");
    System.out.println("p99 response time: " + p99Latency + " ms");
    System.out.println("Min response time: " + minLatency + " ms");
    System.out.println("Max response time: " + maxLatency + " ms");
    System.out.println("****** Results ******");
//    System.out.println("Throughput: " + throughput + " requests/second");
  }

  public Integer getSuccessRequestCount() {
    return successRequestCount.get();
  }

  public Integer getFailedRequestCount() {
    return failedRequestCount.get();
  }

}
