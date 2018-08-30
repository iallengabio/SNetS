package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import grmlsa.GRMLSA;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.regeneratorAssignment.RegeneratorAssignmentAlgorithmInterface;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

/**
 * Class that represents the control plane for a Translucent Elastic Optical Network.
 * 
 * @author Alexandre
 */
public class TranslucentControlPlane extends ControlPlane {
	
	protected RegeneratorAssignmentAlgorithmInterface regeneratorAssignment;
	
	/**
	 * Creates a new instance of TranslucentControlPlane
	 * 
	 * @param mesh Mesh
	 * @param rmlsaType int
	 * @param trafficGroomingAlgorithm TrafficGroomingAlgorithmInterface
	 * @param integratedRMLSAAlgorithm IntegratedRMLSAAlgorithmInterface
	 * @param routingAlgorithm RoutingAlgorithmInterface
	 * @param spectrumAssignmentAlgorithm SpectrumAssignmentAlgorithmInterface
	 * @param regeneratorAssignment regeneratorAssignment
	 */
	public TranslucentControlPlane(Mesh mesh, int rmlsaType, TrafficGroomingAlgorithmInterface trafficGroomingAlgorithm, IntegratedRMLSAAlgorithmInterface integratedRMLSAAlgorithm, RoutingAlgorithmInterface routingAlgorithm, SpectrumAssignmentAlgorithmInterface spectrumAssignmentAlgorithm, RegeneratorAssignmentAlgorithmInterface regeneratorAssignment, ModulationSelectionAlgorithmInterface modulationSelection) {
		super(mesh, rmlsaType, trafficGroomingAlgorithm, integratedRMLSAAlgorithm, routingAlgorithm, spectrumAssignmentAlgorithm, modulationSelection);

		this.regeneratorAssignment = regeneratorAssignment;
	}

	/**
     * This method create a new translucent circuit.
     * 
     * @param rfc RequestForConnection
     * @return Circuit
     */
    public Circuit createNewCircuit(RequestForConnection rfc){
    	
    	Circuit circuit = new TranslucentCircuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		ArrayList<Circuit> circs = new ArrayList<>();
		circs.add(circuit);
		rfc.setCircuit(circs);
		
		return circuit;
    }
    
    /**
     * Method that allocate the regenerators used by a circuit
     * 
     * @param circuit TranslucentCircuit
     */
    public void allocateRegenerators(TranslucentCircuit circuit){
    	// Verifies that the list of regenerators is not empty
		if(circuit.getRegeneratorsNodesIndexList() != null && circuit.getRegeneratorsNodesIndexList().size() > 0){
			
			// Releases regenerators
			for(int i = 0; i < circuit.getRegeneratorsNodesIndexList().size(); i++){
				Node node = circuit.getRoute().getNode(circuit.getRegeneratorsNodesIndexList().get(i));
				
				if(!node.getRegenerators().allocatesRegenerators(circuit)){
					System.err.println("ERROR: RequestTranslucent class. Method liberateNodesRegenerators(). Trying to allocate non-free regenerator!");
				}
			}
		}	
    }
    
	/**
	 * Method that releases the regenerators used by a circuit.
	 * To avoid an error in the metrics you should not delete the regenerators from the 
	 * list of regenerators used by a circuit.
	 * 
	 * @param circuit TranslucentCircuit
	 */
	public void releasesRegenerators(TranslucentCircuit circuit) {
		
		// Verifies that the list of regenerators is not empty
		if(circuit.getRegeneratorsNodesIndexList() != null && circuit.getRegeneratorsNodesIndexList().size() > 0){
			
			// Releases regenerators
			for(int i = 0; i < circuit.getRegeneratorsNodesIndexList().size(); i++){
				Node node = circuit.getRoute().getNode(circuit.getRegeneratorsNodesIndexList().get(i));
				
				if(!node.getRegenerators().releasesRegenerators(circuit)){
					System.err.println("ERROR: RequestTranslucent class. Method liberateNodesRegenerators(). Trying to release unallocated regenerator!");
				}
			}
		}	
	}
	
