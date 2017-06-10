package simulator.eventListeners;

import measurement.Measurements;
import request.RequestForConnection;
import simulator.Event;
import simulator.EventMachine;
import simulator.Simulation;

import java.io.Serializable;

/**
 * This class is the event listener of a new arrive request for a new connection.
 * This class handles each request for a new connection trying assign a new optical circuit.
 *
 * @author Iallen
 */
public class ArriveRequestForConexionListener implements EventListener, Serializable {

    private EventMachine em;
    private Simulation simulation;

    /**
     * Creates a new instance of ArriveRequestForConexionListener.
     * 
     * @param em EventMachine
     * @param simulation Simulation
     */
    public ArriveRequestForConexionListener(EventMachine em, Simulation simulation) {
        this.em = em;
        this.simulation = simulation;
    }

    /**
     * Run a certain 'e' event.
     */
    @Override
    public void execute(Event e) {

        RequestForConnection requestForConnection = (RequestForConnection) e.getObject();

        // Schedule next request to connect to this same requestGenerator
        Measurements m = simulation.getMeasurements();
        
        if (!m.finished()) { // Schedule another request through the same generator of this
            requestForConnection.getRequestGenerator().scheduleNextRequest(em, this);
        }

        beforeReq();
        
        // Try to satisfy the request
        Boolean success = simulation.getControlPlane().handleRequisition(requestForConnection);
        if (success) {// Schedule the end of the requisition and release of resources
            em.insert(new Event(requestForConnection, new HoldRequestListener(simulation), requestForConnection.getTimeOfFinalizeHours()));
        }
        
        afterReq(requestForConnection, success);
    }

    /**
     * Verifies transient state and generation of requests
     */
    private void beforeReq() {
        Measurements m = simulation.getMeasurements();

        m.transientStepVerify(simulation.getControlPlane().getMesh().getNodeList());

        m.incNumGeneratedReq();
    }

    /**
     * Update performance metrics
     * @param request RequestForConnection
     * @param success boolean
     */
    private void afterReq(RequestForConnection request, boolean success) {

        Measurements m = simulation.getMeasurements();
        m.getProbabilidadeDeBloqueioMeasurement().addNewObservation(success, request);
        m.getProbabilidadeDeBloqueioDeBandaMeasurement().addNewObservation(success, request);
        m.getFragmentacaoExterna().addNewObservation(request.getCircuit());
        m.getUtilizacaoSpectro().addNewObservation();
        m.getFragmentacaoRelativa().addNewObservation(request.getCircuit());
        m.getSpectrumSizeStatistics().addNewObservation(request.getCircuit());
        m.getTransmitersReceiversUtilization().addNewObservation();
    }

}
