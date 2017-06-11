package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grmlsa.GRMLSA;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;
import request.RequestForConnection;

/**
 * Class that represents the control plane for a Transparent Elastic Optical Network.
 * This class should make calls to RMLSA algorithms, store routes in case of fixed routing, 
 * provide information about the state of the network, etc.
 *
 * @author Iallen
 */
public class ControlPlane {

    private int rsaType;
    private RoutingAlgorithmInterface routing;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    private IntegratedRMLSAAlgorithmInterface integrated;
    private ModulationSelector modulationSelector;
    private TrafficGroomingAlgorithmInterface grooming;
	
    private Mesh mesh;

    
    /**
     * The first key represents the source node.
     * The second key represents the destination node.
     */
    private HashMap<String, HashMap<String, List<Circuit>>> activeCircuits;

    /**
     * Instance the control plane with the list of active circuits in empty
     * @param mesh
     * @param rmlsaType
     * @param trafficGroomingAlgorithm
     * @param integratedRSAAlgoritm
     * @param routingInterface
     * @param spectrumAssignmentAlgoritm
     */
    public ControlPlane(Mesh mesh, int rmlsaType, TrafficGroomingAlgorithmInterface trafficGroomingAlgorithm, IntegratedRMLSAAlgorithmInterface integratedRSAAlgoritm, RoutingAlgorithmInterface routingInterface, SpectrumAssignmentAlgorithmInterface spectrumAssignmentAlgoritm) {
        activeCircuits = new HashMap<>();

        this.rsaType = rmlsaType;
        this.grooming = trafficGroomingAlgorithm;
        this.integrated = integratedRSAAlgoritm;
        this.routing = routingInterface;
        this.spectrumAssignment = spectrumAssignmentAlgoritm;
        this.modulationSelector = new ModulationSelector(mesh.getLinkList().get(0).getSlotSpectrumBand(), mesh.getGuardBand(), mesh);

        setMesh(mesh);
    }
    
    /**
     * This method create a new transparent circuit.
     * 
     * @param rfc RequestForConnection
     * @return Circuit
     */
    public Circuit createNewCircuit(RequestForConnection rfc){
    	
    	Circuit circuit = new Circuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		rfc.setCircuit(circuit);
		
		return circuit;
    }

