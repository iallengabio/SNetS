package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the implementation of the Dedicated Partition algorithm presented in the article:
 *  - Spectrum management in heterogeneous bandwidth optical networks (2014)
 *  
 * The Dedicated Partition divides the frequency slots into regions that are dedicated for each type of request. 
 * The types of requests are defined by the number of frequency slots required for each request.
 * 
 * @author Iallen
 */
public class DedicatedPartition implements IntegratedRMLSAAlgorithmInterface{

	private NewKShortestPaths kShortestsPaths;
	private ModulationSelectionAlgorithmInterface modulationSelection;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
	
	private HashMap<Integer, int[]> zones; 
	
	public DedicatedPartition(){
		
		List<int[]> zones = null;
		try {
			//zones = ZonesFileReader.readTrafic(Util.projectPath + "/zones");
		} catch (Exception e) {
			System.err.println("It was not possible to read the file with the zones specification!");
			
			e.printStackTrace();
		}
		this.zones = new HashMap<>();
		int aux[];
		for (int[] zone : zones) {
			aux = new int[2];
			aux[0] = zone[1];
			aux[1] = zone[2];
			this.zones.put(zone[0], aux);			
		}
	}
	
	@Override
	public boolean rsa(Circuit circuit, ControlPlane cp) {
		if(kShortestsPaths == null){
			kShortestsPaths = new NewKShortestPaths(cp.getMesh(), 3); //This algorithm uses 3 alternative paths
		}
		if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection();
        	modulationSelection.setAvaliableModulations(ModulationSelector.configureModulations(cp.getMesh()));
        }
		if(spectrumAssignment == null){
			spectrumAssignment = new FirstFit();
		}
		
		List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
		Route chosenRoute = null;
		Modulation chosenMod = null;
		int chosenBand[] = {999999,999999}; // Value never reached
		
		for (Route route : candidateRoutes) {
			
			circuit.setRoute(route);
			Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
			
			// Calculate how many slots are needed for this route
			int numSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			int zone[] = this.zones.get(numSlots);
			List<int[]> primaryZone = new ArrayList<>();
			primaryZone.add(zone);			
			
			List<int[]> merge = IntersectionFreeSpectrum.merge(route);
			merge = IntersectionFreeSpectrum.merge(merge, primaryZone);
			
			int ff[] = spectrumAssignment.policy(numSlots, merge, circuit, cp);
			
			if(ff != null && ff[0] < chosenBand[0]){
				chosenBand = ff;
				chosenRoute = route;
				chosenMod = mod;
			}
		}
		
		if(chosenRoute != null){ // If there is no route chosen is why no available resource was found on any of the candidate routes
			circuit.setRoute(chosenRoute);
			circuit.setModulation(chosenMod);
			circuit.setSpectrumAssigned(chosenBand);
			
			return true;
			
		}else{
			circuit.setRoute(candidateRoutes.get(0));
			circuit.setModulation(modulationSelection.getAvaliableModulations().get(0));
			circuit.setSpectrumAssigned(null);
			
			return false;
		}
		
	}

}
