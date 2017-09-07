package simulator.eventListeners;

import simulator.Event;


/**
 * Interface of classes that deal with the execution of events in the simulator by changing the state 
 * of the system
 * 
 * @author Iallen
 */
public interface EventListener {

	/**
     * Run a certain 'e' event.
     *
     * @param e Event
     */
    public abstract void execute(Event e) throws Exception;
}

