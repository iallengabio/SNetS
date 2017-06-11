package network.controlPlane;

import java.util.List;

import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;
import network.Circuit;
import network.Link;
import network.Mesh;
import network.Node;
import network.PhysicalLayer;
import network.TranslucentCircuit;

/**
 * Class that represents the control plane for a Translucent Elastic Optical Network.
 * 
 * @author Alexandre
 */
public class TranslucentControlPlane extends TransparentControlPlane {

	public TranslucentControlPlane(Mesh mesh, int rmlsaType, TrafficGroomingAlgorithmInterface trafficGroomingAlgorithm,
			                       IntegratedRMLSAAlgorithmInterface integratedRSAAlgoritm, RoutingAlgorithmInterface routingInterface,
			                       SpectrumAssignmentAlgorithmInterface spectrumAssignmentAlgoritm) {
		super(mesh, rmlsaType, trafficGroomingAlgorithm, integratedRSAAlgoritm, routingInterface, spectrumAssignmentAlgoritm);

		
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
	private boolean computeQualityOfTransmission(TranslucentCircuit circuit){
    	
    	boolean minQoT = true;
		int sourceNodeIndex = 0;
		double minSNRdB = Double.MAX_VALUE;
		Route route = circuit.getRoute();
		
		int mumberTransparentSegments = circuit.getRegeneratorsNodesIndexList().size() + 1;
		for(int i = 0; i < mumberTransparentSegments; i++){
			
			int destinationNodeIndex = route.getNodeList().size() - 1;
			if(i < mumberTransparentSegments - 1){
				destinationNodeIndex = circuit.getRegeneratorsNodesIndexList().get(i);
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
    private void allocateSpectrum(Circuit circuit, int[] chosen, List<Link> links) {
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
    private void releaseSpectrum(Circuit circuit, int chosen[], List<Link> links) {
    	for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
        	chosen = circuit.getSpectrumAssignedByLink(link);
        	
            link.liberateSpectrum(chosen);
            link.removeCircuit(circuit);
        }
    }
    
    
	
}
