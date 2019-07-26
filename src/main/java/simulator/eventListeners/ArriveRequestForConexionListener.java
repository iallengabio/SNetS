package simulator.eventListeners;

import grmlsa.Route;
import measurement.Measurements;
import request.RequestForConnection;
import simulator.Event;
import simulator.EventMachine;
import simulator.Simulation;

/**
 * This class is the event listener of a new arrive request for a new connection.
 * This class handles each request for a new connection trying assign a new optical circuit.
 *
 * @author Iallen
 */
public class ArriveRequestForConexionListener implements EventListener {

    private EventMachine em;
    private Simulation simulation;
    private int numOfRequests;

    /**
     * Creates a new instance of ArriveRequestForConexionListener.
     * 
     * @param em EventMachine
     * @param simulation Simulation
     */
    public
    ArriveRequestForConexionListener(EventMachine em, Simulation simulation) {
        this.em = em;
        this.simulation = simulation;
        numOfRequests = 0;
    }

    /**
     * Run a certain 'e' event.
     */
    @Override
    public void execute(Event e) throws Exception {

        RequestForConnection requestForConnection = (RequestForConnection) e.getObject();

        // Schedule next request to connect to this same requestGenerator
        Measurements m = simulation.getMeasurements();
        
        if (!m.finished()) { // Schedule another request through the same generator of this
            requestForConnection.getRequestGenerator().scheduleNextRequest(em, this);
        }

        beforeReq();
        /*if(m.getConsumedEnergyMetric() != null){
        	m.getConsumedEnergyMetric().addNewObservation(simulation.getControlPlane(), e.getTimeHours(), false, requestForConnection, false);
        }*/
        
        // Try to satisfy the request
        Boolean success = simulation.getControlPlane().handleRequisition(requestForConnection);
        if (success) {// Schedule the end of the requisition and release of resources
            em.insert(new Event(requestForConnection, new HoldRequestListener(simulation), requestForConnection.getTimeOfFinalizeHours()));
        }
        
        afterReq(requestForConnection, success);

        numOfRequests++;
        //System.out.println(numOfRequests);

        //if(numOfRequests%10==0) System.out.println(numOfRequests);
        
        //printTest(requestForConnection, success);
    }

    /**
     * Verifies transient state and generation of requests
     */
    private void beforeReq() {
        Measurements m = simulation.getMeasurements();
        
        // Transient state check
        m.transientStepVerify();
        
        // Increase in the number of generated circuit requests
        m.incNumGeneratedReq();
    }

    /**
     * Update performance metrics
     * 
     * @param request RequestForConnection
     * @param success boolean
     */
    private void afterReq(RequestForConnection request, boolean success) {
        Measurements m = simulation.getMeasurements();
        
        // Adds a new note for all enabled performance metrics
        m.addNewObservation(simulation.getControlPlane(), success, request);
        
        /*if(m.getConsumedEnergyMetric() != null){
        	m.getConsumedEnergyMetric().addNewObservation(simulation.getControlPlane(), request.getTimeOfFinalizeHours(), success, request, true);
        }*/
    }
    
    
    public void printTest(RequestForConnection rfc, boolean success){
		Route route = rfc.getCircuits().get(0).getRoute();
		
		System.out.println("----------------------------------------------------------");
    	System.out.println("Par = "+ rfc.getPair().getPairName());
    	System.out.print("Lista de nos: ");
    	int size = route.getNodeList().size();
    	for(int i = 0; i < size; i++){
    		System.out.print(route.getNodeList().get(i).getName()+", ");
    	}
    	System.out.println();
    	System.out.println("Taxa de bits (Gbps) = " + (rfc.getRequiredBandwidth() / 1000000000.0));
    	System.out.println("Modulacao = " + rfc.getCircuits().get(0).getModulation().getName());
    	System.out.println("Distancia = " + route.getDistanceAllLinks());
    	if(rfc.getCircuits().get(0).getSpectrumAssigned() != null){
    		System.out.println("Quant slots requeridos = " + (rfc.getCircuits().get(0).getSpectrumAssigned()[1] - rfc.getCircuits().get(0).getSpectrumAssigned()[0] + 1));
    		System.out.println("Faixa de espectro = (" + rfc.getCircuits().get(0).getSpectrumAssigned()[0] + ", " + rfc.getCircuits().get(0).getSpectrumAssigned()[1] + ")");
    	}else{
    		System.out.println("Faixa de espectro = vazio");
    	}
    	System.out.println("SNR (dB) = " + rfc.getCircuits().get(0).getSNR());
    	System.out.println("RMLSA = " + success);
    	System.out.println("QoT = " + rfc.getCircuits().get(0).isQoT());
    	System.out.println("QoT for other = " + rfc.getCircuits().get(0).isQoTForOther());
	}

}
