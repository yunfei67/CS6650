import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@WebServlet(urlPatterns = {"/*"})
public class Servlet extends HttpServlet {
  private ExecutorService executorService;
  private EventCountCircuitBreaker circuitBreaker;
  private boolean circuitBreakerEnabled;
  private DatabaseService databaseService;

  @Override
  public void init(){
    executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    circuitBreakerEnabled = YamlConfigLoader.isCircuitBreakerEnabled();

    // Tomcat has a default limit of 10 threads, so we set the jedisPool maxTotal to 10
    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setMaxTotal(10);
    jedisPoolConfig.setMaxIdle(10);
    JedisPool jedisPool = new JedisPool(jedisPoolConfig, YamlConfigLoader.getRedisHost(), YamlConfigLoader.getRedisPort());
    databaseService = new RedisDatabaseService(jedisPool);

    if (circuitBreakerEnabled) {
      int openingThreshold = YamlConfigLoader.getCircuitBreakerOpeningThreshold();
      int closingThreshold = YamlConfigLoader.getCircuitBreakerClosingThreshold();
      circuitBreaker = new EventCountCircuitBreaker(openingThreshold, 100, TimeUnit.MILLISECONDS, closingThreshold);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String[] pathParts = pathInfo.split("/");

    response.setContentType("application/json");

    try {
      if (pathParts.length == 8 && "resorts".equals(pathParts[1]) && "seasons".equals(pathParts[3]) && "days".equals(pathParts[5])) {

        // GET1: /resorts/{resortID}/seasons/{seasonID}/days/{dayID}/skiers
        long skierCnt = databaseService.getSkiersForDay(pathParts[2], pathParts[4], pathParts[6]);
        response.getWriter().write(new Gson().toJson(skierCnt));
      } else if (pathParts.length == 9 && "skiers".equals(pathParts[1]) && "seasons".equals(pathParts[3]) && "days".equals(pathParts[5]) && "skiers".equals(pathParts[7])) {

        // GET2: /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        long vertical = databaseService.getSkierDayVertical(pathParts[8], pathParts[2], pathParts[4], pathParts[6]);
        response.getWriter().write(new Gson().toJson(vertical));
      } else if (pathParts.length == 4 && "skiers".equals(pathParts[1])) {

        // GET3: /skiers/{skierID}/vertical
        long totalVertical = databaseService.getTotalSkierVertical(pathParts[2]);
        response.getWriter().write(new Gson().toJson(totalVertical));

      } else {
        // Invalid URL parameters
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": \"Invalid URL parameters\"}");
      }
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");

    if (circuitBreakerEnabled && !circuitBreaker.checkState()) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      response.getWriter().write("Service is unavailable due to high load!");
      return;
    }

    StringBuilder sb = new StringBuilder();
    String line;

    try {
      while ((line = request.getReader().readLine()) != null) {
        sb.append(line);
      }

      executorService.submit(() -> {
        Channel channel = null;
        try {
          channel = MqChannelPool.borrowChannel();
          String queueName = YamlConfigLoader.getRabbitMqQueueName();
          channel.basicPublish("", queueName, null, sb.toString().getBytes());

          if (circuitBreakerEnabled) {
            circuitBreaker.incrementAndCheckState();
          }

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          if (channel != null) {
            MqChannelPool.returnChannel(channel);
          }
        }
      });

      response.setStatus(HttpServletResponse.SC_OK);
    } catch (IOException | JsonSyntaxException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid input!");
    }
  }
}
