package grmlsa.spectrumAssignment;

import java.util.List;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 * This is a class that assigns spectrum following the policy of worst fit
 * 
 * @author Iallen
 */
public class WorstFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
    	List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());

		// now just look for the free range with size farthest from the amount of slots required
		int chosen[] = policy(numberOfSlots, composition, circuit, cp);
		circuit.setSpectrumAssigned(chosen);
		
		if(chosen == null)
			return false; //Found no contiguous track and remains available
		
		return true;
    }
    
    @Override
    public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
		if(numberOfSlots> maxAmplitude) return null;
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