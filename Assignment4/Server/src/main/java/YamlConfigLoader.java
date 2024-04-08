import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class YamlConfigLoader {
  private static final String YAML_FILE = "config.yml";
  private static Map<String, Object> properties;

  static {
    Yaml yaml = new Yaml();
    try (InputStream in = YamlConfigLoader.class.getClassLoader().getResourceAsStream(YAML_FILE)) {
      if (in == null) {
        throw new RuntimeException(YAML_FILE + " not found on classpath");
      }
      properties = yaml.load(in);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load configuration from " + YAML_FILE, e);
    }
  }

  public static String getRabbitMqHost() {
    return (String) ((Map<String, Object>) properties.get("rabbitmq")).get("host");
  }

  public static int getRabbitMqPort() {
    return (int) ((Map<String, Object>) properties.get("rabbitmq")).get("port");
  }

  public static String getRabbitMqQueueName() {
    return (String) ((Map<String, Object>) properties.get("rabbitmq")).get("queueName");
  }

  public static String getRedisHost() {
    return (String) ((Map<String, Object>) properties.get("redis")).get("host");
  }

  public static int getRedisPort() {
    return (int) ((Map<String, Object>) properties.get("redis")).get("port");
  }

  public static boolean isCircuitBreakerEnabled() {
    return (boolean) ((Map<String, Object>) properties.get("circuitBreaker")).get("enabled");
  }

  public static int getCircuitBreakerOpeningThreshold() {
    return (int) ((Map<String, Object>) properties.get("circuitBreaker")).get("openingThreshold");
  }

  public static int getCircuitBreakerClosingThreshold() {
    return (int) ((Map<String, Object>) properties.get("circuitBreaker")).get("closingThreshold");
  }
}
