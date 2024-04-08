import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientPoolUtil {
  private static final CloseableHttpClient httpClient;

  static {
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(1000);
    cm.setDefaultMaxPerRoute(1000);

    httpClient = HttpClients.custom()
        .setConnectionManager(cm)
        .build();
  }

  public static CloseableHttpClient getHttpClient() {
    return httpClient;
  }
}
