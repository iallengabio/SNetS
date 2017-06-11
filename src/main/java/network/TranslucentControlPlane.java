package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import grmlsa.GRMLSA;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
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
	

	public TranslucentControlPlane(Mesh mesh, int rmlsaType, TrafficGroomingAlgorithmInterface trafficGroomingAlgorithm,
			                       IntegratedRMLSAAlgorithmInterface integratedRSAAlgoritm, RoutingAlgorithmInterface routingInterface,
			                       SpectrumAssignmentAlgorithmInterface spectrumAssignmentAlgoritm) {
		super(mesh, rmlsaType, trafficGroomingAlgorithm, integratedRSAAlgoritm, routingInterface, spectrumAssignmentAlgoritm);

		
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
		rfc.setCircuit(circuit);
		
		return circuit;
    }
    
	/**
	 * Method that releases the regenerators used by a circuit.
	 * To avoid an error in the metrics you should not delete the regenerators from the 
	 * list of regenerators used by a circuit.
	 * 
	 * @param TranslucentCircuit circuit
	 */
	public void liberateNodesRegenerators(TranslucentCircuit circuit) {
		
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
	protected boolean computeQualityOfTransmission(Circuit circuit){
    	
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
     * This method allocates the spectrum band selected for the circuit in the route links
     * 
     * @param circuit Circuit
     * @param chosen int[]
     * @param links List<Link>
     */
	@Override
	protected void allocateSpectrum(Circuit circuit, int[] chosen, List<Link> links) {
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            chosen = circuit.getSpectrumAssignedByLink(link);
            
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
	@Override
	protected void releaseSpectrum(Circuit circuit, int chosen[], List<Link> links) {
    	for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
        	chosen = circuit.getSpectrumAssignedByLink(link);
        	
            link.liberateSpectrum(chosen);
            link.removeCircuit(circuit);
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
                return integrated.rsa(circuit, this.getMesh());

            case GRMLSA.RSA_SEQUENCIAL:
                if (routing.findRoute(circuit, this.getMesh())) {
                	
                	if(regeneratorAssignment.assignRegenerator((TranslucentCircuit)circuit, spectrumAssignment)){
                		
                		if(((TranslucentCircuit)circuit).getRegeneratorsNodesIndexList().size() == 0){
                			return withoutRegenerator((TranslucentCircuit)circuit, circuit.getRoute(), spectrumAssignment, modulationSelector);
                			
                		}else{
                			return withRegenerator((TranslucentCircuit)circuit, circuit.getRoute(), spectrumAssignment, modulationSelector, ((TranslucentCircuit)circuit).getRegeneratorsNodesIndexList());
                		}
                	}
                } else {
                    return false;
                }
        }

        return false;
    }
	
    /**
	 * This method attempts to define the modulation format and the spectrum to be allocated in each link of the route selected for the request
	 * 
	 * @param circuit - Circuit
	 * @param route - Route
	 * @param spectrumAssignment - SpectrumAssignmentAlgorithmInterface
	 * @param modulationSelector - ModulationSelector
	 * @return boolean - True, if you could define the modulation format and put the spectrum, or false, otherwise
	 */
	public boolean withoutRegenerator(TranslucentCircuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ModulationSelector modulationSelector){
		Modulation mod = modulationSelector.selectModulation(circuit, route, spectrumAssignment, mesh);
		
		if(mod != null){
			circuit.setModulation(mod);
			
			HashMap<Link, Modulation> modulationByLink = new HashMap<Link, Modulation>();
			Vector<Link> linkList = route.getLinkList();
			for(int l = 0; l < linkList.size(); l++){
				modulationByLink.put(linkList.get(l), mod);
			}
			circuit.setModulationByLink(modulationByLink);
			
			if(spectrumAssignment.assignSpectrum(mod.requiredSlots(circuit.getRequiredBandwidth()), circuit)){
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
	 * selected for the request taking into account the transparent segments among the regenerators selected for the request
	 * 
	 * @param request - Request
	 * @param route - Route
	 * @param spectrumAssignment - SpectrumAssignmentAlgorithmInterface
	 * @param modulationSelector - ModulationSelector
	 * @param regeneratorsNodesIndexList - ArrayList<Integer>
	 * @return true, se conguiu definir o fomato de modula��o e colcar o espectro, ou false, caso contrario
	 */
	public boolean withRegenerator(TranslucentCircuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ModulationSelector modulationSelector, List<Integer> regeneratorsNodesIndexList){
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
			
			int chosen[] = tryAssignSpectrum(circuit, route, sourceNodeIndex, destinationNodeIndex, composition, spectrumAssignment, modulationSelector, true, false);
			
			if(chosen == null){
				// Steps to avoid error in the metrics
				destinationNodeIndex = route.getNodeList().size() - 1;
				Modulation mod = circuit.getModulation();
				
				for(int l = sourceNodeIndex; l < destinationNodeIndex; l++){
					sourceNode = route.getNode(l);
					destinationNode = route.getNode(l + 1);
					link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
					
					modulationByLink.put(link, mod);
				}
				
				circuit.setModulationByLink(modulationByLink);;
				return false;
			}
			
			Modulation mod = circuit.getModulation();
			for(int l = sourceNodeIndex; l < destinationNodeIndex; l++){
				sourceNode = route.getNode(l);
				destinationNode = route.getNode(l + 1);
				link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
				
				spectrumAssignedByLink.put(link, chosen);
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
	 * @param circuit - Circuit
	 * @param route - Route
	 * @param sourceNodeIndex - int
	 * @param destinationNodeIndex - int
	 * @param composition - List<int[]>
	 * @param spectrumAssignment - SpectrumAssignmentAlgorithmInterface
	 * @param modulationSelector - ModulationSelector
	 * @param checkAssignSpectrum - boolean (True, to save the first range of allocated spectrum, or false, otherwise)
	 * @param checkSigma - boolean
	 * @return int[]
	 */
	public int[] tryAssignSpectrum(TranslucentCircuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, List<int[]> composition, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ModulationSelector modulationSelector, boolean checkAssignSpectrum, boolean checkSigma){
		int resChosen[] = null;
		int alternativeChosen[] = null; //Used to avoid error in metrics
		Modulation resMod = null;
		Modulation alternativeMod = null;
		List<Modulation> avaliableModulations = modulationSelector.getAvaliableModulations();
		
		for(int i = 0; i < avaliableModulations.size(); i++){
			Modulation mod = avaliableModulations.get(i);
			int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			
			int chosen[] = spectrumAssignment.policy(numberOfSlots, composition, circuit);
			if(chosen != null){
				if(alternativeMod == null){
					alternativeMod = mod;
					if(checkAssignSpectrum){
						alternativeChosen = chosen;
					}
				}
				
				boolean flag = mesh.getPhysicalLayer().isAdmissibleModultionBySegment(circuit, route, sourceNodeIndex, destinationNodeIndex, mod, chosen);
				if(flag){
					resChosen = chosen;
					resMod = mod;
				}
			}
		}
		
		if(resMod == null){ //QoT is not acceptable for all modulations
			resMod = avaliableModulations.get(0); //To avoid metric error
			
			if(alternativeMod != null){ //Allocated spectro using some modulation, but the one that was inadmissible
				resMod = alternativeMod;
				if(checkAssignSpectrum){
					resChosen = alternativeChosen;
				}
				circuit.setQoT(false); //To mark that the blockade was by Qto inadmissible
			}
		}
		
		circuit.setModulation(resMod);
		
		return resChosen;
	}
	
}
