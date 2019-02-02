package grmlsa.spectrumAssignment;

import java.util.List;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;


/**
 * This class represents the spectrum allocation technique called Exact Fit.
 * This technique attempts to allocate a range of spectrum with exactly the same size as the number of slots required by the new request.
 * If the spectrum band is not found, the request is allocated following the first fit policy.
 *
 *Implementation based on:
 * - Routing and Spectrum Allocation in Elastic Optical Networks: A Tutorial (2015)
 *
 * @author Alexandre
 */

public class ExactFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
    	List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());

        int chosen[] = policy(numberOfSlots, composition, circuit, cp);
        circuit.setSpectrumAssigned(chosen);
        
        if (chosen == null)
        	return false;
        
        return true;
    }
    
    @Override
    public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
    	int chosen[] = null;
        
        for (int[] band : freeSpectrumBands) {
        	int sizeBand = band[1] - band[0] + 1;
            if (sizeBand == numberOfSlots) {
                chosen = band.clone();
                chosen[1] = chosen[0] + numberOfSlots - 1; //It is not necessary to allocate the entire band, just the amount of slots required
                break;
            }
        }
        
        if (chosen == null) { // did not find any contiguous tracks and is still available
            // now just look for the free range and apply first fit policy
        	
	        for (int[] band : freeSpectrumBands) {
	            if (band[1] - band[0] + 1 >= numberOfSlots) {
	                chosen = band.clone();
	                chosen[1] = chosen[0] + numberOfSlots - 1; //It is not necessary to allocate the entire band, just the amount of slots required
	                break;
	            }
	        }
        }
        
        return chosen;
    }

}