    /**
     * Configures the network mesh
     * 
     * @param mesh the mesh to set
     */
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        // Initialize the active circuit list
        for (Node node1 : mesh.getNodeList()) {
            HashMap<String, List<Circuit>> hmAux = new HashMap<>();
            for (Node node2 : mesh.getNodeList()) {
                hmAux.put(node2.getName(), new ArrayList<>());
            }
            activeCircuits.put(node1.getName(), hmAux);
        }
    }

    /**
     * Returns the network mesh
     * 
     * @return the mesh
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * This method tries to satisfy a certain request by checking if there are available resources for the establishment of the circuit.
     * This method verifies the possibility of satisfying a circuit request.
     *
     * @param rfc RequestForConnection
     * @return boolean
     */
    public boolean handleRequisition(RequestForConnection rfc) {
        return grooming.searchCircuitsForGrooming(rfc, this);
    }

    /**
     * This method ends a connection
     *
     * @param rfc RequestForConnection
     */
    public void finalizeConnection(RequestForConnection rfc) {
        this.grooming.finishConnection(rfc, this);
    }
    /**
     * Releases the resources being used by a given circuit
     *
     * @param circuit
     */
    public void releaseCircuit(Circuit circuit) {
        Route route = circuit.getRoute();

        releaseSpectrum(circuit, circuit.getSpectrumAssigned(), route.getLinkList());

        // Release transmitter and receiver
        circuit.getSource().getTxs().releasesTransmitters();
        circuit.getDestination().getRxs().releasesReceivers();

        activeCircuits.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).remove(circuit);
    }

    /**
     * This method is called after executing RMLSA algorithms to allocate resources in the network
     *
     * @param circuit Circuit
     */
    protected void allocateCircuit(Circuit circuit) {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[] = circuit.getSpectrumAssigned();
        
        allocateSpectrum(circuit, chosen, links);
        
        // Allocates transmitter and receiver
        circuit.getSource().getTxs().allocatesTransmitters();
        circuit.getDestination().getRxs().allocatesReceivers();
        
        activeCircuits.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).add(circuit);
    }

    /**
     * This method allocates the spectrum band selected for the circuit in the route links
     * 
     * @param circuit Circuit
     * @param chosen int[]
     * @param links List<Link>
     */
    protected void allocateSpectrum(Circuit circuit, int[] chosen, List<Link> links) {
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            
            link.useSpectrum(chosen);
            link.addCircuit(circuit);
        }
    }

    /**
     * This method releases the allocated spectrum for the circuit
     * 
     * @param circuit Circuit
     * @param chosen int[]
     * @param links List<Link>
     */
    protected void releaseSpectrum(Circuit circuit, int chosen[], List<Link> links) {
        for (int i = 0; i < links.size(); i++) {
        	Link link = links.get(i);
        	
            link.liberateSpectrum(chosen);
            link.removeCircuit(circuit);
        }
    }
    
    /**
     * This method tries to establish a new circuit in the network
     *
     * @param circuit Circuit
     * @return true if the circuit has been successfully allocated, false if the circuit can not be allocated.
     */
    public boolean establishCircuit(Circuit circuit) {

    	// Check if there are free transmitters and receivers
    	if(circuit.getSource().getTxs().hasFreeTransmitters() && circuit.getDestination().getRxs().hasFreeRecivers()) {
    		
    		// Can allocate spectrum
            if (this.tryEstablishNewCircuit(circuit)) {

            	// Pre-admits the circuit for QoT verification
                this.allocateCircuit(circuit);
                
                // QoT verification
                if(isAdmissibleQualityOfTransmission(circuit)){
                	return true; // Admits the circuit
                }
            }
        }

        return false; // Rejects the circuit
    }

    /**
     * This method tries to answer a given request by allocating the necessary resources to the same one
     *
     * @param circuit Circuit
     * @return boolean
     */
    protected boolean tryEstablishNewCircuit(Circuit circuit) {

        switch (this.rsaType) {
            case GRMLSA.RSA_INTEGRATED:
                return integrated.rsa(circuit, this.getMesh());

            case GRMLSA.RSA_SEQUENCIAL:
                if (routing.findRoute(circuit, this.getMesh())) {
                    Modulation mod = modulationSelector.selectModulation(circuit, circuit.getRoute(), spectrumAssignment, this.getMesh());
                    circuit.setModulation(mod);
                    return spectrumAssignment.assignSpectrum(mod.requiredSlots(circuit.getRequiredBandwidth()), circuit);
                } else {
                    return false;
                }
        }

        return false;
    }

    /**
     * Increase the number of slots used by a given circuit
     *
     * @param circuit Circuit
     * @param upperBand int[]
     * @param bottomBand int[]
     * @return boolean
     */
    public boolean expandCircuit(Circuit circuit, int upperBand[], int bottomBand[]) {

        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[];
        int specAssigAt[] = circuit.getSpectrumAssigned();
        
        if (upperBand != null) {
            chosen = upperBand;
            allocateSpectrum(circuit, chosen, links);
            specAssigAt[1] = upperBand[1];
        }
        
        if (bottomBand != null) {
            chosen = bottomBand;
            allocateSpectrum(circuit, chosen, links);
            specAssigAt[0] = bottomBand[0];
        }
        circuit.setSpectrumAssigned(specAssigAt);

        return true;
    }

    /**
     * Reduces the number of slots used by a given circuit
     *
     * @param circuit Circuit
     * @param bottomBand int[]
     * @param upperBand int[]
     */
    public void retractCircuit(Circuit circuit, int bottomBand[], int upperBand[]) {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[];
        int specAssigAt[] = circuit.getSpectrumAssigned();
        
        if (bottomBand != null) {
            chosen = bottomBand;
            releaseSpectrum(circuit, chosen, links);
            specAssigAt[0] = bottomBand[1] + 1;
        }
        
        if (upperBand != null) {
            chosen = upperBand;
            releaseSpectrum(circuit, chosen, links);
            specAssigAt[1] = upperBand[0] - 1;
        }
        
        circuit.setSpectrumAssigned(specAssigAt);
    }

    /**
     * To find active circuits on the network with specified source and destination
     *
     * @param source String
     * @param destination String
     * @return List<Circuit>
     */
    public List<Circuit> searchForActiveCircuits(String source, String destination) {
        return this.activeCircuits.get(source).get(destination);
    }
    
    /**
     * This method verifies the transmission quality of the circuit in the establishment 
     * and also verifies the transmission quality of the other already active circuits
     * 
     * @param circuit Circuit
     * @return boolean
     */
    protected boolean isAdmissibleQualityOfTransmission(Circuit circuit){
    	
    	// Check if it is to test the QoT
    	if(mesh.getPhysicalLayer().isActiveQoT()){
    		
    		// Verifies the QoT of the current circuit
    		if(computeQualityOfTransmission(circuit)){
    			boolean QoTForOther = true;
    			
    			// Check if it is to test the QoT of other already active circuits
    			if(mesh.getPhysicalLayer().isActiveQoTForOther()){
    				
    				// Calculates the QoT of the other circuits
    				QoTForOther = computeQoTForOther(circuit);
    				circuit.setQoTForOther(QoTForOther);
    			}
    			
    			if(QoTForOther){
    				// QoT the other circuits was kept acceptable
    				return true;
    			} else {
    				// QoT the other circuits was not kept acceptable, frees allocated resources
    				releaseCircuit(circuit);
    				// Recalculates the QoT of the other circuits
    				computeQoTForOther(circuit);
    			}
    			
    		} else {
    			// Circuit QoT is not acceptable, frees allocated resources
    			releaseCircuit(circuit);
    		}
    	} else {
    		// If it does not check the QoT then it returns acceptable
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * This method verifies the quality of the transmission of the circuit
     * 
     * @param circuit Circuit
     * @return boolean - True, if QoT is acceptable, or false, otherwise
     */
    protected boolean computeQualityOfTransmission(Circuit circuit){
    	double SNR = mesh.getPhysicalLayer().computeSNRSegment(circuit, circuit.getRequiredBandwidth(), circuit.getRoute(), 0, circuit.getRoute().getNodeList().size() - 1, circuit.getModulation(), circuit.getSpectrumAssigned(), false);
		double SNRdB = PhysicalLayer.ratioForDB(SNR);
		circuit.setSNR(SNRdB);
		
		boolean QoT = mesh.getPhysicalLayer().isAdmissible(circuit.getModulation(), SNRdB, SNR);
		circuit.setQoT(QoT);
		
		return QoT;
    }
    
    /**
     * This method verifies the transmission quality of the other already active circuits
     * 
     * @param circuit Circuit
     * @return boolean - True, if it did not affect another circuit, or false otherwise
     */
    protected boolean computeQoTForOther(Circuit circuit){
    	List<Circuit> circuits = new ArrayList<>();
		
		Route route = circuit.getRoute();
		for (Link link : route.getLinkList()) {
			List<Circuit> circuitsTemp = link.getCircuitList();

            for (Circuit circuitTemp : circuitsTemp) {
                if (!circuit.equals(circuitTemp) && !circuits.contains(circuitTemp)) {
                    circuits.add(circuitTemp);
                }
            }
		}

        for (Circuit circuitTemp : circuits) {
            boolean QoT = computeQualityOfTransmission(circuitTemp);
            if (!QoT) {
                return false;
            }
        }
		
		return true;
    }
}
