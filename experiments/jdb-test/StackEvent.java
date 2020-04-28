public class StackEvent {
    private String eventType;
    private String eventMethod;
    private String returnValue;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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
        this.returnValue = returnValue;
    }

    public String toString() {
        if (this.getEventType().equals("entered")) {
            return "Entered Method: " + this.getEventMethod();
        }
        return "Exited Method: " + this.getEventMethod() + ", Returned: " + this.getReturnValue();
    }
}