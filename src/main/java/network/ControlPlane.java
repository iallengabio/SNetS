package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import grmlsa.GRMLSA;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelector;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

/**
 * Class that represents the control plane for a Transparent Elastic Optical Network.
 * This class should make calls to RMLSA algorithms, store routes in case of fixed routing, 
 * provide information about the state of the network, etc.
 *
 * @author Iallen
 */
public class ControlPlane {

    protected int rsaType;
    protected RoutingAlgorithmInterface routing;
    protected SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    protected IntegratedRMLSAAlgorithmInterface integrated;
    protected ModulationSelectionAlgorithmInterface modulationSelection;
    protected TrafficGroomingAlgorithmInterface grooming;
    
    protected Mesh mesh;
    
    /**
     * The first key represents the source node.
     * The second key represents the destination node.
     */
    protected HashMap<String, HashMap<String, List<Circuit>>> activeCircuits;

    
    private TreeSet<Circuit> connectionList;

    /**
     * Instance the control plane with the list of active circuits in empty
     * 
     * @param mesh Mesh
     * @param rmlsaType int
     * @param trafficGroomingAlgorithm TrafficGroomingAlgorithmInterface
     * @param integratedRMLSAAlgorithm IntegratedRMLSAAlgorithmInterface
     * @param routingAlgorithm RoutingAlgorithmInterface
     * @param spectrumAssignmentAlgorithm SpectrumAssignmentAlgorithmInterface
     * @param modulationSelection ModulationSelectionAlgorithmInterface
     */
    public ControlPlane(Mesh mesh, int rmlsaType, TrafficGroomingAlgorithmInterface trafficGroomingAlgorithm, IntegratedRMLSAAlgorithmInterface integratedRMLSAAlgorithm, RoutingAlgorithmInterface routingAlgorithm, SpectrumAssignmentAlgorithmInterface spectrumAssignmentAlgorithm, ModulationSelectionAlgorithmInterface modulationSelection) {
        this.activeCircuits = new HashMap<>();
        this.connectionList = new TreeSet<>();
        
        this.rsaType = rmlsaType;
        this.grooming = trafficGroomingAlgorithm;
        this.integrated = integratedRMLSAAlgorithm;
        this.routing = routingAlgorithm;
        this.spectrumAssignment = spectrumAssignmentAlgorithm;
        this.modulationSelection = modulationSelection;
        
        this.modulationSelection.setAvaliableModulations(ModulationSelector.configureModulations(mesh));

        setMesh(mesh);
        mesh.computesPowerConsmption(this);
    }
    
    /**
     * This method creates a new transparent circuit.
     * 
     * @param rfc RequestForConnection
     * @return Circuit
     */
    public Circuit createNewCircuit(RequestForConnection rfc){
    	Circuit circuit = new Circuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		ArrayList<Circuit> circs = new ArrayList<>();
		circs.add(circuit);
		rfc.setCircuit(circs);
		return circuit;
    }
    
