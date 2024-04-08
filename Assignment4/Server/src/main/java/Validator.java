import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

  /**
   * URL pattern for the project:
   * url: http://35.94.152.46:8080/Server-1.0-SNAPSHOT/skiers
   * url: http://LB-2-Server-2104101047.us-west-2.elb.amazonaws.com/Server-1.0-SNAPSHOT/skiers
   * url: http://LB-4-Server-2104101047.us-west-2.elb.amazonaws.com/Server-1.0-SNAPSHOT/skiers
   *
   * Please note data are in body of the request, therefore there are no parameters in the URL
   *
   */
  private static final String URL_PATTERN =
      "^http(s)?://(\\w+\\.)*(\\w+):(\\d+)/(\\w+(-\\d+\\.\\d+-SNAPSHOT)?)/(\\w+)$";

  public static boolean isValidUrl(String url) {
    Pattern pattern = Pattern.compile(URL_PATTERN);
    Matcher matcher = pattern.matcher(url);
    return matcher.matches();
  }

  /**
   * Check if the input string is a valid JSON
   * @param jsonInString
   * @return true if the input string is a valid JSON, false otherwise
   */

  public static boolean isValidJson(String jsonInString) {
    try {
      new Gson().fromJson(jsonInString, Object.class);
      return true;
    } catch (JsonSyntaxException ex) {
      return false;
    }
  }

  /**
   * Check if the input parameters are valid
   * @param liftRideEvent
   *
   * skierID - between 1 and 100000
   * resortID - between 1 and 10
   * liftID - between 1 and 40
   * seasonID - 2024
   * dayID - 1
   * time - between 1 and 360
   *
   * @return true if the input parameters are valid, false otherwise
   */
  public static boolean isValidParams(LiftRideEvent liftRideEvent) {
    if (liftRideEvent.getSkierID() == null || liftRideEvent.getResortID() == null
        || liftRideEvent.getLiftID() == null || liftRideEvent.getSeasonID() == null
        || liftRideEvent.getDayID() == null || liftRideEvent.getTime() == null) {
      return false;
    }

    if (liftRideEvent.getSkierID() < 1 || liftRideEvent.getSkierID() > 100000
        || liftRideEvent.getDayID() != 1 || liftRideEvent.getSeasonID() != 2024
        || liftRideEvent.getLiftID() < 1 || liftRideEvent.getLiftID()  > 40 || liftRideEvent.getTime() < 1
        || liftRideEvent.getTime() > 360 || liftRideEvent.getLiftID() < 1 || liftRideEvent.getLiftID() > 40) {
      return false;
    }
    return true;
  }

}
