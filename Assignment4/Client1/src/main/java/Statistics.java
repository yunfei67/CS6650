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

  public Integer getSuccessRequestCount() {
    return successRequestCount.get();
  }

  public Integer getFailedRequestCount() {
    return failedRequestCount.get();
  }

}
