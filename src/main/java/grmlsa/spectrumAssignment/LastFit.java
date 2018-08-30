package grmlsa.spectrumAssignment;

import java.util.List;

import network.Circuit;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the spectrum allocation technique called First Fit.
 * This technique chooses the last free spectrum band that accommodates the request.
 *
 * @author Iallen
 */
public class LastFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit) {
    	List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());

        int chosen[] = policy(numberOfSlots, composition, circuit);

        if (chosen == null) return false;

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
        int band[] = null;
        int i;
        
        for (i = freeSpectrumBands.size() - 1; i >= 0; i--) {
            band = freeSpectrumBands.get(i);
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band.clone();
                chosen[0] = chosen[1] - numberOfSlots + 1;//It is not necessary to allocate the entire band, just the amount of slots required

                break;
            }
        }

        return chosen;
    }

}

