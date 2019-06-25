package simulator.eventListeners;

import request.RequestForConnection;
import simulator.Event;
import simulator.Simulation;

/**
 * This class is the event listener of a hold request for the finalize connection.
 * This class handles each connection trying to liberate the resources allocated for optical circuit.
 *
 * @author Iallen
 */
public class HoldRequestListener implements EventListener {
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
    public void execute(Event e) throws Exception {
        RequestForConnection request = (RequestForConnection) e.getObject();
        simulation.getControlPlane().finalizeConnection(request);
        if(simulation.getMeasurements().getConsumedEnergyMetric() != null){
            RequestForConnection rfc = new RequestForConnection();
            rfc.setTimeOfRequestHours(e.getTimeHours());
            simulation.getMeasurements().getConsumedEnergyMetric().addNewObservation(simulation.getControlPlane(),true,rfc);
        }

    }

}
