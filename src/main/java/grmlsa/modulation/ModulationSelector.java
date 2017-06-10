package grmlsa.modulation;

import java.util.ArrayList;
import java.util.List;

import network.Circuit;
import network.ControlPlane;

public class ModulationSelector {
	
	List<Modulation> avaliableModulations;
	
	public ModulationSelector(double freqSlot, int guardBand){
		avaliableModulations = new ArrayList<>();
		avaliableModulations.add(new Modulation("BPSK", 1.67, freqSlot, 10000, guardBand));
		avaliableModulations.add(new Modulation("QPSK", 3.33, freqSlot, 5000, guardBand));
		avaliableModulations.add(new Modulation("16QAM", 6.67, freqSlot, 1000, guardBand));
	}
	
	/**
	 * retorna modula��o robusta o suficiente para atender a requisi��o com a maior taxa de bits por simbolo poss�vel
	 * @param r
	 * @return
	 */
	public Modulation selectModulation(Circuit r){
		
		double maxBPS = 0.0;
		Modulation res = null;
		
		for (Modulation mod : avaliableModulations) {
			if(mod.getMaxRange() >= r.getRoute().getDistanceAllLinks()){//modula��o robusta o suficiente para esta requisi��o
				if(mod.getBitsPerSymbol()>maxBPS){ //escolher a modula��o com maior quantidade de bits por simbolo poss�vel
					res = mod;
				}				
			}
		}
		
		return res;
	}

}
