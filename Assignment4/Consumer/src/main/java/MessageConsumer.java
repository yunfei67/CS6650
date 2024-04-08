import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;


public class MessageConsumer {
  private final Channel channel;
  private final DatabaseService databaseService;
  public MessageConsumer(Channel channel, DatabaseService databaseService) {
    this.channel = channel;
    this.databaseService = databaseService;
  }

  public void consume(String QUEUE_NAME) {
    Gson gson = new Gson();
    try {
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");

        LiftRideEvent liftRideEvent = gson.fromJson(message, LiftRideEvent.class);
        databaseService.saveRecord(liftRideEvent.getSkierID().toString(), liftRideEvent.getDayID().toString(),
            liftRideEvent.getLiftID().toString(), liftRideEvent.getTime().toString(),
            liftRideEvent.getResortID().toString(), liftRideEvent.getSeasonID().toString());

      };
      channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
