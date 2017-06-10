package grmlsa.routing;

import network.Circuit;
import network.Mesh;


/**
 * This interface represents a routing algorithm.
 * @author Iallen
 *
 */
public interface RoutingInterface {
	
	/**
	 * This method computes a route for a new circuit.
	 * The info about the pair of origin and destination are available in @request.
	 * @return
	 */
	public boolean findRoute(Circuit request, Mesh mesh);

}
