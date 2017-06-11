package grmlsa.regeneratorAssignment;

import java.util.ArrayList;
import java.util.Vector;

import grmlsa.spectrumAssignment.SpectrumAssignmentIterface;
import network.CircuitTranslucent;
import network.Node;

/**
 * This class applies the allocation policy of all available regenerators.
 * 
 * @author Alexandre
 */
public class AllAssignmentOfRegenerator implements RegeneratorAssignmentIterface {

	@Override
	public boolean assignRegenerator(CircuitTranslucent circuit, SpectrumAssignmentIterface spectrumAssignment){
		Vector<Node> nodeList = circuit.getRoute().getNodeList();
		ArrayList<Node> regeneratorsNodesList = new ArrayList<Node>();
		
		for(int i = 1; i < nodeList.size(); i++){ // Starting from the second node
			Node node = nodeList.get(i);
			
			int numberRegeneratorsRequired = node.getRegenerators().getAmountOfRequiredRegenerators(circuit);
			if(node.getRegenerators().canRegenerate(numberRegeneratorsRequired)){ // Check if you can regenerate
				
				if(!regeneratorsNodesList.contains(node)){ // Verifies that the node is no allocated to this connection
					
					if(node.getRegenerators().allocatesRegenerators(circuit)){ // Allocates regenerator
						regeneratorsNodesList.add(node); // Adds the node to the regenerator nodes list for this connection
					}
				}
			}
		}
		
		circuit.setRegeneratorsNodesList(regeneratorsNodesList);
		
		return true;
	}
}
