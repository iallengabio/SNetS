package grmlsa.spectrumAssignment;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

import java.util.List;

/**
 * This class represents the spectrum allocation technique called First Fit.
 * This technique chooses the first free spectrum band that accommodates the request.
 *
 * @author Iallen
 */
public class FirstFit implements SpectrumAssignmentAlgorithmInterface {

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
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band.clone();
                chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                break;
            }
        }
        return chosen;
	}
}