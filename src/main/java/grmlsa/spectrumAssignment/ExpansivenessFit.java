package grmlsa.spectrumAssignment;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

import java.util.List;

/**
 * This is a class that assigns spectrum following the policy of expansiveness fit.
 * This policy chooses the spectrum band that maximizes the expansion capacity of the established circuits.
 * The new circuits will be allocated in the middle of the bigger free spectrum range.
 * 
 * @author Iallen
 */
public class ExpansivenessFit implements SpectrumAssignmentAlgorithmInterface {

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
    
    /**
     * Applies the policy of allocation of spectrum expansiveness fit
     * 
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @param circuit Circuit
     * @return int[]
     */
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
					greaterDifference = sizeBand - numberOfSlots;
					chosen[0] = chosen[0] + ((sizeBand - numberOfSlots)/2);
					chosen[1] = chosen[0] + numberOfSlots - 1;
				}
			}
		}
		return chosen;
    }

    /**
     *
     * @param sb Spectrum band to be evaluated.
     * @param freeSpectrumBands free spectrum bands in all links.
     * @return the expasiveness of the future circuit with 'sb' assigned.
     */
    public int expasiveness(int sb[], List<int[]> freeSpectrumBands){
        int expansiveness;
        for (int aux[]:freeSpectrumBands) {
            if (aux[0] <= sb[0] && aux[1] >= sb[1]) {//aux contains sb
                int fsaux = aux[1]-aux[0]+1;
                int fssb = sb[1]-sb[0]+1;
                expansiveness = fsaux - fssb;
                return expansiveness;
            }
        }
        return -1; //should not reach here.
    }

}