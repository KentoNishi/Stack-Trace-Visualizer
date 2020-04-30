public class StackEvent {
    private String eventMethod;
    private String returnValue;

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
        this.returnValue = returnValue;
    }

    public String toString() {
        return this.eventMethod;
    }
}