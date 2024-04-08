import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DataProcessor {
  private static final String FILE_NAME = YamlConfigLoader.getOutputFileName();

  public static synchronized void writeDataToCSV(List<RequestData> requestDataList) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
      for (RequestData rd : requestDataList) {
        writer.write(String.format("%d,%s,%d,%d\n", rd.getStartTime(), rd.getRequestType(), rd.getLatency(), rd.getResponseCode()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void clearData() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, false))) {
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
