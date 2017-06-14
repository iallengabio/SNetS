package grmlsa.regeneratorAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import network.Link;
import network.Node;
import network.TranslucentCircuit;
import network.TranslucentControlPlane;
import util.IntersectionFreeSpectrum;


/**
 * Implementation of the algorithm First Narrowest Spectrum Regenerator Assignment (FNS-RA).
 * Based on article:
 *  - Heuristic Algorithms for Regenerator Assignment in Dynamic Translucent Elastic Optical Networks (ICTON 2015)
 * 
 * @author Alexandre
 */
public class FNSRegeneratorAssignment implements RegeneratorAssignmentAlgorithmInterface {

	@Override
	public boolean assignRegenerator(TranslucentCircuit circuit, TranslucentControlPlane controlPlane) {
		ArrayList<Integer> regeneratorsNodesIndexList = new ArrayList<Integer>();
		circuit.setRegeneratorsNodesIndexList(new ArrayList<Integer>());
		Route route = circuit.getRoute();
		Vector<Node> nodes = route.getNodeList();
		
		//hight index means a highest spectral efficiency of the modulation format
		List<Modulation> avaliableModulations = controlPlane.getModulationSelector().getAvaliableModulations();
		
		int r = 0;
		int m = avaliableModulations.size()-1; //modulation format with the higher spectral efficiency
		int N = nodes.size();
		for(int s = 0; s < N; s++){
			for(int x = s + 1; x < N; x++){
				Node nodeX = nodes.get(x);
				
				if(isThereFreeRegenAt(circuit, nodeX) || (x == N-1)){
					Modulation mod = avaliableModulations.get(m);
					
					if(isThereSpectrumAndQoT(circuit, route, s, x, controlPlane, mod)){
						if(x == N-1){
							//Assign the modulation format with the higher possible spectral efficiency in all stored transparent segments (STS)
							//Perform spectrum assignment in all STS. Establishes all STS. Finish the algorithm.
							
							// Configures the list of circuit regenerators nodes
							circuit.setRegeneratorsNodesIndexList(regeneratorsNodesIndexList);
							
							return controlPlane.strategySelection(circuit);
							
						}else{
							if(m != avaliableModulations.size()-1){
								// this step is a correction in the original algorithm
								if(r == s){
									r++;
								}
								//store the transparent segment from s to r
								Node nodeR = nodes.get(r);
								
								if(!regeneratorsNodesIndexList.contains(r)){ // Verifies that the node is no allocated to this connection
									
									if(nodeR.getRegenerators().allocatesRegenerators(circuit)){ // Allocates regenerators
										regeneratorsNodesIndexList.add(r);
									}
								}
								
								r = x;
								s = x;
								m = avaliableModulations.size()-1;
								
							}else{
								r = x;
							}
						}
					}else{
						if(r != s){
							//store the transparent segment from s to r
							Node nodeR = nodes.get(r);
							
							if(!regeneratorsNodesIndexList.contains(r)){ // Verifies that the node is no allocated to this connection
								
								if(nodeR.getRegenerators().allocatesRegenerators(circuit)){ // Allocates regenerators
									regeneratorsNodesIndexList.add(r);
								}
							}
							
							s = r; //update current source point
							x = r; //update current testing point
							
						}else{
							x = x - 1;
							m = m - 1;
							
							if(m < 0){
								//block the current request
								
								// Configures the list of circuit regenerators nodes
								circuit.setRegeneratorsNodesIndexList(regeneratorsNodesIndexList);
								
								controlPlane.strategySelection(circuit);
								
								return false;
							}
						}
					}
				}
			}
		}
		
		return controlPlane.strategySelection(circuit);
	}

	/**
	 * A Boolean function that returns true if there are unused regenerators at the node nx to regenerate
	 *  streams of br bit rate, and false otherwise.
	 *  
	 * @param circuit TranslucentCircuit
	 * @param nodeX Node
	 * @return boolean
	 */
	public boolean isThereFreeRegenAt(TranslucentCircuit circuit, Node nodeX){
		int numberRegeneratorsRequired = nodeX.getRegenerators().getAmountOfRequiredRegenerators(circuit);
		
		if(nodeX.getRegenerators().canRegenerate(numberRegeneratorsRequired)){ // Check if you can regenerate
			return true;
		}
		return false;
	}
	
	/**
	 * A boolean function that returns true if there is the modulation format capable of being assigned
	 *  to the transparent segment between nodes ns and nx in terms of both OSNR and available spectrum, and false otherwise.
	 *  
	 * @param circuit - TranslucentCircuit
	 * @param route - Route
	 * @param sourceNodeIndex - int
	 * @param destinationNodeIndex - int
	 * @param cp - TranslucentControlPlane
	 * @param modulation -Modulation
	 * @return boolean
	 */
	public boolean isThereSpectrumAndQoT(TranslucentCircuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, TranslucentControlPlane cp, Modulation modulation){
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
		
		return tryAssignSpectrum(circuit, route, sourceNodeIndex, destinationNodeIndex, composition, cp, modulation);
	}
	
	/**
	 * This method attempts to choose a range of spectrum for the request according to a modulation format
	 * Returns true if false and false otherwise
	 * 
	 * @param circuit - TranslucentCircuit
	 * @param route - Route
	 * @param sourceNodeIndex - int
	 * @param destinatinNodeIndex - int
	 * @param composition - List<int[]>
	 * @param cp - TranslucentControlPlane
	 * @param modulation - Modulation
	 * @return boolean
	 */
	public boolean tryAssignSpectrum(TranslucentCircuit circuit, Route route, int sourceNodeIndex, int destinatinNodeIndex, List<int[]> composition, TranslucentControlPlane cp, Modulation modulation){
		boolean flagQoT = true; // Assuming that the circuit QoT starts as acceptable
		boolean flagSuccess = false;
		
		int numberOfSlots = modulation.requiredSlots(circuit.getRequiredBandwidth());
		int chosen[] = cp.getSpectrumAssignment().policy(numberOfSlots, composition, circuit);
		if(chosen != null){
			
			flagQoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultionBySegment(circuit, route, sourceNodeIndex, destinatinNodeIndex, modulation, chosen);
			if(flagQoT){
				flagSuccess = true;
			}
		}
		
		// Configures the circuit information
		circuit.setModulation(modulation);
		circuit.setSpectrumAssigned(chosen);
		circuit.setQoT(flagQoT);
		
		return flagSuccess;
	}

}
