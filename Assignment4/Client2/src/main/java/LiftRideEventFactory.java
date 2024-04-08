import java.util.concurrent.ThreadLocalRandom;

public class LiftRideEventFactory {
  private static final Integer DEFAULT_SEASON_ID = 2024;
  private static final Integer DEFAULT_DAY_ID = 1;

  public static LiftRideEvent createRandomLiftRideEvent() {
    Integer skierID = ThreadLocalRandom.current().nextInt(1, 100001);
    Integer resortID = ThreadLocalRandom.current().nextInt(1, 11);
    Integer liftID = ThreadLocalRandom.current().nextInt(1, 41);
    Integer seasonID = DEFAULT_SEASON_ID;
    Integer DayID = DEFAULT_DAY_ID;
    Integer time = ThreadLocalRandom.current().nextInt(1, 360);

    return new LiftRideEvent(skierID, resortID, liftID, seasonID, DayID, time);
  }

  public static LiftRideEvent createTerminateEvent() {
    // use 0 to indicate the end of the event stream
    Integer skierID = 0;
    Integer resortID = 0;
    Integer liftID = 0;
    Integer seasonID = 0;
    Integer DayID = 0;
    Integer time = 0;

    return new LiftRideEvent(skierID, resortID, liftID, seasonID, DayID, time);
  }

}
