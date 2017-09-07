package simulator;

import network.Circuit;

import java.io.Serializable;
import java.util.Vector;

/**
 * This class represents the simulator event machine.
 * It is responsible for managing the events that are created during a simulation.
 * 
 * @author Iallen
 */
@SuppressWarnings("serial")
public class EventMachine implements Serializable {
	
    private Vector<Event> eventList;
    private double countEvent = 0;

    /**
     * Creates a new instance of EventMachine
     */
    public EventMachine() {
        this.eventList = new Vector<Event>();
    }

    /**
     * Inserts an event in the event machine.
     *
     * @param e Event
     */
    public void insert(Event e) {
        e.setId(this.countEvent);
        this.countEvent++;
        int i = 0;
        while (i < eventList.size() && (eventList.elementAt(i)).getTimeHours() < e.getTimeHours()) {
            i++;
        }
        eventList.insertElementAt(e, i);
    }

    /**
     * Starts running the event machine.
     * The event machine runs until there are no more events in the eventList.
     */
    public void executeEvents() throws Exception {
        while (eventList.size() > 0) {
            Event e = eventList.firstElement();
            eventList.removeElementAt(0);
            e.listener().execute(e);
        }
    }

    /**
     * Returns the number of scheduled events (existing in the eventList).
     *
     * @return int
     */
    public int size() {
        return this.eventList.size();
    }

    /**
     * Ends the event machine (clear eventList).
     */
    public void stopMachine() {
        this.eventList.removeAllElements();
    }

    /**
     * Remove the event that contains the request.
     *
     * @param request Circuit
     */
    public void remove(Circuit request) {
        for (int i = 0; i < this.eventList.size(); i++) {
            Event event = this.eventList.get(i);
            if (event.getObject() instanceof Circuit)
                if (event.getObject() == request) {
                    this.eventList.remove(event);
                    break;
                }
        }
    }
}

