package grmlsa.spectrumAssignment;

import java.util.List;
import java.util.Map;

import network.Circuit;
import network.ControlPlane;
import network.Transmitters;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the spectrum allocation technique called FirstLastFit.
 * Algorithm based on: Spectrum management techniques for elastic optical networks: A survey (2014)
 *                     Routing and spectrum allocation in elastic optical networks: A tutorial (2015)
 * 
 * FirstLastFit algorithm uses the largerBand parameter to choose between FisrtFit or LastFit.
 * The largerBand parameter represents a transmission rate in bit per seconds.
 * The value of largerBand must be entered in the configuration file "others" as shown below.
 * {"variables":{
 *               "largerBand":"80.0E+9"
 *               }
 * }
 * 
 * @author Alexandre
 */
public class FirstLastFit implements SpectrumAssignmentAlgorithmInterface {
	
	private Double largerBand; // bandwidth used to select between FirstFit or LastFit

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
	public static int[] firstFit(int numberOfSlots, List<int[]> freeSpectrumBands){
		int chosen[] = null;
		for (int[] band : freeSpectrumBands) {
			if(band[1] - band[0] + 1 >= numberOfSlots){
				chosen = band.clone();
				chosen[1] = chosen[0] + numberOfSlots - 1;//it is not necessary to allocate the entire band, only the number of slots necessary
				break;
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
	public static int[] lastFit(int numberOfSlots, List<int[]> freeSpectrumBands){
		int chosen[] = null;
		int band[] = null;
		int i;
		for (i = freeSpectrumBands.size()-1; i >= 0; i--) {
			band = freeSpectrumBands.get(i);
			if(band[1] - band[0] + 1 >= numberOfSlots){
				chosen = band.clone();
				chosen[0] = chosen[1] - numberOfSlots + 1;//it is not necessary to allocate the entire band, only the number of slots necessary
				break;
			}
		}
		return chosen;
	}

	@Override
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		if(numberOfSlots> Transmitters.MAX_SPECTRAL_AMPLITUDE) return null;
		if(largerBand == null){
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			largerBand = Double.parseDouble((String)uv.get("largerBand"));
		}
		
		if (circuit.getRequiredBandwidth() >= largerBand) {
			return firstFit(numberOfSlots, freeSpectrumBands);
			
		}else {
			return lastFit(numberOfSlots, freeSpectrumBands);
		}
	}
}
