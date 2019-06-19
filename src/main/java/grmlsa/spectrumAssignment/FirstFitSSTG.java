package grmlsa.spectrumAssignment;

import network.Circuit;
import network.ControlPlane;
import network.Transmitters;
import util.IntersectionFreeSpectrum;

import java.util.List;

/**
 * This class represents the spectrum allocation technique called First Fit Expansiveness, this algorithm cant be used in sequencial RMLSA.
 * This technique chooses the first free spectrum band that accommodates the request with some free slots around the chosen slots.
 *
 * @author Iallen
 */
public class FirstFitSSTG implements SpectrumAssignmentAlgorithmInterface {

    private int ffeSigma = 0;

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
        List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());
        int aux = ffeSigma;
        int chosen[] = null;

        chosen = policy(numberOfSlots, composition, circuit, cp);

        circuit.setSpectrumAssigned(chosen);
        
        if (chosen == null)
        	return false;

        return true;
    }

    /**
     * Applies the policy of allocation of spectrum FirstFit
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
    	int aux = ffeSigma;
    	if(numberOfSlots>ffeSigma) aux = numberOfSlots;
        for (int[] band : freeSpectrumBands) {
            int bandSize = band[1] - band[0] + 1;
            if (bandSize >= aux) {
                chosen = band.clone();
                chosen[0] = chosen[0] + ((bandSize - numberOfSlots)/2);
                chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                break;
            }
        }
        return chosen;
	}

    public void setFfeSigma(int ffeSigma) {
        this.ffeSigma = ffeSigma;
    }
}