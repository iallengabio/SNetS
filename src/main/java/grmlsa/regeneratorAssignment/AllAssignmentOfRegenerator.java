package grmlsa.regeneratorAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import network.Node;
import network.TranslucentCircuit;
import network.TranslucentControlPlane;

/**
 * This class applies the allocation policy of all available regenerators.
 * 
 * @author Alexandre
 */
public class AllAssignmentOfRegenerator implements RegeneratorAssignmentAlgorithmInterface {

	@Override
	public boolean assignRegenerator(TranslucentCircuit circuit, TranslucentControlPlane controlPlane){
		Vector<Node> nodeList = circuit.getRoute().getNodeList();
		List<Integer> regeneratorsNodesIndexList = new ArrayList<Integer>();
		
		int size = nodeList.size() - 1; // Number of nodes of the route without considering the last
		for(int i = 1; i < size; i++){ // Starting from the second node
			Node node = nodeList.get(i);
			
			int numberRegeneratorsRequired = node.getRegenerators().getAmountOfRequiredRegenerators(circuit);
			if(node.getRegenerators().canRegenerate(numberRegeneratorsRequired)){ // Check if you can regenerate
				
				if(!regeneratorsNodesIndexList.contains(i)){ // Verifies that the node is no allocated to this connection
					
					if(node.getRegenerators().allocatesRegenerators(circuit)){ // Allocates regenerator
						regeneratorsNodesIndexList.add(i); // Adds the node to the regenerator nodes list for this connection
					}
				}
			}
		}
		
		// Configures the list of circuit regenerators nodes
		circuit.setRegeneratorsNodesIndexList(regeneratorsNodesIndexList);
		
		return controlPlane.strategySelection(circuit);
	}
}
