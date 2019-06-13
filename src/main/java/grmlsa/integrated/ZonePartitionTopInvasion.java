package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.LastFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 * This class presents a proposal for modification in the Zone Partition algorithm.
 * 
 * @author Iallen
 */
public class ZonePartitionTopInvasion implements IntegratedRMLSAAlgorithmInterface{

	private int k = 3; //This algorithm uses 3 alternative paths
	private KRoutingAlgorithmInterface kShortestsPaths;
	private ModulationSelectionAlgorithmInterface modulationSelection;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment1;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment2;
	
	private HashMap<Integer, int[]> zones; 
	
	public ZonePartitionTopInvasion(){
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
			kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); //This algorithm uses 3 alternative paths
		}
		if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection();
        }
		if(spectrumAssignment1 == null && spectrumAssignment2 == null){
			spectrumAssignment1 = new FirstFit();
			spectrumAssignment2 = new LastFit();
		}
		
		List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
		Route chosenRoute = null;
		Modulation chosenMod = null;
		int chosenBand[] = {999999,999999}; // Value never reached
		
		// Try to allocate in the primary zone
		for (Route route : candidateRoutes) {
			circuit.setRoute(route);
			
			Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment1, cp);
			circuit.setModulation(mod);
			
			if(mod != null){
				
				// Calculate how many slots are needed for this route
				int numSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
				int zone[] = this.zones.get(numSlots);
				List<int[]> primaryZone = new ArrayList<>();
				primaryZone.add(zone);			
				
				List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
				merge = IntersectionFreeSpectrum.merge(merge, primaryZone);
				
				int ff[] = spectrumAssignment1.policy(numSlots, merge, circuit, cp);
				
				if(ff != null && ff[0] < chosenBand[0]){
					chosenBand = ff;
					chosenRoute = route;	
					chosenMod = mod;
				}
			}
		}
		
		// If it was not possible to allocate any resources, try an invasion in the most available zone
		if(chosenRoute == null){
			
			chosenBand[0] = -1;
			chosenBand[1] = -1;
			double moreFree = 0;
			
			for (Route route : candidateRoutes) {
				circuit.setRoute(route);
				
				Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment2, cp);
				if(mod != null){
					
					// Calculate how many slots are needed for this route
					int numSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
					int zone[] = this.zones.get(numSlots);
					List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
					
					int zoneMoreFree = this.searchMoreFreeZone(numSlots, merge);
					
					if(zoneMoreFree == -1){
						//System.out.println("Impossible invasion!");
						continue;
					};
					
					//System.out.println("invasão viável");
					List<int[]> secondaryZone = new ArrayList<>();
					secondaryZone.add(zones.get(zoneMoreFree));
					
					merge = IntersectionFreeSpectrum.merge(merge, secondaryZone);
					
					double aux = ((double) numberFree(merge)) / ((double)(zones.get(zoneMoreFree)[1] - zones.get(zoneMoreFree)[0] + 1));
					
					int lf[] = spectrumAssignment2.policy(numSlots, merge, circuit, cp);
					
					if(lf != null && aux > moreFree){
						chosenBand = lf;
						chosenRoute = route;
						chosenMod = mod;
						moreFree = aux;
					}
				}
			}	
		}
		
		if(chosenRoute!=null){ // If there is no route chosen is why no available resource was found on any of the candidate routes
			circuit.setRoute(chosenRoute);
			circuit.setModulation(chosenMod);
			circuit.setSpectrumAssigned(chosenBand);
			
			return true;
			
		}else{
			circuit.setRoute(candidateRoutes.get(0));
			circuit.setModulation(cp.getMesh().getAvaliableModulations().get(0));
			circuit.setSpectrumAssigned(null);
			
			return false;
		}
		
	}
	
	/**
	 * This method returns the freer zone where the invasion will be made.
	 * The method will return the zone that will fit more requests of the same type
	 * The returned integer corresponds to the number of slots per request of the selected zone
	 * 
	 * @param numSlotsInvader in
	 * @param merge List<int[]>
	 * @return int
	 */
	private int searchMoreFreeZone(int numSlotsInvader, List<int[]> merge){
		int res = -1;
		double greaterAvailability = 0;
		
		List<int[]> aux1, aux2;
		for (Integer z : zones.keySet()) {
			aux1 = new ArrayList<>();
			aux1.add(zones.get(z));
			
			aux2 = IntersectionFreeSpectrum.merge(merge, aux1);
			int numFree = numberFree(aux2);
			
			double aux = ((double) numFree) / ((double)(zones.get(z)[1] - zones.get(z)[0] + 1));
			
			if(aux > greaterAvailability && numFree >= numSlotsInvader){
				greaterAvailability = aux;
				res = z;
			}
		}
		
		return res;
	}
	
	/**
	 * Returns the number of free slots
	 * 
	 * @param lF List<int[]>
	 * @return int
	 */
	private int numberFree(List<int[]> lF){
		int res = 0;
		
		if(lF.size()>0){
			res = res + lF.get(lF.size()-1)[1] - lF.get(lF.size()-1)[0] + 1;
		}
		
		return res;
	}
	
	/**
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
    public KRoutingAlgorithmInterface getRoutingAlgorithm(){
    	return kShortestsPaths;
    }
}
