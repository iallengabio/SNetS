package grmlsa.routing;

import java.util.HashMap;

import grmlsa.Route;
import network.Circuit;
import network.Mesh;

import java.io.Serializable;


/**
 * This interface represents a routing algorithm.
 * 
 * @author Iallen
 */
public interface RoutingAlgorithmInterface extends Serializable {
	
	/**
	 * This method computes a route for a new circuit.
	 * The info about the pair of origin and destination are available in @circuit.
	 * 
	 * @param circuit Circuit
	 * @param mesh Mesh
	 * @return boolean
	 */
	public boolean findRoute(Circuit circuit, Mesh mesh);
	
	/**
	 * Returns the route list for all pairs
	 * 
	 * @return Vector<Route>
	 */
	public HashMap<String, Route> getRoutesForAllPairs();

}
