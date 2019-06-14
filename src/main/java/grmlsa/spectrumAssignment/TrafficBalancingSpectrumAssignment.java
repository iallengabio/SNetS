package grmlsa.spectrumAssignment;

import java.util.ArrayList;
import java.util.List;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 * Algorithm based on: A Quality-of-Transmission Aware Dynamic Routing and Spectrum Assignment 
 *                     Scheme for Future Elastic Optical Networks (2013)
 * TBSA algorithm attempts to perform load balancing on the spectrum allocation
 * 
 * @author Alexandre
 */
public class TrafficBalancingSpectrumAssignment implements SpectrumAssignmentAlgorithmInterface {
	
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
    	if(freeSpectrumBands.size() == 0){
    		return null;
    	}
    	
    	int chosen[] = null;
		List<int[]> lowFreeBands = null;
		List<int[]> upperFreeBands = null;
		int index, div, sumLowBW, sumUpperBW;
		List<int[]> chosenFreeBands = freeSpectrumBands;
		
		do{
			index = 0;
			if(chosenFreeBands.size() > 1){
				if(chosenFreeBands.size() % 2 == 0){
					index = chosenFreeBands.size() / 2;
					
				} else {
					index = (chosenFreeBands.size() / 2) + 1;
				}
			}
			div = chosenFreeBands.get(index)[0];
			
			lowFreeBands = new ArrayList<>();
			upperFreeBands = new ArrayList<>();
			sumLowBW = 0;
			sumUpperBW = 0;
			
			for (int[] band : chosenFreeBands) {
				if((band[0] < div) && (band[1] < div)){
					if(band[1] - band[0] + 1 >= numberOfSlots){
						lowFreeBands.add(band);
						sumLowBW += band[1] - band[0] + 1;
					}
				}else{
					if(band[1] - band[0] + 1 >= numberOfSlots){
						upperFreeBands.add(band);
						sumUpperBW += band[1] - band[0] + 1;
					}
				}
			}
			
			if(sumLowBW > sumUpperBW){
				chosenFreeBands = lowFreeBands;
			}else{
				chosenFreeBands = upperFreeBands;
			}
			
			if(chosenFreeBands.size() == 0){
				return null;
			}
			
		}while(chosenFreeBands.size() != 1);
		
		chosen = chosenFreeBands.get(0).clone();
		chosen[1] = chosen[0] + numberOfSlots - 1;
		
		return chosen;
	}
}
