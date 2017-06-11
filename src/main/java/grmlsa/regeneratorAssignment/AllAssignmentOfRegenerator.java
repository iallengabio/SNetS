package grmlsa.regeneratorAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.TranslucentCircuit;
import network.Node;

/**
 * This class applies the allocation policy of all available regenerators.
 * 
 * @author Alexandre
 */
public class AllAssignmentOfRegenerator implements RegeneratorAssignmentAlgorithmInterface {

	@Override
	public boolean assignRegenerator(TranslucentCircuit circuit, SpectrumAssignmentAlgorithmInterface spectrumAssignment){
		Vector<Node> nodeList = circuit.getRoute().getNodeList();
		List<Integer> regeneratorsNodesIndexList = new ArrayList<Integer>();
		
		for(int i = 1; i < nodeList.size(); i++){ // Starting from the second node
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
		
		circuit.setRegeneratorsNodesIndexList(regeneratorsNodesIndexList);
		
		return true;
	}
}
