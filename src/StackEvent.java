import java.time.Duration;
import java.time.Instant;

public class StackEvent {
    private String eventMethod;
    private String returnValue;
    private Instant startTime;
    private Instant endTime;
    private long timeTaken;

    public StackEvent() {
        this.startTime = Instant.now();
    }

    public String getEventMethod() {
        return eventMethod;
    }

    public void setEventMethod(String eventMethod) {
        this.eventMethod = eventMethod;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.endTime = Instant.now();
        this.timeTaken = Duration.between(this.startTime, this.endTime).toMillis();
        this.returnValue = returnValue;
    }

    public long getTime() {
        return this.timeTaken;
    }

    public String toString() {
        return this.eventMethod;
    }
}