import com.google.gson.Gson;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

public class EventConsumerThread extends Thread {
    private final Integer numRequests;
    private final Integer retryTimes;
    private final String url;
    private final CountDownLatch latch1;
    private final CountDownLatch latch2;

    private final Statistics statistics;

    private final BlockingQueue<LiftRideEvent> eventQueue;

    public EventConsumerThread(int numRequests, int retryTimes, String url, CountDownLatch latch1, CountDownLatch latch2,
        Statistics statistics, BlockingQueue<LiftRideEvent> eventQueue) {
      this.numRequests = numRequests;
      this.retryTimes = retryTimes;
      this.url = url;
      this.latch1 = latch1;
      this.latch2 = latch2;
      this.statistics = statistics;
      this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
      for (int i = 0; i < numRequests; i++) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        try {
          LiftRideEvent event = eventQueue.take();
          postLiftRide(event);
          statistics.incrementSuccessRequestCount();
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
          statistics.incrementFailedRequestCount();
        }
      }

      latch1.countDown();
      latch2.countDown();
    }

    private void postLiftRide(LiftRideEvent event) throws IOException {
      long startTime = 0;
      long endTime;
      int responseCode = 0;
      long latency = 0;

      if (event.getSkierID() == 0) {
        // backtrack
        statistics.decrementSuccessRequestCount();
        Thread.currentThread().interrupt();
        return;
      }

      System.out.println("Posting: " + event.toString());

      for (int i = 0; i < retryTimes; i++) {
        try {
          startTime = System.currentTimeMillis(); // Record the start time
          HttpPost post = new HttpPost(url);
          post.setHeader("Content-Type", "application/json; utf-8");
          post.setHeader("Accept", "application/json");
          post.setEntity(new StringEntity(new Gson().toJson(event))); // Set the request body

          HttpResponse response = HttpClientPoolUtil.getHttpClient().execute(post);
          responseCode = response.getStatusLine().getStatusCode();
          EntityUtils.consume(response.getEntity()); // Ensure the response entity is fully consumed
          endTime = System.currentTimeMillis(); // Record the end time
          latency = endTime - startTime; // Calculate the latency

          // Check the response code
          if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            break;
          } else if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST && responseCode <= HttpURLConnection.HTTP_INTERNAL_ERROR && i < retryTimes - 1) {
            continue;
          } else {
            throw new IOException("Unexpected response code: " + responseCode);
          }
        } catch (Exception e) {
          endTime = System.currentTimeMillis();
          latency = endTime - startTime; // Calculate latency even in case of exception
          if (i == retryTimes - 1) {
            e.printStackTrace();
          }
        } finally {
          Client.requestRecords.add(new RequestData(startTime, "POST", latency, responseCode));
        }
      }

    }
}
