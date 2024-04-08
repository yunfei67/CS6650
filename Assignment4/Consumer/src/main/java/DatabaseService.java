public interface DatabaseService {
  void saveRecord(String skierId, String skiDay, String liftId, String time, String resortId, String seasonId);
  long getSkiersForDay(String resortId, String seasonId, String skiDay);
  long getSkierDayVertical(String skierId, String resortId, String seasonId, String skiDay);
  long getTotalSkierVertical(String skierId);

}
