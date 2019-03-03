package grmlsa.spectrumAssignment;

import java.util.List;

import network.Circuit;
import network.ControlPlane;
import network.Transmitters;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the spectrum allocation technique called Dummy Fit.
 * The objective of this class is to just exemplify the implementation of a spectrum allocation algorithm.
 *
 * @author Iallen
 */
public class DummyFit implements SpectrumAssignmentAlgorithmInterface {

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
        int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = new int[2];

        if (freeSpectrumBands.size() >= 1) {
            int band[] = freeSpectrumBands.get(0);
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen[0] = band[0];
                chosen[1] = chosen[0] + numberOfSlots - 1;
                return chosen;
            }
        }
        return null;
    }
}