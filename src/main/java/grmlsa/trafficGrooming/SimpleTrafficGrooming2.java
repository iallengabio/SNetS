package grmlsa.trafficGrooming;

import java.util.List;

import grmlsa.Route;
import network.Circuit;
import network.ControlPlane;
import network.SurvivalCircuit;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

public class SimpleTrafficGrooming2 extends SimpleTrafficGrooming {
	
	/**
	 * This method defines which circuits will be used to aggregate traffic.
	 * 
	 * @param rfc RequestForConnection
	 * @param cp ControlPlane
	 * @param route Route
	 * @return boolean
	 * @throws Exception
	 */
	public boolean searchCircuitsForGroomingByRoute(RequestForConnection rfc, ControlPlane cp, Route workRoute, Route backupRoute) throws Exception {

		// Search for active circuits with the same origin and destination of the new request.
		List<Circuit> activeCircuits = cp.searchForActiveCircuits(rfc.getPair().getSource().getName(), rfc.getPair().getDestination().getName());

		for (Circuit circuit : activeCircuits) {
			
			// Check if the route of the circuit is equal to the reported route
			if(!workRoute.equals(circuit.getRoute())){
				continue; // if it is different it goes to the next circuit
			}
			boolean flag = false;
			List<Route> backupRoutes = ((SurvivalCircuit)circuit).getBackupRoutes();
			for(Route bckRoute : backupRoutes){
				if(backupRoute.equals(bckRoute)){
					flag = true;
				}
			}
			if(!flag){
				continue;
			}
			
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
		return false;
	}
}
