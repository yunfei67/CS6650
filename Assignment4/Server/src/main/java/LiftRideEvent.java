public class LiftRideEvent {
  private Integer skierID;
  private Integer resortID;
  private Integer liftID;
  private Integer seasonID;
  private Integer dayID;
  private Integer time;

  public LiftRideEvent(Integer skierID, Integer resortID, Integer liftID, Integer seasonID,
      Integer dayID, Integer time) {
    this.skierID = skierID;
    this.resortID = resortID;
    this.liftID = liftID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.time = time;
  }

  public Integer getSkierID() {
    return skierID;
  }

  public Integer getResortID() {
    return resortID;
  }

  public Integer getLiftID() {
    return liftID;
  }

  public Integer getSeasonID() {
    return seasonID;
  }

  public Integer getDayID() {
    return dayID;
  }

  public Integer getTime() {
    return time;
  }

  @Override
  public String toString() {
    return "LiftRideEvent{" +
        "skierID=" + skierID +
        ", resortID=" + resortID +
        ", liftID=" + liftID +
        ", seasonID=" + seasonID +
        ", dayID=" + dayID +
        ", time=" + time +
        '}';
  }
}
