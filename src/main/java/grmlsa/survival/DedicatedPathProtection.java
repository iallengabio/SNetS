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
		int indexBackupRoute = 0;
		
		if(workRoute != null && backupRoute != null){
			
			// Applies the traffic aggregation algorithm
			if(groomingAlgorithm.searchCircuitsForGroomingByRoute(rfc, cp, workRoute, backupRoute)){
				return true;
				
			} else {
				// Try to create a new circuit to accommodate the request
		 		SurvivalCircuit newCircuit = (SurvivalCircuit)cp.createNewCircuit(rfc);
		 		newCircuit.setRoute(workRoute);
		 		((SurvivalCircuit)newCircuit).getBackupRoutes().add(backupRoute);
		 		((SurvivalCircuit)newCircuit).setIndexBackupRoute(indexBackupRoute);
		 		
				Modulation workMod;
				Modulation backupMod;
				int workBand[];
				int backupBand[];
				
				// Check if it is possible to establish the circuit in the work route
		 		if(tryEstablishNewCircuit(newCircuit, workRoute, cp)){
		 			
		 			workMod = newCircuit.getModulation();
		 			workBand = newCircuit.getSpectrumAssigned();
		 			
		 			// Check if it is possible to establish the circuit in the backup route
			 		if(tryEstablishNewCircuit(newCircuit, backupRoute, cp)){
			 			
			 			backupMod = newCircuit.getModulation();
			 			backupBand = newCircuit.getSpectrumAssigned();
			 			
			 			newCircuit.setRoute(workRoute);
			 			newCircuit.setModulation(workMod);
			 			newCircuit.setSpectrumAssigned(workBand);
			 			
			 			((SurvivalCircuit)newCircuit).getBackupRoutes().set(indexBackupRoute, backupRoute);
			 			((SurvivalCircuit)newCircuit).getModulationByBackupRoute().put(backupRoute, backupMod);
			 			((SurvivalCircuit)newCircuit).getSpectrumAssignedByBackupRoute().put(backupRoute, backupBand);
			 			
			 			return true;
			 		}
		 		}
			}
		}
        
		return false;
	}
	
	public boolean tryEstablishNewCircuit(Circuit circuit, Route route, SurvivalControlPlane cp){
		
		if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection(); // Uses the modulation selection algorithm defined in the simulation file
        }
        if(spectrumAssignment == null){
			spectrumAssignment = cp.getSpectrumAssignment(); // Uses the spectrum assignment algorithm defined in the simulation file
		}
        
        circuit.setRoute(route);
        
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
