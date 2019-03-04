package grmlsa.modulation;

import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;

/**
 * this class implement the modulation selection algorithm that returns modulation robust enough to satisfy 
 * the request with the highest bit rate per possible symbol
 * 
 * @author Iallen
 */
public class ModulationSelectionByDistance implements ModulationSelectionAlgorithmInterface {

	private List<Modulation> avaliableModulations;
	
	@Override
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		double maxBPS = 0.0;
		Modulation resMod = null;
		
		for (Modulation mod : avaliableModulations) {
			if(mod.getMaxRange() >= route.getDistanceAllLinks()){//Modulation robust enough for this requirement
				if(mod.getBitsPerSymbol() > maxBPS){ //Choose the modulation with the largest number of bits per possible symbol
					resMod = mod;
					maxBPS = mod.getBitsPerSymbol();
				}
			}
		}
		
		if(resMod == null) {
			resMod = avaliableModulations.get(0); // To avoid metric error
		}
		
		return resMod;
	}

	@Override
	public List<Modulation> getAvaliableModulations() {
		return avaliableModulations;
	}

	@Override
	public void setAvaliableModulations(List<Modulation> avaliableModulations){
		this.avaliableModulations = avaliableModulations;
	}
	
}
