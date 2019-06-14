package grmlsa.regeneratorAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import grmlsa.Route;
import network.Link;
import network.Node;
import network.TranslucentCircuit;
import network.TranslucentControlPlane;
import util.IntersectionFreeSpectrum;


/**
 * Implementation of the algorithm First Longest Reach Regenerator Assignment (FLR-RA).
 * Based on article:
 *  - Heuristic Algorithms for Regenerator Assignment in Dynamic Translucent Elastic Optical Networks (ICTON 2015)
 * 
 * @author Alexandre
 */
public class FLRRegeneratorAssignment implements RegeneratorAssignmentAlgorithmInterface {

	@Override
	public boolean assignRegenerator(TranslucentCircuit circuit, TranslucentControlPlane controlPlane) {
		ArrayList<Integer> regeneratorsNodesIndexList = new ArrayList<Integer>();
		circuit.setRegeneratorsNodesIndexList(new ArrayList<Integer>());
		Route route = circuit.getRoute();
		Vector<Node> nodes = route.getNodeList();
		
		int r = 0;
		int N = nodes.size();
		for(int s = 0; s < N; s++){
			for(int x = s + 1; x < N; x++){
				Node nodeX = nodes.get(x);
				
				if(isThereFreeRegenAt(circuit, nodeX) || (x == N-1)){
					if(isThereSpectrumAndQoT(circuit, route, s, x, controlPlane)){
						if(x == N-1){
							//Assign the modulation format with the higher possible spectral efficiency in all stored transparent segments (STS)
							//Perform spectrum assignment in all STS. Establishes all STS. Finish the algorithm.
							
							// Configures the list of circuit regenerators nodes
							circuit.setRegeneratorsNodesIndexList(regeneratorsNodesIndexList);
							
							return controlPlane.strategySelection(circuit);
							
						}else{
							r = x; //update current regeneration point
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
	 * A boolean function that returns true if there is at least one modulation format capable of being assigned
	 *  to the transparent segment between nodes ns and nx in terms of both OSNR and available spectrum, and false otherwise.
	 *  
	 * @param circuit TranslucentCircuit
	 * @param route Route
	 * @param sourceNodeIndex int
	 * @param destinationNodeIndex int
	 * @param cp TranslucentControlPlane
	 * @return boolean
	 */
	public boolean isThereSpectrumAndQoT(TranslucentCircuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, TranslucentControlPlane cp){
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
		
		return cp.tryAssignModulationAndSpectrum(circuit, route, sourceNodeIndex, destinationNodeIndex, composition);
	}

}
