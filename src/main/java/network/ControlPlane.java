package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import grmlsa.GRMLSA;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionByDistanceAndBandwidth;
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
public class ControlPlane implements Serializable {

    protected int rsaType;
    protected RoutingAlgorithmInterface routing;
    protected SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    protected IntegratedRMLSAAlgorithmInterface integrated;
    protected ModulationSelectionAlgorithmInterface modulationSelection;
    protected TrafficGroomingAlgorithmInterface grooming;
    
    protected ModulationSelectionAlgorithmInterface modSelectByDistForEvaluation; // used to check the blocking types
    
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
        
        this.modSelectByDistForEvaluation = new ModulationSelectionByDistanceAndBandwidth();
        
        setMesh(mesh);
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
        
        mesh.computesPowerConsmption(this);
        
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
        int band[] = circuit.getSpectrumAssigned();
        
        if(!allocateSpectrum(circuit, band, route.getLinkList())){
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
            
            if(!link.useSpectrum(band)){ //spectrum already in use
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
    	
    	// Check if there are free transmitters
        if(circuit.getSource().getTxs().hasFreeTransmitters()){
        	// Check if there are free receivers
            if(circuit.getDestination().getRxs().hasFreeRecivers()){
            	
                // Can allocate spectrum
                if (tryEstablishNewCircuit(circuit)) {
                	
                    // QoT verification
                    if(isAdmissibleQualityOfTransmission(circuit)){
                        updateNetworkPowerConsumption();
                        allocateCircuit(circuit);
                        
                        return true; // Admits the circuit
                    }
                }
                
                if(isBlockingByQoTN(circuit)) {
                    if(shouldTestFragmentation(circuit)){ // check whether it is to test whether the blocking was by fragmentation
 	                    if(isBlockingByFragmentation(circuit)){
 	                        circuit.setBlockCause(Circuit.BY_FRAGMENTATION);
 	                    }else{
 	                        circuit.setBlockCause(Circuit.BY_OTHER);
 	                    }
 	                }else{
 	                	circuit.setBlockCause(Circuit.BY_QOTN);
 	                }
            	}else if(!circuit.isQoTForOther()) {
            		circuit.setBlockCause(Circuit.BY_QOTO);
            	}else {
            		circuit.setBlockCause(Circuit.BY_OTHER);
            	}
                
            }else{
                circuit.setBlockCause(Circuit.BY_LACK_RX);
            }
        }else{
            circuit.setBlockCause(Circuit.BY_LACK_TX);
        }
        circuit.setWasBlocked(true);
        return false; // Rejects the circuit
    }

    private boolean shouldTestFragmentation(Circuit circuit) {
        Modulation modBD = modSelectByDistForEvaluation.selectModulation(circuit, circuit.getRoute(), spectrumAssignment, this);
        Modulation modCirc = circuit.getModulation();
        return !(modBD.getSNRthreshold() >= modCirc.getSNRthreshold());
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
        int currentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
        int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(currentSlots+numSlotsDown+numSlotsUp>maxAmplitude) return false;

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
            computeQualityOfTransmission(circuit, null, false); // Recalculates the QoT and SNR of the circuit
            
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
        computeQualityOfTransmission(circuit, null, false); // Recalculates the QoT and SNR of the circuit
        
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
    		if(computeQualityOfTransmission(circuit, null, false)){
    			boolean QoTForOther = true;
    			
    			// Check if it is to test the QoT of other already active circuits
    			if(mesh.getPhysicalLayer().isActiveQoTForOther()){
    				
    				// Calculates the QoT of the other circuits
    				QoTForOther = computeQoTForOther(circuit);
    				circuit.setQoTForOther(QoTForOther);
    			}
    			
    			return QoTForOther;
    		}
    		
    		return false; // Circuit can not be established
    	}
    	
		// If it does not check the QoT then it returns acceptable
		return true;
    }
    
