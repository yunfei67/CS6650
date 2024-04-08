import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class MainApplication {
  private static final String QUEUE_NAME = "SkierQueue";
  private static final String RABBITMQ_USERNAME = "admin";
  private static final String RABBITMQ_PASSWORD = "admin";
  private static final String RABBITMQ_HOST = "172.31.31.153";
  private static final int RABBITMQ_PORT = 5672;
  private static final String REDIS_HOST = "172.31.26.164";
  private static final int REDIS_PORT = 6379;
  private static final int THREADS = 300;

  public static void main(String[] args) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBITMQ_HOST);
    factory.setPort(RABBITMQ_PORT);
    factory.setUsername(RABBITMQ_USERNAME);
    factory.setPassword(RABBITMQ_PASSWORD);

    ExecutorService executor = Executors.newFixedThreadPool(THREADS);

    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(THREADS);
    poolConfig.setMaxIdle(50);
    JedisPool jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);

    try (Connection connection = factory.newConnection()) {
      for (int i = 0; i < THREADS; i++) {
        DatabaseService databaseService = new RedisDatabaseService(jedisPool);
        executor.submit(() -> {
          try {
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            MessageConsumer consumer = new MessageConsumer(channel, databaseService);
            consumer.consume(QUEUE_NAME);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
        executor.awaitTermination(6000, TimeUnit.SECONDS);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
