import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class RedisDatabaseService implements DatabaseService {

  private JedisPool jedisPool;

  public RedisDatabaseService(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }

  @Override
  public void saveRecord(String skierId, String skiDay, String liftId, String time, String resortId, String seasonId) {
    try (Jedis jedis = jedisPool.getResource()) {
      Pipeline pipeline = jedis.pipelined();

      // Key format for skier activity: resort:[resortId]:season:[seasonId]:day:[day]:skiers
      String skiersDayKey = String.format("resort:%s:season:%s:day:%s:skiers", resortId, seasonId, skiDay);
      pipeline.sadd(skiersDayKey, skierId);

      // Key for individual skier's record on a specific day
      String skierRecordKey = String.format("skier:%s:resort:%s:season:%s:day:%s", skierId, resortId, seasonId, skiDay);
      int verticalIncrease = Integer.parseInt(liftId) * 10;
      pipeline.incrBy(skierRecordKey, verticalIncrease);

      // Key for tracking total vertical per skier
      String verticalKey = String.format("skier:%s:vertical", skierId);
      pipeline.incrBy(verticalKey, verticalIncrease);

      pipeline.sync();
    }
  }

  public long getSkiersForDay(String resortId, String seasonId, String skiDay) {
    try (Jedis jedis = jedisPool.getResource()) {
      String skiersDayKey = String.format("resort:%s:season:%s:day:%s:skiers", resortId, seasonId, skiDay);
      return jedis.scard(skiersDayKey);
    }
  }

  public long getSkierDayVertical(String skierId, String resortId, String seasonId, String skiDay) {
    try (Jedis jedis = jedisPool.getResource()) {
      String skierRecordKey = String.format("skier:%s:resort:%s:season:%s:day:%s", skierId, resortId, seasonId, skiDay);
      String vertical = jedis.get(skierRecordKey);
      return vertical != null ? Long.parseLong(vertical) : 0;
    }
  }

  public long getTotalSkierVertical(String skierId) {
    try (Jedis jedis = jedisPool.getResource()) {
      String verticalKey = String.format("skier:%s:vertical", skierId);
      String vertical = jedis.get(verticalKey);
      return vertical != null ? Long.parseLong(vertical) : 0;
    }
  }

}

