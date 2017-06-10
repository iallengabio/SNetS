package simulator;

import simulator.eventListeners.EventListener;

import java.io.Serializable;

/**
 * This class represents the events that are executed by the simulator event machine
 * 
 * @author Iallen
 */
@SuppressWarnings("serial")
public class Event implements Serializable {

    private Object object;
    private EventListener eventListener;
    private double timeHours;
    private double id;

    /**
     * Build an event.
     *
     * @param r         Object
     * @param eListener EventListener
     * @param timeHours double
     */
    public Event(Object r, EventListener eListener, double timeHours) {
        this.object = r;
        this.eventListener = eListener;
        this.timeHours = timeHours;
    }

    /**
     * Configure event identifier
     * 
     * @param x double
     */
    public void setId(double x) {
        id = x;
    }

    /**
     * Returns the event identifier
     * 
     * @return double
     */
    public double getId() {
        return id;
    }
    
    /**
     * Returns who is the event listener.
     *
     * @return EventListener
     */
    public EventListener listener() {
        return this.eventListener;
    }

    /**
     * Returns the time the event will be triggered in hours.
     *
     * @return double
     */
    public double getTimeHours() {
        return this.timeHours;
    }

    /**
     * Returns the Object associated with the event.
     *
     * @return Object
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * Configures the object associated with the event
     *
     * @param x Object
     */
    public void setObject(Object x) {
        this.object = x;
    }

}

