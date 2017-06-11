package network.controlPlane;

import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;
import network.Mesh;

/**
 * Class that represents the control plane for a Translucent Elastic Optical Network.
 * 
 * @author Alexandre
 */
public class TranslucentControlPlane extends TransparentControlPlane {

	public TranslucentControlPlane(Mesh mesh, int rmlsaType, TrafficGroomingAlgorithmInterface trafficGroomingAlgorithm,
			                       IntegratedRMLSAAlgorithmInterface integratedRSAAlgoritm, RoutingAlgorithmInterface routingInterface,
			                       SpectrumAssignmentAlgorithmInterface spectrumAssignmentAlgoritm) {
		super(mesh, rmlsaType, trafficGroomingAlgorithm, integratedRSAAlgoritm, routingInterface, spectrumAssignmentAlgoritm);

		
	}

	
	
	
}
