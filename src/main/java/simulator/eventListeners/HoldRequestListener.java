package simulator.eventListeners;

import request.RequestForConnection;
import simulator.Event;
import simulator.Simulation;

import java.io.Serializable;

/**
 * This class is the event listener of a hold request for the finalize connection.
 * This class handles each connection trying to liberate the resources allocated for optical circuit.
 *
 * @author Iallen
 */
public class HoldRequestListener implements EventListener, Serializable {
    private Simulation simulation;

    /**
     * Creates a new instance of HoldRequestListener.
     */
    public HoldRequestListener(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Run a certain 'e' event.
     */
    @Override
    public void execute(Event e) {
        RequestForConnection request = (RequestForConnection) e.getObject();
        simulation.getControlPlane().finalizeConnection(request);
    }

}