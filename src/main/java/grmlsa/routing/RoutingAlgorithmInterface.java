package grmlsa.routing;

import network.Circuit;
import network.Mesh;


/**
 * This interface represents a routing algorithm.
 * 
 * @author Iallen
 */
public interface RoutingAlgorithmInterface {
	
	/**
	 * This method computes a route for a new circuit.
	 * The info about the pair of origin and destination are available in @circuit.
	 * 
	 * @param circuit Circuit
	 * @param mesh Mesh
	 * @return boolean
	 */
	public boolean findRoute(Circuit circuit, Mesh mesh);

}
