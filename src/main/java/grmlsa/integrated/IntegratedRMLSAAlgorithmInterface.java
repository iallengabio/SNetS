package grmlsa.integrated;

import grmlsa.KRoutingAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;

import java.io.Serializable;


/**
 * This interface should be implemented by algorithms that solve the RMLSA problem in an integrated way.
 * 
 * @author Iallen
 */
public interface IntegratedRMLSAAlgorithmInterface extends Serializable {
	
	/**
	 * This method must establish for a given request a route and a band of spectrum and return true.
     * If the RMLSA problem can not be resolved, the method returns false.
	 * 
	 * @param circuit Circuit
	 * @param cp ControlPlane
	 * @return boolean Returns whether or not it is possible to allocate resources to establish the connection
	 */
	public boolean rsa(Circuit circuit, ControlPlane cp);

	/**
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
	public KRoutingAlgorithmInterface getRoutingAlgorithm();
}
