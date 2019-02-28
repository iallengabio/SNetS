package grmlsa.spectrumAssignment;

import java.util.List;
import java.util.Map;

import network.Circuit;
import network.ControlPlane;
import network.Transmitters;
import util.IntersectionFreeSpectrum;

/**
 *  This class represents the spectrum allocation technique called DispersionAdaptiveFirstLastFit.
 *  Algorithm based on: Dispersion-adaptive first-last fit spectrum allocation scheme for 
 *                      elastic optical networks. (2016)
 *  
 *  DispersionAdaptiveFirstLastFit algorithm uses the beta parameter to choose between FisrtFit or LastFit.
 *  The beta parameter represents a distance in kilometers.
 *  The value of beta must be entered in the configuration file "others" as shown below.
 * {"variables":{
 *               "beta":"3000.0"
 *               }
 * }
 * 
 * @author Alexandre
 */
public class DispersionAdaptiveFirstLastFit implements SpectrumAssignmentAlgorithmInterface {

	private Double beta;
	
    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
        List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());

        int chosen[] = policy(numberOfSlots, composition, circuit, cp);
        circuit.setSpectrumAssigned(chosen);
        
        if (chosen == null)
        	return false;

        return true;
    }

	/**
	 * Applies the FirstFit policy to a certain list of free bands and returns the chosen band
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @return int[]
	 */
	public static int[] firstFit(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int chosen[] = null;
		for (int[] band : freeSpectrumBands) {
			if(band[1] - band[0] + 1 >= numberOfSlots){
				chosen = band.clone();
				chosen[1] = chosen[0] + numberOfSlots - 1;//it is not necessary to allocate the entire band, only the number of slots necessary
				
				if(aceitableDispersion(numberOfSlots, chosen, circuit, cp)) {
					break;
				}
			}
		}
		return chosen;
	}
	
	/**
	 * Applies the LastFit policy to a certain list of free bands and returns the chosen band
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @return int[]
	 */
	public static int[] lastFit(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int chosen[] = null;
		int band[] = null;
		int i;
		for (i = freeSpectrumBands.size()-1; i >= 0; i--) {
			band = freeSpectrumBands.get(i);
			if(band[1] - band[0] + 1 >= numberOfSlots){
				chosen = band.clone();
				chosen[0] = chosen[1] - numberOfSlots + 1;//it is not necessary to allocate the entire band, only the number of slots necessary
				
				if(aceitableDispersion(numberOfSlots, chosen, circuit, cp)) {
					break;
				}
			}
		}
		return chosen;
	}
	
	@Override
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		if(numberOfSlots> Transmitters.MAX_SPECTRAL_AMPLITUDE) return null;
		if(beta == null){
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			beta = Double.parseDouble((String)uv.get("beta"));
		}
		
		if (circuit.getRoute().getDistanceAllLinks() > beta) {
			return firstFit(numberOfSlots, freeSpectrumBands, circuit, cp);
			
		}else {
			return lastFit(numberOfSlots, freeSpectrumBands, circuit, cp);
		}
	}
	
	/**
	 * Checks the quality of transmission of the circuit using the spectrum band informed
	 * 
	 * @param numberOfSlots int
	 * @param band int[]
	 * @param circuit Circuit
	 * @param cp ControlPlane
	 * @return boolean
	 */
	public static boolean aceitableDispersion(int numberOfSlots, int[] band, Circuit circuit, ControlPlane cp) {
		circuit.setSpectrumAssigned(band);
		
		boolean QoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, circuit.getRoute(), circuit.getModulation(), band);
		return QoT;
	}
}
