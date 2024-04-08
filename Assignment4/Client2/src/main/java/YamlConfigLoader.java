import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class YamlConfigLoader {
  private static final String YAML_FILE = "/config.yml";
  private static Map<String, Object> properties;

  static {
    Yaml yaml = new Yaml();
    try (InputStream in = YamlConfigLoader.class.getResourceAsStream(YAML_FILE)) {
      properties = yaml.load(in);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load configuration from " + YAML_FILE, e);
    }
  }

  public static String getServerUrl() {
    Map<String, String> server = (Map<String, String>) properties.get("server");
    return server.get("url");
  }

  public static int getPhaseOneThreadsNum() {
    return (int) ((Map<String, Object>) properties.get("appConfig")).get("phaseOneThreadsNum");
  }

  public static int getPhaseTwoThreadsNum() {
    return (int) ((Map<String, Object>) properties.get("appConfig")).get("phaseTwoThreadsNum");
  }

  public static int getRetryTimes() {
    return (int) ((Map<String, Object>) properties.get("appConfig")).get("retryTimes");
  }

  public static String getOutputFileName() {
    return (String) ((Map<String, Object>) properties.get("output")).get("outputFileName");
  }
}
