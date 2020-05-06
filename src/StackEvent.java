import java.time.Duration;
import java.time.Instant;

/**
 * A data class to hold stack event information.
 */
public class StackEvent {
    private String eventMethod;
    private String returnValue;
    private Instant startTime;
    private Instant endTime;
    private long timeTaken;
    private String thread;

    /**
     * @param method the method name.
     * @param thread the thread name.
     */
    public StackEvent(String method, String thread) {
        this.startTime = Instant.now();
        this.eventMethod = method;
        this.thread = thread;
    }

    /**
     * Gets the event method name.
     * 
     * @return method name
     */
    public String getEventMethod() {
        return eventMethod;
    }

    /**
     * Sets the event method name.
     * 
     * @param eventMethod event method name
     */
    public void setEventMethod(String eventMethod) {
        this.eventMethod = eventMethod;
    }

    /**
     * Sets the event thread name.
     * 
     * @param thread event thread name
     */
    public void setThread(String thread) {
        this.thread = thread;
    }

    /**
     * Gets the event thread name.
     * 
     * @return event thread name
     */
    public String getThread() {
        return this.thread;
    }

    /**
     * Gets the event return value.
     * 
     * @return event return value
     */
    public String getReturnValue() {
        return returnValue;
    }

    /**
     * Sets the event return value.
     * 
     * @param returnValue event return value
     */
    public void setReturnValue(String returnValue) {
        this.endTime = Instant.now();
        this.timeTaken = Duration.between(this.startTime, this.endTime).toMillis();
        this.returnValue = returnValue;
    }

    /**
     * Gets the event execution time.
     * 
     * @return event execution time
     */
    public long getTime() {
        return this.timeTaken;
    }

    /**
     * Returns a stringified version of the object.
     * 
     * @return stringified object
     */
    public String toString() {
        return this.eventMethod;
    }
}