	/**
     * This method verifies the quality of the transmission of the circuit
     * 
     * @param circuit Circuit
     * @return boolean - True, if QoT is acceptable, or false, otherwise
     */
	@Override
	public boolean computeQualityOfTransmission(Circuit circuit){
    	boolean minQoT = true;
		int sourceNodeIndex = 0;
		double minSNRdB = Double.MAX_VALUE;
		Route route = circuit.getRoute();
		
		int mumberTransparentSegments = ((TranslucentCircuit)circuit).getRegeneratorsNodesIndexList().size() + 1;
		for(int i = 0; i < mumberTransparentSegments; i++){
			
			int destinationNodeIndex = route.getNodeList().size() - 1;
			if(i < mumberTransparentSegments - 1){
				destinationNodeIndex = ((TranslucentCircuit)circuit).getRegeneratorsNodesIndexList().get(i);
			}
			
			Node noSource = route.getNode(sourceNodeIndex);
			Node noDestination = route.getNode(sourceNodeIndex + 1);
			Link link = noSource.getOxc().linkTo(noDestination.getOxc());
			
			Modulation mod = circuit.getModulationByLink(link);
			int sa[] = circuit.getSpectrumAssignedByLink(link);
			
			double SNR = getMesh().getPhysicalLayer().computeSNRSegment(circuit, circuit.getRequiredBandwidth(), route, sourceNodeIndex, destinationNodeIndex, mod, sa, false);
			double SNRdB = PhysicalLayer.ratioForDB(SNR);
			
			boolean QoT = getMesh().getPhysicalLayer().isAdmissible(mod, SNRdB, SNR); 
			if(!QoT){
				minQoT = false;
				if(SNRdB < minSNRdB)
				    minSNRdB = SNRdB;
			}
			circuit.setSNR(SNRdB);
			circuit.setQoT(QoT);
			
			sourceNodeIndex = destinationNodeIndex;
		}
		
		if(!minQoT){
			circuit.setSNR(minSNRdB);
			circuit.setQoT(minQoT);
		}
		return minQoT;
    }
	
	/**
     * This method is called after executing RMLSA algorithms to allocate resources in the network
     *
     * @param circuit Circuit
     */
	@Override
    protected void allocateCircuit(Circuit circuit) throws Exception {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        
        allocateSpectrum(circuit, links);
        
        // Allocates transmitter and receiver
        circuit.getSource().getTxs().allocatesTransmitters();
        circuit.getDestination().getRxs().allocatesReceivers();
        
        addConnection(circuit);
        
        // Allocates regenerators
        allocateRegenerators((TranslucentCircuit)circuit);
    }
    
