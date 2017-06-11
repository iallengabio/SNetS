package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a class that assigns spectrum following the policy of worst fit
 * 
 * @author Iallen
 */
public class WorstFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit) {
    	Route route = circuit.getRoute();		
		List<Link> links = new ArrayList<>(route.getLinkList());		
		List<int[]> composition;
		composition = links.get(0).getFreeSpectrumBands();
		int i;
		for(i = 1; i < links.size(); i++){
			composition = IntersectionFreeSpectrum.merge(composition, links.get(i).getFreeSpectrumBands());
		}
		
		// now just look for the free range with size farthest from the amount of slots required
		int chosen[] = policy(numberOfSlots, composition, circuit);
		
		if(chosen == null) return false; //Found no contiguous track and remains available
		
		circuit.setSpectrumAssigned(chosen);
		
		return true;
    }
    
    /**
     * Applies the policy of allocation of spectrum LastFit
     * 
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @param circuit Circuit
     * @return int[]
     */
    @Override
    public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit){
    	int chosen[] = null;
		int greaterDifference = -1;
		for (int[] band : freeSpectrumBands) {
			int sizeBand = band[1] - band[0] + 1;
			if(sizeBand >= numberOfSlots){
				if(sizeBand - numberOfSlots > greaterDifference){
					chosen = band.clone();
					chosen[1] = chosen[0] + numberOfSlots - 1;
					greaterDifference = sizeBand - numberOfSlots;
				}
			}
		}
		return chosen;
    }

}

