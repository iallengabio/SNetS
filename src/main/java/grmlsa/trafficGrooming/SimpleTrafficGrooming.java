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
	public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane cp) {

		//search for active circuits with the same origin and destination of the new request.
		List<Circuit> activeCircuits = cp.searchForActiveCircuits(rfc.getPair().getSource().getName(), rfc.getPair().getDestination().getName());
		
		for (Circuit circuit : activeCircuits) {
			
			//investigate if the active circuit is able to accommodate the new request
			
			//how many more slots are needed?
			int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() + rfc.getRequiredBandwidth());
			int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int numMoreSlots = numFinalSlots - numCurrentSlots;
			
			//Is it possible to expand the channel?
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
				int upExpBand[] = null;
				int downExpBand[] = null;
				
				if(expansion[0] > 0){ // Expand down
					downExpBand = new int[2];
					downExpBand[1] = bandFreeAdjInferior[1];
					downExpBand[0] = downExpBand[1] - expansion[0] + 1;					
				}
				if(expansion[1] > 0){ // Expansion up
					upExpBand = new int[2];
					upExpBand[0] = bandFreeAdjSuperior[0];
					upExpBand[1] = upExpBand[0] + expansion[1] - 1;
				}
				
				if(cp.expandCircuit(circuit, upExpBand, downExpBand)){// Expansion succeeded
					circuit.addRequest(rfc);
					rfc.setCircuit(circuit);
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
	private int[] decideToExpand(int numMoreSlots, int lowerFreeSlots[], int upperFreeSlots[]){
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
			
		} else if(numUpperFreeSlots >= numMoreSlots){ // Second, try to put everything up
			res[0] = 0;
			res[1] = numMoreSlots;
		}
		
		return res;
	}

	@Override
	public void finishConnection(RequestForConnection rfc, ControlPlane cp) {
		
		Circuit circuit = rfc.getCircuit();
		
		if(circuit.getRequests().size() == 1){ // The connection being terminated is the last to use this channel.
			cp.releaseCircuit(circuit);
			
		}else{ // Reduce the number of slots allocated for this channel if it is possible.

			int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() - rfc.getRequiredBandwidth());
			int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int release = numCurrentSlots - numFinalSlots;
			int releaseBand[] = new int[2];
			
			releaseBand[1] = circuit.getSpectrumAssigned()[1];
			releaseBand[0] = releaseBand[1] - release + 1;

			cp.retractCircuit(circuit, null, releaseBand);
			circuit.removeRequest(rfc);
		}
	}

}