    /**
     * This method verifies the quality of the transmission of the circuit
     * The circuit in question has already allocated the network resources
     * 
     * @param circuit Circuit
     * @param circuitTest Circuit - Circuit used to verify the impact on the other circuit informed
     * @param addTestCircuit boolean - To add the test circuit to the circuit list
     * @return boolean - True, if QoT is acceptable, or false, otherwise
     */
    public boolean computeQualityOfTransmission(Circuit circuit, Circuit testCircuit, boolean addTestCircuit){
    	double SNR = mesh.getPhysicalLayer().computeSNRSegment(circuit, circuit.getRoute(), 0, circuit.getRoute().getNodeList().size() - 1, circuit.getModulation(), circuit.getSpectrumAssigned(), testCircuit, addTestCircuit);
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
    public boolean computeQoTForOther(Circuit circuit){
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
            boolean QoT = computeQualityOfTransmission(circuitTemp, circuit, true);
            
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
     * Calculates the amount of SNR impacted by a circuit in other circuits
     * 
     * @param circuit Circuit
     * @return double - SNR impact
     */
    public double computesImpactOnSNROther(Circuit circuit){
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
		
		double SNRimpact = 0.0;
		double SNRtemp = 0.0;
		double SNRtemp2 = 0.0;
		double SNRdif = 0.0;
		
        for (Circuit circuitTemp : circuits) {
        	
        	// Stores the SNR and QoT values
        	circuitsSNR.put(circuitTemp, circuitTemp.getSNR());
            circuitsQoT.put(circuitTemp, circuitTemp.isQoT());
            SNRtemp2 = circuitTemp.getSNR();
        	
        	// Computes the SNR of the circuitTemp without considering the circuit
            computeQualityOfTransmission(circuitTemp, circuit, false);
            SNRtemp = circuitTemp.getSNR();
            
            // Computes the SNR of the circuitTemp considering the circuit
        	//computeQualityOfTransmission(circuitTemp, circuit, true);
        	//SNRtemp2 = circuitTemp.getSNR();
            
            circuitTemp.setSNR(circuitsSNR.get(circuitTemp));
            circuitTemp.setQoT(circuitsQoT.get(circuitTemp));
            
        	SNRdif = SNRtemp - SNRtemp2;
        	if(SNRdif < 0.0) {
        		SNRdif = -1.0 * SNRdif;
        	}
        	
        	SNRimpact += SNRdif;
        }
        
		return SNRimpact;
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
		
	    for (int i = 0; i < circuit.getRoute().getLinkList().size(); i++) {
	    	circuit.getRoute().getLinkList().get(i).addCircuit(circuit);
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
		
	    for (int i = 0; i < circuit.getRoute().getLinkList().size(); i++) {
	    	circuit.getRoute().getLinkList().get(i).removeCircuit(circuit);
	    }
	}
	
	/**
	 * This method checks whether the circuit blocking was by QoTN
	 * Returns true if the blocking was by QoTN and false otherwise
	 *
	 * @return boolean
	 */
	public boolean isBlockingByQoTN(Circuit circuit){
		// Check if it is to test the QoT
        if(mesh.getPhysicalLayer().isActiveQoT()){
            // Check if it is possible to compute the circuit QoT
            if(circuit.getRoute() != null && circuit.getModulation() != null && circuit.getSpectrumAssigned() != null){
                // Check if the QoT is acceptable
                if(!computeQualityOfTransmission(circuit, null, false)){
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
	public boolean isBlockingByFragmentation(Circuit circuit){
		
        if (circuit.getRoute() == null) return false;
        
        // For fragmentation verification
        Modulation modBD = modSelectByDistForEvaluation.selectModulation(circuit, circuit.getRoute(), spectrumAssignment, this);
        
        List<Link> links = circuit.getRoute().getLinkList();
        List<int[]> merge = links.get(0).getFreeSpectrumBands();
        
        for (int i = 1; i < links.size(); i++) {
            merge = IntersectionFreeSpectrum.merge(merge, links.get(i).getFreeSpectrumBands());
        }
        
        int totalFree = 0;
        for (int[] band : merge) {
            totalFree += (band[1] - band[0] + 1);
        }
        
        int numSlotsRequired = modBD.requiredSlots(circuit.getRequiredBandwidth());
        if (totalFree > numSlotsRequired) {
            return true;
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
		double SNR = mesh.getPhysicalLayer().computeSNRSegment(circuit, circuit.getRoute(), 0, circuit.getRoute().getNodeList().size() - 1, circuit.getModulation(), circuit.getSpectrumAssigned(), null, false);
		double SNRdB = PhysicalLayer.ratioForDB(SNR);
		
		double modulationSNRthreshold = circuit.getModulation().getSNRthreshold();
		double deltaSNR = SNRdB - modulationSNRthreshold;
		
		return deltaSNR;
	}
	
	/**
	 * Updates the network's power consumption
	 */
    private void updateNetworkPowerConsumption(){
        this.mesh.computesPowerConsmption(this);
    }
}
