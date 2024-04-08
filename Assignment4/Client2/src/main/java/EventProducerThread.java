import java.util.concurrent.BlockingQueue;

public class EventProducerThread implements Runnable {
  private final BlockingQueue<LiftRideEvent> eventQueue;
  private final int totalEvents;
  private int terminateEvents;

  public EventProducerThread(BlockingQueue<LiftRideEvent> eventQueue, int totalEvents) {
    this.eventQueue = eventQueue;
    this.totalEvents = totalEvents;
  }

  public EventProducerThread(BlockingQueue<LiftRideEvent> eventQueue, int totalEvents,
      int terminateEvents) {
    this.eventQueue = eventQueue;
    this.totalEvents = totalEvents;
    this.terminateEvents = terminateEvents;
  }

  @Override
  public void run() {
    for (int i = 0; i < totalEvents; i++) {
      LiftRideEvent event = LiftRideEventFactory.createRandomLiftRideEvent();
      try {
        eventQueue.put(event);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    for (int i = 0; i < terminateEvents; i++) {
      LiftRideEvent event = LiftRideEventFactory.createTerminateEvent();
      try {
        eventQueue.put(event);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
