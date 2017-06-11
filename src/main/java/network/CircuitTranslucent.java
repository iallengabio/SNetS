package network;

import java.util.List;

public class CircuitTranslucent extends Circuit {
	
	private List<Node> regeneratorsNodesList;

	public CircuitTranslucent() {
		super();
		
		
	}

	/**
	 * Returns the list of regenerators nodes
	 * 
	 * @return the regeneratorsNodesList
	 */
	public List<Node> getRegeneratorsNodesList() {
		return regeneratorsNodesList;
	}

	/**
	 * Sets the list of regenerators nodes
	 * 
	 * @param regeneratorsNodesList the regeneratorsNodesList to set
	 */
	public void setRegeneratorsNodesList(List<Node> regeneratorsNodesList) {
		this.regeneratorsNodesList = regeneratorsNodesList;
	}
	
	
}
