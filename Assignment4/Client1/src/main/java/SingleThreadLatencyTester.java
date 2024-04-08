import com.google.gson.Gson;
import com.sun.nio.sctp.IllegalReceiveException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

public class SingleThreadLatencyTester {

  private final static Statistics statistics = new Statistics();
  private final static Integer retryTimes = YamlConfigLoader.getRetryTimes();
  private final static String url= YamlConfigLoader.getServerUrl();


  public static void main(String[] args) {
    // event producer

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < 10000; i++) {
      LiftRideEvent event = LiftRideEventFactory.createRandomLiftRideEvent();
      try {
        postLiftRide(event);
        statistics.incrementSuccessRequestCount();
      } catch (IOException e) {
        statistics.incrementFailedRequestCount();
        throw new IllegalReceiveException("Failed to post lift ride event!");
      }
    }

    long endTime = System.currentTimeMillis();
    System.out.println("Single Thread Latency Test Finished!");
    System.out.println("Success count: " + statistics.getSuccessRequestCount());
    System.out.println("Failed count: " + statistics.getFailedRequestCount());
    System.out.println("Total time: " + (endTime - startTime) + "ms");
    System.out.println("Calculated latency: " + (endTime - startTime) / 10000.0 + "ms");

  }

  private synchronized static void postLiftRide(LiftRideEvent event) throws IOException {
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

    for (int i = 0; i < 5; i++) {
      try {
        startTime = System.currentTimeMillis(); // Record the start time
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json; utf-8");
        post.setHeader("Accept", "application/json");
        post.setEntity(new StringEntity(new Gson().toJson(event))); // Set the request body

        HttpResponse response = HttpClientPoolUtil.getHttpClient().execute(post);
        responseCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());
        endTime = System.currentTimeMillis();
        latency = endTime - startTime;

        if (responseCode == HttpURLConnection.HTTP_OK
            || responseCode == HttpURLConnection.HTTP_CREATED) {
          break;
        } else if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
            && responseCode < HttpURLConnection.HTTP_INTERNAL_ERROR && i < retryTimes - 1) {
          continue;
        } else {
          throw new IOException("Unexpected response code: " + responseCode);
        }
      } catch (Exception e) {
        endTime = System.currentTimeMillis();
        latency = endTime - startTime;
        if (i == retryTimes - 1) {
          e.printStackTrace();
        }
      }
    }
  }

}
