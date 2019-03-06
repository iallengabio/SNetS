package grmlsa.spectrumAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the spectrum allocation technique called Spectrum Assignment with Interference Reduction (SAIR).
 * Algorithm based on: Alocacao de Espectro com Reducao de Interferencias entre Circuitos em Rede Opticas Elasticas (2018)
 * 
 * SAIR algorithm uses the largerBand parameter to choose between FisrtFit or LastFit.
 * The largerBand parameter represents a transmission rate in bit per seconds.
 * The value of largerBand must be entered in the configuration file "others" as shown below.
 * {"variables":{
 *               "largerBand":"80.0E+9"
 *               }
 * }
 * 
 * @author Alexandre
 */
public class SpectrumAssignmentWithInterferenceReduction implements SpectrumAssignmentAlgorithmInterface {

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
	 * Applies the FirstFit policy to a certain list of free bands and returns the chosen band taking into account the interference between circuits
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @param circuit Circuit
	 * @param cp ControlPlane
	 * @return int[]
	 */
	public static int[] firstFitChooseByInterference(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int chosen[] = null;
		ArrayList<int[]> freeBands = new ArrayList<int[]>();
		
		for (int band[] : freeSpectrumBands) { // checks and guard the free bands that can establish the circuit
			if(band[1] - band[0] + 1 >= numberOfSlots){
				int bandTemp[] = band.clone();
				freeBands.add(bandTemp);
			}
		}
		
		if(freeBands.size() > 0){ // if there are free bands, select the one that causes less impact on the other active circuits
			int numOfSlotsTotal = circuit.getRoute().getLinkList().firstElement().getNumOfSlots();
			
			int size = freeBands.size();
			if((size == 1) && ((freeBands.get(0)[0] == 1) && (freeBands.get(0)[1] == numOfSlotsTotal))){ // there is only one band (totally free spectrum)
				chosen = freeBands.get(0);
				chosen[1] = chosen[0] + numberOfSlots - 1;
				
			} else {
				// first opticon
				int chosenBand[] = null;
				double chosenWorstDeltaSNR = 0.0; // band interference
				
				// to avoid metrics error
				int chosenBand2[] = null;
				double chosenWorstDeltaSNR2 = 0.0;
				
				List<Circuit> circuitList = new ArrayList<Circuit>();
				for (Link link : circuit.getRoute().getLinkList()) {
					TreeSet<Circuit> circuitsTemp = link.getCircuitList();
					
					for(Circuit circuitTemp : circuitsTemp){
						if(!circuit.equals(circuitTemp) && !circuitList.contains(circuitTemp)){
							circuitList.add(circuitTemp);
						}
					}
				}
				
				for(int indexBand = 0; indexBand < size; indexBand++){
					int band[] = freeBands.get(indexBand);
					
					for(int f = 0; f < 2; f++){ // to check if it is best to allocate next to the lowest slot index or the highest slot index of the band
						int bandTemp[] = band.clone();
						
						if(f == 0){ // lowest slot index of the band
							bandTemp[1] = band[0] + numberOfSlots - 1;
							
						}else{ // highest slot index of the band
							bandTemp[0] = band[1] - numberOfSlots + 1;
						}
						
						circuit.setSpectrumAssigned(bandTemp);
						
						double worstDeltaSNR = Double.MAX_VALUE; // minimum delta SNR
						boolean impactOnOtherRequest = false; // without significant impact on other circuits
						
						for(int i = 0; i < circuitList.size(); i++){
							Circuit circuitTemp = circuitList.get(i);
							boolean QoT = cp.computeQualityOfTransmission(circuitTemp, circuit);
							
							double SNRthreshold = circuitTemp.getModulation().getSNRthreshold();
							double deltaSNR = circuitTemp.getSNR() - SNRthreshold;
							
							if(deltaSNR < worstDeltaSNR){
								worstDeltaSNR = deltaSNR;
							}
							
							if(!QoT){ // circuit with unacceptable QoT, has the worst deltaSNR
								impactOnOtherRequest = true; // with significant impact on one of the other circuits
								break;
							}
						}
						
						if(!impactOnOtherRequest && (worstDeltaSNR > chosenWorstDeltaSNR)){
							chosenWorstDeltaSNR = worstDeltaSNR;
							chosenBand = bandTemp;
							
						} else if(worstDeltaSNR > chosenWorstDeltaSNR2){
							chosenWorstDeltaSNR2 = worstDeltaSNR;
							chosenBand2 = bandTemp;
						}
					}
				}
				
				if((chosenBand == null) && (chosenBand2 != null)){
					chosenBand = chosenBand2;
				}
				
				chosen = chosenBand;
			}
		}
		
		return chosen;
	}
	
