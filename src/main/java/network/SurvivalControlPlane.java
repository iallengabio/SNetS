package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.survival.SurvivalStrategyInterface;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

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
	 * @param survivalStrategy SurvivalStrategyInterface
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
     * Verifies if there are free transmitters and receivers for the establishment of the new circuit
     * 
     * @param circuit Circuit
     * @return boolean
     */
    public boolean thereAreFreeTransponders(Circuit circuit){
    	return survivalStrategy.thereAreFreeTransponders(circuit);
    }
    
    /**
     * This method tries to answer a given request by allocating the necessary resources to the same one
     *
     * @param circuit Circuit
     * @return boolean
     */
    protected boolean tryEstablishNewCircuit(Circuit circuit) {
        return survivalStrategy.applyStrategy(circuit, this);
    }
    
    @Override
    public void allocateCircuit(Circuit circuit) throws Exception {
        Route workRoute = circuit.getRoute();
        int workBand[] = circuit.getSpectrumAssigned();
        
        if(!allocateSpectrum(circuit, workBand, workRoute.getLinkList())){
            throw new Exception("bad RMLSA choice. Spectrum cant be allocated.");
        }
        
        Route backupRoute = ((SurvivalCircuit)circuit).getBackupRoute();
        int backupBand[] = ((SurvivalCircuit)circuit).getSpectrumAssignedByBackupRoute();
        
        if(!allocateSpectrum(circuit, backupBand, backupRoute.getLinkList())){
            throw new Exception("bad RMLSA choice. Spectrum cant be allocated.");
        }
        
        // Allocates transmitter and receiver
        circuit.getSource().getTxs().allocatesTransmitters(((SurvivalCircuit)circuit).getRequiredNumberOfTxs());
        circuit.getDestination().getRxs().allocatesReceivers(((SurvivalCircuit)circuit).getRequiredNumberOfRxs());
        
        addConnection(circuit);
    }
    
    @Override
    public void releaseCircuit(Circuit circuit) throws Exception {
        Route workRoute = circuit.getRoute();
        int workBand[] = circuit.getSpectrumAssigned();
        
        releaseSpectrum(circuit, workBand, workRoute.getLinkList());
        
        Route backupRoute = ((SurvivalCircuit)circuit).getBackupRoute();
        int backupBand[] = ((SurvivalCircuit)circuit).getSpectrumAssignedByBackupRoute();
        
        releaseSpectrum(circuit, backupBand, backupRoute.getLinkList());

        // Release transmitter and receiver
        circuit.getSource().getTxs().releasesTransmitters(((SurvivalCircuit)circuit).getRequiredNumberOfTxs());
        circuit.getDestination().getRxs().releasesReceivers(((SurvivalCircuit)circuit).getRequiredNumberOfRxs());

        removeConnection(circuit);

        updateNetworkPowerConsumption();
    }
    
    @Override
    protected boolean isAdmissibleQualityOfTransmission(Circuit circuit) throws Exception {
    	
    	// Check if it is to test the QoT
    	if(mesh.getPhysicalLayer().isActiveQoT()){
    		
    		// Verifies the QoT of the current circuit
    		if(computeQualityOfTransmission(circuit)){
    			boolean QoTForOther = true;
    			
    			// Check if it is to test the QoT of other already active circuits
    			if(mesh.getPhysicalLayer().isActiveQoTForOther()){
    				
    				// Calculates the QoT of the other circuits
    				//QoTForOther = computeQoTForOther(circuit);
    				//circuit.setQoTForOther(QoTForOther);
    				
    				QoTForOther = circuit.isQoTForOther();
    			}
    			
    			return QoTForOther;
    		}
    		
    		return false;
    	}
    	
		// If it does not check the QoT then it returns acceptable
		return true;
    }

    @Override
    public boolean computeQualityOfTransmission(Circuit circuit){
    	Route workRoute = circuit.getRoute();
        int workBand[] = circuit.getSpectrumAssigned();
        Modulation workMod = circuit.getModulation();
    	
    	double workSNR = mesh.getPhysicalLayer().computeSNRSegment(circuit, workRoute, 0, workRoute.getNodeList().size() - 1, workMod, workBand, false);
		double workSNRdB = PhysicalLayer.ratioForDB(workSNR);
		circuit.setSNR(workSNRdB);
		
		boolean workQoT = mesh.getPhysicalLayer().isAdmissible(workMod, workSNRdB, workSNR);
		circuit.setQoT(workQoT);
		
        Route backupRoute = ((SurvivalCircuit)circuit).getBackupRoute();
        int backupBand[] = ((SurvivalCircuit)circuit).getSpectrumAssignedByBackupRoute();
        Modulation backupMod = ((SurvivalCircuit)circuit).getModulationByBackupRoute();
        
        double backupSNR = mesh.getPhysicalLayer().computeSNRSegment(circuit, backupRoute, 0, backupRoute.getNodeList().size() - 1, backupMod, backupBand, false);
		double backupSNRdB = PhysicalLayer.ratioForDB(backupSNR);
		
		boolean backupQoT = mesh.getPhysicalLayer().isAdmissible(backupMod, backupSNRdB, backupSNR);
		if(workSNRdB > backupSNRdB){
			circuit.setSNR(backupSNRdB);
			circuit.setQoT(backupQoT);
		}
		
		return (workQoT && backupQoT);
    }
    
    @Override
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
		
        Route backupRoute = ((SurvivalCircuit)circuit).getBackupRoute();
		for (Link link : backupRoute.getLinkList()) {
			
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
            
        	// Recalculates the QoT and SNR of the circuits
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
    
    @Override
    public boolean isBlockingByQoTN(List<Circuit> circuits){
		// Check if it is to test the QoT
        if(mesh.getPhysicalLayer().isActiveQoT()){
        	for(Circuit circuit : circuits){
        		
        		Route workRoute = circuit.getRoute();
                int workBand[] = circuit.getSpectrumAssigned();
                Modulation workMod = circuit.getModulation();
                
                Route backupRoute = ((SurvivalCircuit)circuit).getBackupRoute();
                int backupBand[] = ((SurvivalCircuit)circuit).getSpectrumAssignedByBackupRoute();
                Modulation backupMod = ((SurvivalCircuit)circuit).getModulationByBackupRoute();
                
                // Check if it is possible to compute the circuit QoT
                if(workRoute != null && workMod != null && workBand != null
                   && backupRoute != null && backupMod != null && backupBand != null){
                	
                    // Check if the QoT is acceptable
                	if(!computeQualityOfTransmission(circuit)){
                    	return true;
                    }
                }
            }
	    }
		return false;
	}
    
    @Override
    public boolean isBlockingByFragmentation(List<Circuit> circuits){
	    for(Circuit circuit : circuits) {
	    	
	    	Route workRoute = circuit.getRoute();
            Modulation workMod = circuit.getModulation();
            
            Route backupRoute = ((SurvivalCircuit)circuit).getBackupRoute();
            Modulation backupMod = ((SurvivalCircuit)circuit).getModulationByBackupRoute();
            
            if (workRoute != null){

	            List<Link> links = workRoute.getLinkList();
	            List<int[]> merge = links.get(0).getFreeSpectrumBands();
	
	            for (int i = 1; i < links.size(); i++) {
	                merge = IntersectionFreeSpectrum.merge(merge, links.get(i).getFreeSpectrumBands());
	            }
	
	            int totalFree = 0;
	            for (int[] band : merge) {
	                totalFree += (band[1] - band[0] + 1);
	            }
	            
	            Modulation mod = workMod;
	            if (mod == null) {
	                mod = modulationSelection.getAvaliableModulations().get(0);
	            }
	            
	            int numSlotsRequired = mod.requiredSlots(circuit.getRequiredBandwidth());
	            if (totalFree > numSlotsRequired) {
	                return true;
	            }
            }
            
            if (backupRoute != null){

	            List<Link> links = backupRoute.getLinkList();
	            List<int[]> merge = links.get(0).getFreeSpectrumBands();
	
	            for (int i = 1; i < links.size(); i++) {
	                merge = IntersectionFreeSpectrum.merge(merge, links.get(i).getFreeSpectrumBands());
	            }
	
	            int totalFree = 0;
	            for (int[] band : merge) {
	                totalFree += (band[1] - band[0] + 1);
	            }
	            
	            Modulation mod = backupMod;
	            if (mod == null) {
	                mod = modulationSelection.getAvaliableModulations().get(0);
	            }
	            
	            int numSlotsRequired = mod.requiredSlots(circuit.getRequiredBandwidth());
	            if (totalFree > numSlotsRequired) {
	                return true;
	            }
            }
        }
	    
        return false;
	}
    
}
