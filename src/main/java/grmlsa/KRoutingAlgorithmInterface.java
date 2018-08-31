package grmlsa;

import java.util.HashMap;
import java.util.List;

import network.Node;

/**
 * This interface represents a k routing algorithm.
 * 
 * @author Alexandre
 */
public interface KRoutingAlgorithmInterface {
	
	/**
     * Returns the k shortest paths between two nodes
     * 
     * @param n1 Node
     * @param n2 Node
     * @return List<Route>
     */
    public List<Route> getRoutes(Node n1, Node n2);
    
	/**
	 * Returns the route list for all pairs
	 * 
	 * @return Vector<Route>
	 */
	public HashMap<String, List<Route>> getRoutesForAllPairs();
}