import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class MqChannelPool {

  private static final String HOST = YamlConfigLoader.getRabbitMqHost();
  private static final int PORT = YamlConfigLoader.getRabbitMqPort();
  private static ObjectPool<Channel> channelPool;

  static {
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(HOST);
      factory.setPort(PORT);
      factory.setUsername("admin");
      factory.setPassword("admin");
      Connection connection = factory.newConnection();
      channelPool = new GenericObjectPool<>(new ChannelFactory(connection));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Channel borrowChannel() throws Exception {
    return channelPool.borrowObject();
  }

  public static void returnChannel(Channel channel) {
    try {
      channelPool.returnObject(channel);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