    /**
     * This method creates a new transparent circuit.
     * 
     * @param rfc RequestForConnection
     * @param p Pair
     * @return Circuit
     */
    public Circuit createNewCircuit(RequestForConnection rfc, Pair p) {
        Circuit circuit = new Circuit();
        circuit.setPair(p);
        circuit.addRequest(rfc);
        if (rfc.getCircuits() == null) {
            rfc.setCircuit(new ArrayList<>());
        }
        rfc.getCircuits().add(circuit);
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
            	if(!node1.equals(node2)){
	                hmAux.put(node2.getName(), new ArrayList<>());
            	}
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
     * Returns the modulation selection
     * 
     * @return ModulationSelection
     */
    public ModulationSelectionAlgorithmInterface getModulationSelection(){
    	return modulationSelection;
    }

    /**
     * Returns the spectrum assignment
     * 
     * @return SpectrumAssignmentAlgorithmInterface
     */
    public SpectrumAssignmentAlgorithmInterface getSpectrumAssignment(){
    	return spectrumAssignment;
    }
    
    /**
     * Returns the routing algorithm
     * 
     * @return RoutingAlgorithmInterface
     */
    public RoutingAlgorithmInterface getRouting(){
    	return routing;
    }
    
    /**
     * Returns the integrated RMLSA algorithm
     * 
     * @return IntegratedRMLSAAlgorithmInterface
     */
    public IntegratedRMLSAAlgorithmInterface getIntegrated(){
    	return integrated;
    }
    
    /**
     * This method tries to satisfy a certain request by checking if there are available resources for the establishment of the circuit.
     * This method verifies the possibility of satisfying a circuit request.
     *
     * @param rfc RequestForConnection
     * @return boolean
     */
    public boolean handleRequisition(RequestForConnection rfc) throws Exception {
        return grooming.searchCircuitsForGrooming(rfc, this);
    }

    /**
     * This method ends a connection
     *
     * @param rfc RequestForConnection
     */
    public void finalizeConnection(RequestForConnection rfc) throws Exception {
        this.grooming.finishConnection(rfc, this);
    }

    /**
     * This method is called after executing RMLSA algorithms to allocate resources in the network
     *
     * @param circuit Circuit
     */
    public void allocateCircuit(Circuit circuit) throws Exception {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int band[] = circuit.getSpectrumAssigned();

        if(!allocateSpectrum(circuit, band, links)){
            throw new Exception("bad RMLSA choice. Spectrum cant be allocated.");
        }

        // Allocates transmitter and receiver
        circuit.getSource().getTxs().allocatesTransmitters();
        circuit.getDestination().getRxs().allocatesReceivers();
        
        addConnection(circuit);
    }

    /**
     * This method allocates the spectrum band selected for the circuit in the route links
     * 
     * @param circuit Circuit
     * @param band int[]
     * @param links List<Link>
     */
    protected boolean allocateSpectrum(Circuit circuit, int[] band, List<Link> links) throws Exception {
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);

            if(!link.useSpectrum(band)){//spectrum already in use
                i--;
                for(;i>=0;i--){
                    links.get(i).liberateSpectrum(band);
                }
                return false;
            }
        }
        return true;
    }
    
    /**
     * Releases the resources being used by a given circuit
     *
     * @param circuit
     */
    public void releaseCircuit(Circuit circuit) throws Exception {
        Route route = circuit.getRoute();
        int band[] = circuit.getSpectrumAssigned();
        
        releaseSpectrum(circuit, band, route.getLinkList());

        // Release transmitter and receiver
        circuit.getSource().getTxs().releasesTransmitters();
        circuit.getDestination().getRxs().releasesReceivers();

        removeConnection(circuit);

        updateNetworkPowerConsumption();
    }
    
    /**
     * This method releases the allocated spectrum for the circuit
     * 
     * @param circuit Circuit
     * @param band int[]
     * @param links List<Link>
     */
    protected void releaseSpectrum(Circuit circuit, int band[], List<Link> links) throws Exception {
        for (int i = 0; i < links.size(); i++) {
        	Link link = links.get(i);
        	
            link.liberateSpectrum(band);
        }
    }
    
    /**
     * This method tries to establish a new circuit in the network
     *
     * @param circuit Circuit
     * @return true if the circuit has been successfully allocated, false if the circuit can not be allocated.
     */
    public boolean establishCircuit(Circuit circuit) throws Exception {

    	// Check if there are free transmitters and receivers
    	if(circuit.getSource().getTxs().hasFreeTransmitters() && circuit.getDestination().getRxs().hasFreeRecivers()) {
    		
    		// Can allocate spectrum
            if (this.tryEstablishNewCircuit(circuit)) {

            	// Pre-admits the circuit for QoT verification
                this.allocateCircuit(circuit);
                
                // QoT verification
                if(isAdmissibleQualityOfTransmission(circuit)){
                    this.updateNetworkPowerConsumption();
                	return true; // Admits the circuit
                	
                } else {
                	// Circuit QoT is not acceptable, frees allocated resources
        			releaseCircuit(circuit);
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
                return integrated.rsa(circuit, this);

            case GRMLSA.RSA_SEQUENCIAL:
                if (routing.findRoute(circuit, this.getMesh())) {
                    Modulation mod = modulationSelection.selectModulation(circuit, circuit.getRoute(), spectrumAssignment, this);
                    circuit.setModulation(mod);
                    if(mod != null){
	                    return spectrumAssignment.assignSpectrum(mod.requiredSlots(circuit.getRequiredBandwidth()), circuit, this);
                    }
                }
        }

        return false;
    }

    /**
     * Increases the number of slots used by a given circuit
     * 
     * @param circuit Circuit
     * @param numSlotsUp int
     * @param numSlotsDown int
     * @return boolean
     * @throws Exception
     */
    public boolean expandCircuit(Circuit circuit, int numSlotsDown, int numSlotsUp) throws Exception {
        //calculate the spectrum band at top
        int upperBand[] = new int[2];
        upperBand[0] = circuit.getSpectrumAssigned()[1] + 1;
        upperBand[1] = upperBand[0] + numSlotsUp - 1;

        //calculate the spectrum band at bottom
        int bottomBand[] = new int[2];
        bottomBand[1] = circuit.getSpectrumAssigned()[0] - 1;
        bottomBand[0] = bottomBand[1] - numSlotsDown + 1;

        int specAssigAt[] = circuit.getSpectrumAssigned();
        int newSpecAssigAt[] = specAssigAt.clone();

        //try to expand circuit
        if (numSlotsUp > 0){
            if(allocateSpectrum(circuit, upperBand, new ArrayList<>(circuit.getRoute().getLinkList()))){
                newSpecAssigAt[1] = upperBand[1];
            }else{
                throw new Exception("Bad RMLSA. Spectrum cant be allocated.");
            }
        }
        if(numSlotsDown > 0) {
            if(allocateSpectrum(circuit, bottomBand, new ArrayList<>(circuit.getRoute().getLinkList()))) {
                newSpecAssigAt[0] = bottomBand[0];
            }else{
                throw new Exception("Bad RMLSA. Spectrum cant be allocated.");
            }
        }

        circuit.setSpectrumAssigned(newSpecAssigAt);

        // Verifies if the expansion did not affect the QoT of the circuit or other already active circuits
        boolean QoT = isAdmissibleQualityOfTransmission(circuit);

        if(!QoT){
            if (numSlotsUp>0) {
                releaseSpectrum(circuit, upperBand, new ArrayList<>(circuit.getRoute().getLinkList()));
            }

            if (numSlotsDown>0) {
                releaseSpectrum(circuit, bottomBand, new ArrayList<>(circuit.getRoute().getLinkList()));
            }
            
            circuit.setSpectrumAssigned(specAssigAt);
            //isAdmissibleQualityOfTransmission(circuit); //recompute QoT
            computeQualityOfTransmission(circuit); // Recalculates the QoT and SNR of the circuit
            
        }else{
            this.updateNetworkPowerConsumption();
        }

        return QoT;
    }

    /**
     * Decreases the number of slots used by a given circuit
     * 
     * @param circuit Circuit
     * @param numSlotsDown int
     * @param numSlotsUp int
     * @throws Exception
     */
    public void retractCircuit(Circuit circuit, int numSlotsDown, int numSlotsUp) throws Exception {
        //calculate the spectrum band at top
        int upperBand[] = new int[2];
        upperBand[1] = circuit.getSpectrumAssigned()[1];
        upperBand[0] = upperBand[1] - numSlotsUp + 1;

        //calculate the spectrum band at bottom
        int bottomBand[] = new int[2];
        bottomBand[0] = circuit.getSpectrumAssigned()[0];
        bottomBand[1] = bottomBand[0] + numSlotsDown - 1;
        int newSpecAssign[] = circuit.getSpectrumAssigned().clone();
        
        if (numSlotsUp>0) {
            releaseSpectrum(circuit, upperBand, new ArrayList<>(circuit.getRoute().getLinkList()));
            newSpecAssign[1] = upperBand[0]-1;
        }

        if (numSlotsDown>0) {
            releaseSpectrum(circuit, bottomBand, new ArrayList<>(circuit.getRoute().getLinkList()));
            newSpecAssign[0] = bottomBand[1]+1;
        }
        
        circuit.setSpectrumAssigned(newSpecAssign);
        //isAdmissibleQualityOfTransmission(circuit); //compute QoT
        computeQualityOfTransmission(circuit); // Recalculates the QoT and SNR of the circuit
        
        this.updateNetworkPowerConsumption();
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
     * To find active circuits on the network with specified source
     * 
     * @param source String
     * @return List<Circuit>
     */
    public List<Circuit> searchForActiveCircuits(String source){
        List<Circuit> res = new ArrayList<>();
        for(List<Circuit> lc : activeCircuits.get(source).values()){
            res.addAll(lc);
        }
        return res;
    }
    
    /**
     * To find active circuits on the network
     * 
     * @return List<Circuit>
     */
    public List<Circuit> searchForActiveCircuits(){
        List<Circuit> res = new ArrayList<>();
        for(HashMap<String, List<Circuit>> hA : activeCircuits.values()){
            for(List<Circuit> lc : hA.values()){
                res.addAll(lc);
            }
        }
        return res;
    }
    
    /**
     * This method verifies the transmission quality of the circuit in the establishment 
     * and also verifies the transmission quality of the other already active circuits
     * 
     * @param circuit Circuit
     * @return boolean
     */
    protected boolean isAdmissibleQualityOfTransmission(Circuit circuit) throws Exception {
    	
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
    			
    			return QoTForOther;
    		}
    		
    		return false;
    	}
    	
		// If it does not check the QoT then it returns acceptable
		return true;
    }
    
    /**
     * This method verifies the quality of the transmission of the circuit
     * 
     * @param circuit Circuit
     * @return boolean - True, if QoT is acceptable, or false, otherwise
     */
    public boolean computeQualityOfTransmission(Circuit circuit){
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
    	TreeSet<Circuit> circuits = new TreeSet<Circuit>(); // Circuit list for test
    	HashMap<Circuit, Double> circuitsSNR = new HashMap<Circuit, Double>(); // To guard the SNR of the test list circuits
    	HashMap<Circuit, Boolean> circuitsQoT = new HashMap<Circuit, Boolean>(); // To guard the QoT of the test list circuits
		
    	// Search for all circuits that have links in common with the circuit under evaluation
		Route route = circuit.getRoute();
		for (Link link : route.getLinkList()) {
			
			// Picks up the active circuits that use the link
			TreeSet<Circuit> circuitsTemp = link.getCircuitList();
            for (Circuit circuitTemp : circuitsTemp) {
            	
            	// If the circuit is different from the circuit under evaluation and is not in the circuit list for test
                if (!circuit.equals(circuitTemp) && !circuits.contains(circuitTemp)) {
                    circuits.add(circuitTemp);
                }
            }
		}
		
		// Tests the QoT of circuits
        for (Circuit circuitTemp : circuits) {

        	// Stores the SNR and QoT values
        	circuitsSNR.put(circuitTemp, circuitTemp.getSNR());
            circuitsQoT.put(circuitTemp, circuitTemp.isQoT());
            
        	// Recalculates the QoT and SNR of the circuit
            boolean QoT = computeQualityOfTransmission(circuitTemp);
            if (!QoT) {
            	
            	// Returns the SNR and QoT values of circuits before the establishment of the circuit in evaluation
            	for (Circuit circuitAux : circuitsSNR.keySet()) {
            		circuitAux.setSNR(circuitsSNR.get(circuitAux));
            		circuitAux.setQoT(circuitsQoT.get(circuitAux));
            	}
            	
                return false;
            }
        }
		
		return true;
    }
    
    /**
	 * This method returns the power consumption of a given circuit.
	 * 
	 * @return double - power consumption (W)
	 */
	public double getPowerConsumption(Circuit circuit){
		double powerConsumption = EnergyConsumption.computePowerConsumptionBySegment(this, circuit, circuit.getRequiredBandwidth(), circuit.getRoute(), 0, circuit.getRoute().getNodeList().size() - 1, circuit.getModulation(), circuit.getSpectrumAssigned());
		circuit.setPowerConsumption(powerConsumption);
		return powerConsumption;
	}
	
	/**
	 * This method returns the list of active circuits
	 * 
	 * @return Circuit
	 */
	public TreeSet<Circuit> getConnections(){
		return connectionList;
	}
	
	/**
	 * This method adds a circuit to the list of active circuits
	 * 
	 * @param circuit Circuit
	 */
	public void addConnection(Circuit circuit){
		activeCircuits.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).add(circuit);
		
		if(!connectionList.contains(circuit)){
			connectionList.add(circuit);
		}
		
		List<Link> links = new ArrayList<Link>(circuit.getRoute().getLinkList());
	    for (int i = 0; i < links.size(); i++) {
	    	links.get(i).addCircuit(circuit);
	    }
	}
	
	/**
	 * This method removes a circuit from the active circuit list
	 * 
	 * @param circuit Circuit
	 */
	public void removeConnection(Circuit circuit){
		activeCircuits.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).remove(circuit);
		
		if(connectionList.contains(circuit)){
			connectionList.remove(circuit);
		}
		
		List<Link> links = new ArrayList<Link>(circuit.getRoute().getLinkList());
	    for (int i = 0; i < links.size(); i++) {
	    	links.get(i).removeCircuit(circuit);
	    }
	}
	
	/**
	 * This method checks whether the circuit blocking was by QoTN
	 * Returns true if the blocking was by QoTN and false otherwise
	 *
	 * @return boolean
	 */
	public boolean isBlockingByQoTN(List<Circuit> circuits){
		// Check if it is to test the QoT
        if(mesh.getPhysicalLayer().isActiveQoT()){
        	for(Circuit circuit : circuits){
                // Check if it is possible to compute the circuit QoT
                if(circuit.getRoute() != null && circuit.getModulation() != null && circuit.getSpectrumAssigned() != null){
                    // Check if the QoT is acceptable
                	if(!computeQualityOfTransmission(circuit)){
                    	return true;
                    }
                }
            }
	    }
		return false;
	}
	
	/**
	 * This method checks whether the circuit blocking was by QoTO
	 * Returns true if the blocking was by QoTO and false otherwise
	 *
	 * @return boolean
	 */
	public boolean isBlockingByQoTO(List<Circuit> circuits){
		// Check if it is to test the QoT of other already active circuits
        if(mesh.getPhysicalLayer().isActiveQoTForOther()){
        	for(Circuit circuit : circuits){
        		// Check if the QoTO is acceptable
                if(!circuit.isQoTForOther()){
                	return true;
                }
            }
	    }
		return false;
	}
	
	/**
	 * This method checks whether the circuit blocking was by fragmentation
	 * Returns true if the blocking was by fragmentation and false otherwise
	 *
	 * @return boolean
	 */
	public boolean isBlockingByFragmentation(List<Circuit> circuits){
	    for(Circuit circuit : circuits) {
            if (circuit.getRoute() == null) return false;

            List<Link> links = circuit.getRoute().getLinkList();
            List<int[]> merge = links.get(0).getFreeSpectrumBands();

            for (int i = 1; i < links.size(); i++) {
                merge = IntersectionFreeSpectrum.merge(merge, links.get(i).getFreeSpectrumBands());
            }

            int totalFree = 0;
            for (int[] band : merge) {
                totalFree += (band[1] - band[0] + 1);
            }

            Modulation mod = circuit.getModulation();
            if (mod == null) {
                mod = modulationSelection.getAvaliableModulations().get(0);
            }

            int numSlotsRequired = mod.requiredSlots(circuit.getRequiredBandwidth());
            if (totalFree > numSlotsRequired) {
                return true;
            }
        }
        return false;
	}
	
	/**
	 * Returns the list of modulation used by the circuit
	 * 
	 * @param circuit
	 * @return List<Modulation>
	 */
	public List<Modulation> getModulationsUsedByCircuit(Circuit circuit){
		List<Modulation> modList = new ArrayList<>();
		modList.add(circuit.getModulation());
		return modList;
	}
	
	/**
	 * This method returns the circuit SNR delta
	 * Can change according to the type of circuit
	 * 
	 * @return double - delta SNR (dB)
	 */
	public double getDeltaSNR(Circuit circuit){
		double SNR = mesh.getPhysicalLayer().computeSNRSegment(circuit, circuit.getRequiredBandwidth(), circuit.getRoute(), 0, circuit.getRoute().getNodeList().size() - 1, circuit.getModulation(), circuit.getSpectrumAssigned(), false);
		double SNRdB = PhysicalLayer.ratioForDB(SNR);
		
		double modulationSNRthreshold = circuit.getModulation().getSNRthreshold();
		double deltaSNR = SNRdB - modulationSNRthreshold;
		
		return deltaSNR;
	}

    private void updateNetworkPowerConsumption(){
        this.mesh.computesPowerConsmption(this);
    }
}
