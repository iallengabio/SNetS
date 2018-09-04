package network;

import java.util.ArrayList;

import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.survival.SurvivalStrategyInterface;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;
import request.RequestForConnection;

public class SurvivalControlPlane extends ControlPlane {
	
	protected SurvivalStrategyInterface survivalStrategy;
	
	/**
	 * Creates a new instance of SurvivalControlPlane
	 * 
	 * @param mesh Mesh
	 * @param rmlsaType int
	 * @param trafficGroomingAlgorithm TrafficGroomingAlgorithmInterface
	 * @param integratedRMLSAAlgorithm IntegratedRMLSAAlgorithmInterface
	 * @param routingAlgorithm RoutingAlgorithmInterface
	 * @param spectrumAssignmentAlgorithm SpectrumAssignmentAlgorithmInterface
	 * @param modulationSelection ModulationSelectionAlgorithmInterface
	 */
	public SurvivalControlPlane(Mesh mesh, int rmlsaType, TrafficGroomingAlgorithmInterface trafficGroomingAlgorithm, IntegratedRMLSAAlgorithmInterface integratedRMLSAAlgorithm, RoutingAlgorithmInterface routingAlgorithm, SpectrumAssignmentAlgorithmInterface spectrumAssignmentAlgorithm, ModulationSelectionAlgorithmInterface modulationSelection, SurvivalStrategyInterface survivalStrategy){
		super(mesh, rmlsaType, trafficGroomingAlgorithm, integratedRMLSAAlgorithm, routingAlgorithm, spectrumAssignmentAlgorithm, modulationSelection);
		
		this.survivalStrategy = survivalStrategy;
	}

	/**
     * This method creates a new survival circuit.
     * 
     * @param rfc RequestForConnection
     * @return Circuit
     */
    public Circuit createNewCircuit(RequestForConnection rfc){
    	
    	Circuit circuit = new SurvivalCircuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		ArrayList<Circuit> circs = new ArrayList<>();
		circs.add(circuit);
		rfc.setCircuit(circs);
		
		return circuit;
    }
    
    /**
     * This method creates a new survival circuit.
     * 
     * @param rfc RequestForConnection
     * @param p Pair
     * @return Circuit
     */
    public Circuit createNewCircuit(RequestForConnection rfc, Pair p) {
    	
        Circuit circuit = new SurvivalCircuit();
        circuit.setPair(p);
        circuit.addRequest(rfc);
        if (rfc.getCircuits() == null) {
            rfc.setCircuit(new ArrayList<>());
        }
        rfc.getCircuits().add(circuit);
        
        return circuit;
    }

	/**
	 * Returns the survival strategy
	 * 
	 * @return the survivalStrategy
	 */
	public SurvivalStrategyInterface getSurvivalStrategy() {
		return survivalStrategy;
	}

	/**
	 * Sets the survival strategy
	 * 
	 * @param survivalStrategy the survivalStrategy to set
	 */
	public void setSurvivalStrategy(SurvivalStrategyInterface survivalStrategy) {
		this.survivalStrategy = survivalStrategy;
	}
    
	/**
     * This method tries to satisfy a certain request by checking if there are available resources for the establishment of the circuit.
     * This method verifies the possibility of satisfying a circuit request.
     *
     * @param rfc RequestForConnection
     * @return boolean
     */
    public boolean handleRequisition(RequestForConnection rfc) throws Exception {
        return survivalStrategy.applyStrategy(rfc, this);
    }
    
    /**
     * Verifies if there are free transmitters and receivers for the establishment of the new circuit
     * 
     * @param circuit Circuit
     * @return boolean
     */
    public boolean thereAreFreeTransponders(Circuit circuit){
    	return (circuit.getSource().getTxs().hasFreeTransmitters() && circuit.getDestination().getRxs().hasFreeRecivers());
    }
    
    /**
     * This method tries to establish a new circuit in the network
     *
     * @param circuit Circuit
     * @return true if the circuit has been successfully allocated, false if the circuit can not be allocated.
     */
    public boolean establishCircuit(Circuit circuit) throws Exception {

    	// Checks if there are free transmitters and receivers
    	if(thereAreFreeTransponders(circuit)) {
    		
    		// Can allocate spectrum
            if (tryEstablishNewCircuit(circuit)) {

            	// Pre-admits the circuit for QoT verification
                this.allocateCircuit(circuit);
                
                // QoT verification
                if(isAdmissibleQualityOfTransmission(circuit)){
                    updateNetworkPowerConsumption();
                	return true; // Admits the circuit
                	
                } else {
                	// Circuit QoT is not acceptable, frees allocated resources
        			releaseCircuit(circuit);
                }
            }
        }

        return false; // Rejects the circuit
    }
}
