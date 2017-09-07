package simulator;

import measurement.Measurements;
import network.Pair;
import network.RequestGenerator;
import simulator.eventListeners.ArriveRequestForConexionListener;

/**
 * This class is responsible for instantiating the event machine.
 * 
 * @author Iallen
 */
public class Simulator {

    private EventMachine eMachine;
    private ArriveRequestForConexionListener arriveRequest;
    private Simulation simulation;

    /**
     * Creates a new instance of Simulator.
     *
     * @param simulation Simulation
     */
    public Simulator(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Starts the Simulation. For this you need:
	 * (1) loading the mesh;
	 * (2) create the instances of ArriveRequest and FinalizeRequest;
     * (3) scheduling the first requisition arrival events.
     *
     * @return Measurements
     */
    public Measurements start() throws Exception {
        eMachine = new EventMachine();
     // Creating the ArriveRequest event listener
        arriveRequest = new ArriveRequestForConexionListener(this.getEventMachine(), simulation);
        this.scheduleFirstEvents();
        this.eMachine.executeEvents();
        return this.simulation.getMeasurements();
    }

    /**
     * Schedule the first events of arrive request.
	 * This is done for each network request generator.
     */
    private void scheduleFirstEvents() {
        for (Pair pair : simulation.getMesh().getPairList()) {
            for (RequestGenerator rg : pair.getRequestGenerators()) {
                rg.scheduleNextRequest(eMachine, arriveRequest);
            }
        }
    }

    /**
     * Returns the event machine
     * 
     * @return EventMachine
     */
    public EventMachine getEventMachine() {
        return this.eMachine;
    }
    
    /**
     * Returns the simulation
     * 
     * @return Simulation
     */
    public Simulation getSimulation() {
        return this.simulation;
    }
}