	/**
	 * Applies the LastFit policy to a certain list of free bands and returns the chosen band taking into account the interference between circuits
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @param circuit Circuit
	 * @param cp ControlPlane
	 * @return int[]
	 */
	public static int[] lastFitChooseByInterference(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int chosen[] = null;
		ArrayList<int[]> freeBands = new ArrayList<int[]>();
		
		for (int band[] : freeSpectrumBands) { // checks and guard the free bands that can establish the circuit
			if(band[1] - band[0] + 1 >= numberOfSlots){
				int bandTemp[] = band.clone();
				freeBands.add(bandTemp);
			}
		}
		
		if(freeBands.size() > 0){ // if there are free bands, select the one that causes less impact on the other active circuits
			int numOfSlotsTotal = circuit.getRoute().getLinkList().firstElement().getNumOfSlots();
			
			int size = freeBands.size();
			if((size == 1) && ((freeBands.get(0)[0] == 1) && (freeBands.get(0)[1] == numOfSlotsTotal))){ // there is only one band (totally free spectrum)
				chosen = freeBands.get(0);
				chosen[0] = chosen[1] - numberOfSlots + 1;
				
			} else {
				// first option
				int chosenBand[] = null;
				double chosenWorstDeltaSNR = 0.0; // band interference
				
				// to avoid metrics error
				int chosenBand2[] = null;
				double chosenWorstDeltaSNR2 = 0.0;
				
				List<Circuit> circuitList = new ArrayList<Circuit>();
				for (Link link : circuit.getRoute().getLinkList()) {
					TreeSet<Circuit> circuitsTemp = link.getCircuitList();
					
					for(Circuit circuitTemp : circuitsTemp){
						if(!circuit.equals(circuitTemp) && !circuitList.contains(circuitTemp)){
							circuitList.add(circuitTemp);
						}
					}
				}
				
				for(int indexBand = size - 1; indexBand >= 0; indexBand--){
					int band[] = freeBands.get(indexBand);
					
					for(int f = 1; f >= 0; f--){ // to check if it is best to allocate next to the lowest slot index or the highest slot index of the band
						int bandTemp[] = band.clone();
						
						if(f == 0){ // lowest slot index of the band
							bandTemp[1] = band[0] + numberOfSlots - 1;
							
						}else{ // highest slot index of the band
							bandTemp[0] = band[1] - numberOfSlots + 1;
						}
						
						circuit.setSpectrumAssigned(bandTemp);
						
						double worstDeltaSNR = Double.MAX_VALUE; // minimum delta SNR
						boolean impactOnOtherRequest = false; // without significant impact on other circuits
						
						for(int i = 0; i < circuitList.size(); i++){
							Circuit circuitTemp = circuitList.get(i);
							boolean QoT = cp.computeQualityOfTransmission(circuitTemp, circuit);
							
							double SNRthreshold = circuitTemp.getModulation().getSNRthreshold();
							double deltaSNR = circuitTemp.getSNR() - SNRthreshold;
							
							if(deltaSNR < worstDeltaSNR){
								worstDeltaSNR = deltaSNR;
							}
							
							if(!QoT){ // circuit with unacceptable QoT, has the worst deltaSNR
								impactOnOtherRequest = true; // with significant impact on one of the other circuits
								break;
							}
						}
						
						if(!impactOnOtherRequest && (worstDeltaSNR > chosenWorstDeltaSNR)){
							chosenWorstDeltaSNR = worstDeltaSNR;
							chosenBand = bandTemp;
							
						} else if(worstDeltaSNR > chosenWorstDeltaSNR2){
							chosenWorstDeltaSNR2 = worstDeltaSNR;
							chosenBand2 = bandTemp;
						}
					}
				}
				
				if((chosenBand == null) && (chosenBand2 != null)){
					chosenBand = chosenBand2;
				}
				
				chosen = chosenBand;
			}
		}
		
		return chosen;
	}
	
	@Override
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
		
		if(numberOfSlots> maxAmplitude) return null;
		
		if(largerBand == null){
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			largerBand = Double.parseDouble((String)uv.get("largerBand"));
		}
		
		if (circuit.getRequiredBandwidth() >= largerBand) {
			return firstFitChooseByInterference(numberOfSlots, freeSpectrumBands, circuit, cp);
			
		}else {
			return lastFitChooseByInterference(numberOfSlots, freeSpectrumBands, circuit, cp);
		}
	}
}
