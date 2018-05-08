package grmlsa.integrated;

import network.Circuit;
import network.ControlPlane;
import network.Mesh;


/**
 * This interface should be implemented by algorithms that solve the RMLSA problem in an integrated way.
 * 
 * @author Iallen
 */
public interface IntegratedRMLSAAlgorithmInterface {
	
	/**
	 * This method must establish for a given request a route and a band of spectrum and return true.
     * If the RMLSA problem can not be resolved, the method returns false.
	 * 
	 * @param circuit Circuit
	 * @param mesh Mesh
	 * @return boolean Returns whether or not it is possible to allocate resources to establish the connection
	 */
	public boolean rsa(Circuit circuit, Mesh mesh, ControlPlane cp);

}
