package grmlsa.trafficGrooming;

import java.util.List;

import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

/**
 * This class represents a dummy technique of traffic grooming.
 * The objective of this algorithm is just demonstrate the traffic grooming capabilities of SNetS.
 * This algorithm only aggregates requests with the same origin and destination.
 *
 * @author Iallen
 *
 */
public class SimpleTrafficGrooming implements TrafficGroomingAlgorithmInterface {

	@Override
	public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane cp) throws Exception {

		// Search for active circuits with the same origin and destination of the new request.
		List<Circuit> activeCircuits = cp.searchForActiveCircuits(rfc.getPair().getSource().getName(), rfc.getPair().getDestination().getName());

		for (Circuit circuit : activeCircuits) {
			
			// Investigate if the active circuit is able to accommodate the new request
			
			// How many more slots are needed?
			int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() + rfc.getRequiredBandwidth());
			int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int numMoreSlots = numFinalSlots - numCurrentSlots;
			
			// You can add without increasing the number of slots
			if(numMoreSlots == 0){
				circuit.addRequest(rfc);
				rfc.getCircuits().add(circuit);
				return true;
			}
			
			// Is it possible to expand the channel?
			List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());
			
			int[] bandFreeAdjInferior = IntersectionFreeSpectrum.bandAdjacentInferior(circuit.getSpectrumAssigned(), composition);
			int numFreeSlotsDown = 0;
			
			if(bandFreeAdjInferior != null){
				numFreeSlotsDown = bandFreeAdjInferior[1] - bandFreeAdjInferior[0] + 1;
			}
			
			int[] bandFreeAdjSuperior = IntersectionFreeSpectrum.bandAdjacentSuperior(circuit.getSpectrumAssigned(), composition);
			int numFreeSlotsUp = 0;
			
			if(bandFreeAdjSuperior != null){
				numFreeSlotsUp = bandFreeAdjSuperior[1] - bandFreeAdjSuperior[0] + 1;
			}
			
			int totalNumAdjFreeSlots = numFreeSlotsUp + numFreeSlotsDown;
			
			if(numMoreSlots < totalNumAdjFreeSlots){ // Yes, it is possible expands the channel to accommodate the new request
				
				// Expand this channel to accommodate the new request
				int expansion[] = decideToExpand(numMoreSlots, bandFreeAdjInferior, bandFreeAdjSuperior);

				if(cp.expandCircuit(circuit, expansion[0], expansion[1])){// Expansion succeeded
					circuit.addRequest(rfc);
					rfc.getCircuits().add(circuit);
					return true;
				}
			}
		}
		
		// Failed to aggregation the new request.
		// Try to create a new circuit to accommodate that.
		Circuit circuit = cp.createNewCircuit(rfc);
		
		return cp.establishCircuit(circuit);
	}
	
	/**
	 * This method decides how to expand the channel to accommodate new connections.
	 * 
	 * @param numMoreSlots int - Number of slots that are still needed to establish the circuit
	 * @param upperFreeSlots int
	 * @param lowerFreeSlots int
	 * @return a vector with size 2, the index 0 represents the number of slots to use below (lower),
	 *                               the index 1 represents the number of slots to use above (upper).
	 */
	protected int[] decideToExpand(int numMoreSlots, int lowerFreeSlots[], int upperFreeSlots[]){
		int res[] = new int[2];

		int numLowerFreeSlots = 0;
		if(lowerFreeSlots != null){
			numLowerFreeSlots = lowerFreeSlots[1] - lowerFreeSlots[0] + 1;
		}

		int numUpperFreeSlots = 0;
		if(upperFreeSlots != null){
			numUpperFreeSlots = upperFreeSlots[1] - upperFreeSlots[0] + 1;
		}

		if(numLowerFreeSlots >= numMoreSlots){ // First, try to put everything down
			res[0] = numMoreSlots;
			res[1] = 0;
		}else{ // Elsewere, use fully down free spectrum band and the remaining on top
			res[0] = numLowerFreeSlots;
			res[1] = numMoreSlots - numLowerFreeSlots;
		}
		
		return res;
	}

	@Override
	public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {
		Circuit circuit = rfc.getCircuits().get(0);
		
		if(circuit.getRequests().size() == 1){ // The connection being terminated is the last to use this channel.
			cp.releaseCircuit(circuit);
			
		}else{ // Reduce the number of slots allocated for this channel if it is possible.

			int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() - rfc.getRequiredBandwidth());
			int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int release = numCurrentSlots - numFinalSlots;
			int releaseBand[] = new int[2];

			if(release!=0) {
				releaseBand[1] = circuit.getSpectrumAssigned()[1];
				releaseBand[0] = releaseBand[1] - release + 1;
				cp.retractCircuit(circuit, 0, release);
			}

			circuit.removeRequest(rfc);
		}
	}

}
