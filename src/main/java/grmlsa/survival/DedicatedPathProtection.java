package grmlsa.survival;

import java.util.List;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.routing.FixedDoubleRouteBacktracking;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.trafficGrooming.SimpleTrafficGrooming2;
import network.Circuit;
import network.SurvivalCircuit;
import network.SurvivalControlPlane;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

/**
 * this class represents the dedicate path protection
 * 
 * @author Alexandre
 */
public class DedicatedPathProtection implements SurvivalStrategyInterface {

    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    
	@Override
	public boolean applyStrategy(RequestForConnection rfc, SurvivalControlPlane cp) throws Exception {
		
		FixedDoubleRouteBacktracking routingAlgorithm = new FixedDoubleRouteBacktracking();
		SimpleTrafficGrooming2 groomingAlgorithm = new SimpleTrafficGrooming2();
		
		List<Route> listRoutes = routingAlgorithm.findRoute(rfc, cp.getMesh());
		Route workRoute = listRoutes.get(0);
		Route backupRoute = listRoutes.get(1);
		
		boolean flagWorkRoute = false;
		boolean flagBackupRoute = false;
		
		// Work route search
		if(workRoute != null){
			// Applies the traffic aggregation algorithm
			if(groomingAlgorithm.searchCircuitsForGroomingByRoute(rfc, cp, workRoute, backupRoute)){
				flagWorkRoute = true;
				
			} else {
				// Try to create a new circuit to accommodate that.
		 		SurvivalCircuit workCircuit = (SurvivalCircuit)cp.createNewCircuit(rfc);
		 		workCircuit.setRoute(workRoute);
		 		
		 		if(tryEstablishNewCircuit(workCircuit, workRoute, cp)){
		 			flagWorkRoute = true;
		 		}
			}
		}
 		
		// Backup route search
		if(backupRoute != null){
			// Applies the traffic aggregation algorithm
			if(groomingAlgorithm.searchCircuitsForGroomingByRoute(rfc, cp, workRoute, backupRoute)){
				flagBackupRoute = true;
				
			} else {
				// Try to create a new circuit to accommodate that.
				SurvivalCircuit backupCircuit = (SurvivalCircuit)cp.createNewCircuit(rfc);
		 		backupCircuit.setRoute(backupRoute);
		 		
		 		if(tryEstablishNewCircuit(backupCircuit, workRoute, cp)){
		 			flagBackupRoute = true;
		 		}
			}
		}
        
		return (flagWorkRoute && flagBackupRoute);
	}
	
	public boolean tryEstablishNewCircuit(Circuit circuit, Route route, SurvivalControlPlane cp){
		
		if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection(); // Uses the modulation selection algorithm defined in the simulation file
        }
        if(spectrumAssignment == null){
			spectrumAssignment = cp.getSpectrumAssignment(); // Uses the spectrum assignment algorithm defined in the simulation file
		}
        
        // Modulation and spectrum range selected
        Modulation chosenMod = null;
        int chosenBand[] = null;
        
        // To avoid metrics error
 		Modulation checkMod = null;
 		int checkBand[] = null;
 		
 		List<Modulation> avaliableModulations = modulationSelection.getAvaliableModulations();
 		
		for(int m = 0; m < avaliableModulations.size(); m++){
			Modulation mod = avaliableModulations.get(m);
			circuit.setModulation(mod);
			
			int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			List<int[]> merge = IntersectionFreeSpectrum.merge(route);
			
			int band[] = spectrumAssignment.policy(numberOfSlots, merge, circuit, cp);
			circuit.setSpectrumAssigned(band);
			
			if(band != null){
				if(checkMod == null){
					checkMod = mod;
					checkBand = band;
				}
				
				boolean circuitQoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band);
				
				if(circuitQoT){
					chosenMod = mod;
					chosenBand = band;
				}
			}
		}
 		
		circuit.setModulation(chosenMod);
		circuit.setSpectrumAssigned(chosenBand);
		
 		if(chosenMod == null){
 			circuit.setModulation(checkMod);
 			circuit.setSpectrumAssigned(checkBand);
 			
 			return false;
 		}
 		
 		return true;
	}
	
	@Override
	public boolean thereAreFreeTransponders(Circuit circuit){
		boolean flag = true;
		
		if(circuit.getSource().getTxs().hasFreeTransmitters() && circuit.getDestination().getRxs().hasFreeRecivers()){
			
		}
    	return flag;
    }
	
	@Override
	public boolean survive(Circuit circuit) throws Exception {
		
		
		
		
		
		return true;
	}

	
}