	/**
     * This method allocates the spectrum band selected for the circuit in the route links
     * 
     * @param circuit Circuit
     * @param links List<Link>
     */
	protected void allocateSpectrum(Circuit circuit, List<Link> links) throws Exception {
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            int[] band = circuit.getSpectrumAssignedByLink(link);
            
            link.useSpectrum(band);
        }
    }
    
	/**
     * Releases the resources being used by a given circuit
     *
     * @param circuit
     */
	@Override
    public void releaseCircuit(Circuit circuit) throws Exception {
        Route route = circuit.getRoute();

        releaseSpectrum(circuit, route.getLinkList());

        // Release transmitter and receiver
        circuit.getSource().getTxs().releasesTransmitters();
        circuit.getDestination().getRxs().releasesReceivers();
        
        removeConnection(circuit);
        
        // Release regenerators
        releasesRegenerators((TranslucentCircuit)circuit);
    }
    
    /**
     * This method releases the allocated spectrum for the circuit
     * 
     * @param circuit Circuit
     * @param links List<Link>
     */
	protected void releaseSpectrum(Circuit circuit, List<Link> links) throws Exception {
    	for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            int band[] = circuit.getSpectrumAssignedByLink(link);
        	
            link.liberateSpectrum(band);
        }
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
                	return regeneratorAssignment.assignRegenerator((TranslucentCircuit)circuit, this);
                }
        }

        return false;
    }
	
    /**
     * This method selects how modulation format selection and spectrum allocation will be applied.
     * 
     * @param circuit TranslucentCircuit
     * @return boolean - True, if you could define the modulation format and put the spectrum, or false, otherwise
     */
    public boolean strategySelection(TranslucentCircuit circuit){
    	// Releases the regenerators allocated so that they can be later allocated to the other network resources
		releasesRegenerators(circuit);
		
		if(circuit.getRegeneratorsNodesIndexList().size() == 0){
			return withoutRegenerator(circuit, circuit.getRoute());
			
		} else {
			return withRegenerator(circuit, circuit.getRoute(), circuit.getRegeneratorsNodesIndexList());
		}
    }
    
    /**
	 * This method attempts to define the modulation format and the spectrum to be allocated in each link of the route selected for the circuit
	 * 
	 * @param circuit - Circuit
	 * @param route - Route
	 * @return boolean - True, if you could define the modulation format and put the spectrum, or false, otherwise
	 */
	public boolean withoutRegenerator(TranslucentCircuit circuit, Route route){
		Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, this);
		
		if(mod != null){
			circuit.setModulation(mod);
			
			HashMap<Link, Modulation> modulationByLink = new HashMap<Link, Modulation>();
			Vector<Link> linkList = route.getLinkList();
			for(int l = 0; l < linkList.size(); l++){
				modulationByLink.put(linkList.get(l), mod);
			}
			circuit.setModulationByLink(modulationByLink);
			
			if(spectrumAssignment.assignSpectrum(mod.requiredSlots(circuit.getRequiredBandwidth()), circuit, this)){
				int sa[] = circuit.getSpectrumAssigned();
				
				HashMap<Link, int[]> spectrumAssignedByLink = new HashMap<Link, int[]>();
				for(int l = 0; l < linkList.size(); l++){
					spectrumAssignedByLink.put(linkList.get(l), sa);
				}
				circuit.setSpectrumAssignedByLink(spectrumAssignedByLink);;
				
				return true;
			}
		}
		
		return false;		
	}
	
	/**
	 * This method tries to define the modulation format and the spectrum to be allocated in each link of the route 
	 * selected for the circuit taking into account the transparent segments among the regenerators selected for the circuit
	 * 
	 * @param circuit - TranslucentCircuit
	 * @param route - Route
	 * @param regeneratorsNodesIndexList - List<Integer>
	 * @return True, if you could define the modulation format and put the spectrum, or false, otherwise
	 */
	public boolean withRegenerator(TranslucentCircuit circuit, Route route, List<Integer> regeneratorsNodesIndexList){
		HashMap<Link, int[]> spectrumAssignedByLink = new HashMap<Link, int[]>();
		HashMap<Link, Modulation> modulationByLink = new HashMap<Link, Modulation>();
		
		int sourceNodeIndex = 0;
		int numberTransparentSegments = regeneratorsNodesIndexList.size() + 1;
		for(int i = 0; i < numberTransparentSegments; i++){
			
			int destinationNodeIndex = route.getNodeList().size() - 1;
			if(i < numberTransparentSegments - 1){
				destinationNodeIndex = regeneratorsNodesIndexList.get(i);
			}
			
			Node sourceNode = route.getNode(sourceNodeIndex);
			Node destinationNode = route.getNode(sourceNodeIndex + 1);
			Link link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
			
			List<int[]> composition = link.getFreeSpectrumBands();
			
			for(int l = sourceNodeIndex + 1; l < destinationNodeIndex; l++){
				sourceNode = route.getNode(l);
				destinationNode = route.getNode(l + 1);
				link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
				
				composition = IntersectionFreeSpectrum.merge(composition, link.getFreeSpectrumBands());
			}
			
			tryAssignModulationAndSpectrum(circuit, route, sourceNodeIndex, destinationNodeIndex, composition);
			int sa[] = circuit.getSpectrumAssigned();
			Modulation mod = circuit.getModulation();
			
			if(sa == null){
				// The circuit will be blocked since it was not possible to allocate spectrum
				// Steps to avoid error in the metrics
				destinationNodeIndex = route.getNodeList().size() - 1;
				
				for(int l = sourceNodeIndex; l < destinationNodeIndex; l++){
					sourceNode = route.getNode(l);
					destinationNode = route.getNode(l + 1);
					link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
					
					spectrumAssignedByLink.put(link, sa);
					modulationByLink.put(link, mod);
				}
				
				circuit.setSpectrumAssignedByLink(spectrumAssignedByLink);
				circuit.setModulationByLink(modulationByLink);
				
				return false;
			}
			
			for(int l = sourceNodeIndex; l < destinationNodeIndex; l++){
				sourceNode = route.getNode(l);
				destinationNode = route.getNode(l + 1);
				link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
				
				spectrumAssignedByLink.put(link, sa);
				modulationByLink.put(link, mod);
			}
			
			sourceNodeIndex = destinationNodeIndex;
		}

		circuit.setSpectrumAssignedByLink(spectrumAssignedByLink);
		circuit.setModulationByLink(modulationByLink);
		
		circuit.setSpectrumAssigned(spectrumAssignedByLink.get(route.getLinkList().firstElement()));
		circuit.setModulation(modulationByLink.get(route.getLinkList().firstElement()));
		
		return true;
	}
	
	/**
	 * This method attempts to choose a modulation format according to the QoT of the transmission also tries to choose 
	 * a range of spectrum for the request according to the selected modulation
	 * 
	 * @param circuit - TranslucentCircuit
	 * @param route - Route
	 * @param sourceNodeIndex - int
	 * @param destinationNodeIndex - int
	 * @param composition - List<int[]>
	 * @return boolean
	 */
	public boolean tryAssignModulationAndSpectrum(TranslucentCircuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, List<int[]> composition){
		boolean flagQoT = true; // Assuming that the circuit QoT starts as acceptable
		boolean flagSuccess = false;
		
		// Modulation and spectrum selected
		Modulation chosenMod = null;
		int chosenBand[] = null;
		
		// Modulation which at least allocates spectrum, used to avoid error in metrics
		Modulation alternativeMod = null;
		int alternativeBand[] = null;
		
		List<Modulation> avaliableModulations = modulationSelection.getAvaliableModulations();
		
		for(int i = 0; i < avaliableModulations.size(); i++){
			Modulation mod = avaliableModulations.get(i);
			int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			
			int band[] = spectrumAssignment.policy(numberOfSlots, composition, circuit, this);
			if(band != null){
				if(alternativeMod == null){
					alternativeMod = mod; // The first modulation that was able to allocate spectrum
					alternativeBand = band;
				}
				
				boolean flag = mesh.getPhysicalLayer().isAdmissibleModultionBySegment(circuit, route, sourceNodeIndex, destinationNodeIndex, mod, band);
				if(flag){
					chosenMod = mod; // Save the modulation that has admissible QoT
					chosenBand = band;
					
					flagSuccess = true;
				}
			}
		}
		
		if(chosenMod == null){ // QoT is not acceptable for all modulations
			chosenMod = avaliableModulations.get(0); // To avoid metric error
			chosenBand = null;
			
			if(alternativeMod != null){ // Allocated spectrum using some modulation, but the QoT was inadmissible
				chosenMod = alternativeMod;
				chosenBand = alternativeBand;
				
				flagQoT = false; // To mark that the blockade was by QoT inadmissible
			}
		}
		
		// Configures the circuit information. They can be used by the method that requested the modulation selection
		circuit.setModulation(chosenMod);
		circuit.setSpectrumAssigned(chosenBand);
		circuit.setQoT(flagQoT);
		
		return flagSuccess;
	}
	
	/**
	 * This method returns the power consumption of a given circuit.
	 * 
	 * @return double - power consumption (W)
	 */
	@Override
	public double getPowerConsumption(Circuit circuit){
		double PCtotal = 0.0;
		
		int sourceNodeIndex = 0;
		Route route = circuit.getRoute();
		
		int numberOfTransparentSegments = ((TranslucentCircuit)circuit).getRegeneratorsNodesIndexList().size() + 1;
		for(int i = 0; i < numberOfTransparentSegments; i++){
			
			int destinationNodeIndex = route.getNodeList().size() - 1;
			if(i < numberOfTransparentSegments - 1){
				destinationNodeIndex = ((TranslucentCircuit)circuit).getRegeneratorsNodesIndexList().get(i);
			}
			
			Node sourceNode = route.getNode(sourceNodeIndex);
			Node destinationNode = route.getNode(sourceNodeIndex + 1);
			Link link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
			
			Modulation mod = circuit.getModulationByLink(link);
			int sa[] = circuit.getSpectrumAssignedByLink(link);
			
			double PCsegment = EnergyConsumption.computePowerConsumptionBySegment(this, circuit, circuit.getRequiredBandwidth(), route, sourceNodeIndex, destinationNodeIndex, mod, sa);
			PCtotal += PCsegment;
			
			sourceNodeIndex = destinationNodeIndex;
		}
		
		circuit.setPowerConsumption(PCtotal);
		
		return PCtotal;
	}
	
	/**
	 * This method checks whether the circuit blocking was by QoTN
	 * Returns true if the blocking was by QoTN and false otherwise
	 *
	 * @return boolean
	 */
	@Override
	public boolean isBlockingByQoTN(List<Circuit> circuits){
		// Check if it is to test the QoT
		for(Circuit circuit : circuits) {
			if (mesh.getPhysicalLayer().isActiveQoT()) {

				if (circuit.getRoute() == null) return false;

				Route route = circuit.getRoute();
				int sourceNodeIndex = 0;
				int mumberTransparentSegments = ((TranslucentCircuit) circuit).getRegeneratorsNodesIndexList().size() + 1;

				// Verifies by transparent segment if they have modulation and spectrum selected
				for (int i = 0; i < mumberTransparentSegments; i++) {

					int destinationNodeIndex = route.getNodeList().size() - 1;
					if (i < mumberTransparentSegments - 1) {
						destinationNodeIndex = ((TranslucentCircuit) circuit).getRegeneratorsNodesIndexList().get(i);
					}

					Node noSource = route.getNode(sourceNodeIndex);
					Node noDestination = route.getNode(sourceNodeIndex + 1);
					Link link = noSource.getOxc().linkTo(noDestination.getOxc());

					Modulation mod = circuit.getModulationByLink(link);
					int sa[] = circuit.getSpectrumAssignedByLink(link);

					// If it does not have modulation or spectrum by segment the blockade is not by QoT
					if (mod == null || sa == null) {
						return false;
					}

					sourceNodeIndex = destinationNodeIndex;
				}

				// Now you can check the QoT by transparent segment
				if(!computeQualityOfTransmission(circuit)){
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
	@Override
	public boolean isBlockingByFragmentation(List<Circuit> circuits){
		for(Circuit circuit : circuits) {
			if (circuit.getRoute() == null) return false;

			Route route = circuit.getRoute();
			int sourceNodeIndex = 0;
			int mumberTransparentSegments = ((TranslucentCircuit) circuit).getRegeneratorsNodesIndexList().size() + 1;

			for (int i = 0; i < mumberTransparentSegments; i++) {

				int destinationNodeIndex = route.getNodeList().size() - 1;
				if (i < mumberTransparentSegments - 1) {
					destinationNodeIndex = ((TranslucentCircuit) circuit).getRegeneratorsNodesIndexList().get(i);
				}

				Node sourceNode = route.getNode(sourceNodeIndex);
				Node destinationNode = route.getNode(sourceNodeIndex + 1);
				Link link = sourceNode.getOxc().linkTo(destinationNode.getOxc());

				List<int[]> merge = link.getFreeSpectrumBands();
				for (int n = sourceNodeIndex; n < destinationNodeIndex; n++) {
					sourceNode = route.getNode(n);
					destinationNode = route.getNode(n + 1);
					link = sourceNode.getOxc().linkTo(destinationNode.getOxc());

					merge = IntersectionFreeSpectrum.merge(merge, link.getFreeSpectrumBands());
				}

				int totalFree = 0;
				for (int[] band : merge) {
					totalFree += (band[1] - band[0] + 1);
				}

				Modulation mod = circuit.getModulationByLink(link);
				if (mod == null) {
					mod = modulationSelection.getAvaliableModulations().get(0);
				}

				int numSlotsRequired = mod.requiredSlots(circuit.getRequiredBandwidth());
				if (totalFree > numSlotsRequired) {
					return true;
				}

				sourceNodeIndex = destinationNodeIndex;
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
	@Override
	public List<Modulation> getModulationsUsedByCircuit(Circuit circuit){
		List<Modulation> modList = new ArrayList<>();

		HashMap<Link, Modulation> modLink = ((TranslucentCircuit)circuit).getModulationByLink();
		for(Link link : modLink.keySet()){
			modList.add(((TranslucentCircuit)circuit).getModulationByLink(link));
		}
		
		return modList;
	}
}
