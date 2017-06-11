package grmlsa.modulation;

import java.util.ArrayList;
import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentInterface;
import network.Circuit;
import network.Mesh;

public class ModulationSelector {
	
	private List<Modulation> avaliableModulations;
	
	/**
	 * Distancias baseadas nos artigos:
	 * "Efficient Resource Allocation for All-Optical Multicasting Over Spectrum-Sliced Elastic Optical Networks"
	 * "Evaluating Internal Blocking in Noncontentionless Flex-grid ROADMs"
	 * "On the Complexity of Routing and Spectrum Assignment in Flexible-Grid Ring Networks"
	 * 
	 * Os outros valores:
	 * "Error Vector Magnitude as a Performance Measure for Advanced Modulation Formats"
	 * 
	 * @param freqSlot
	 * @param guardBand
	 */
	public ModulationSelector(double freqSlot, int guardBand, Mesh mesh){
		avaliableModulations = new ArrayList<>();
		//String name, double bitsPerSymbol, double freqSlot, double maxRange, int guardBand, double level, double k2, double M
		avaliableModulations.add(new Modulation("BPSK", 1.75, freqSlot, 10000.0, guardBand, 2.0, 1.0, 2.0, mesh));
		avaliableModulations.add(new Modulation("QPSK", 3.33, freqSlot, 5000.0, guardBand, 3.0, 1.0, 4.0, mesh));
		avaliableModulations.add(new Modulation("8QAM", 4.50, freqSlot, 2500.0, guardBand, 4.0, 1.0, 8.0, mesh));
		avaliableModulations.add(new Modulation("16QAM", 6.67, freqSlot, 1250.0, guardBand, 5.0, 1.8, 16.0, mesh));
		avaliableModulations.add(new Modulation("32QAM", 13.32, freqSlot, 625.0, guardBand, 6.0, 1.7, 32.0, mesh));
		avaliableModulations.add(new Modulation("64QAM", 23.64, freqSlot, 312.0, guardBand, 7.0, 2.333, 64.0, mesh));
	}
	
	/**
	 * Este metodo seleciona o processo de selecao do formato de modulacao
	 * O formato de modulacao pode ser selecionado pelo seu alcance ou pela QoT
	 * @param request - Request
	 * @param route - Route
	 * @param spectrumAssignment - SpectrumAssignmentAlgoritm
	 * @return Modulation
	 */
	public Modulation selectModulation (Circuit circuit, Route route, SpectrumAssignmentInterface spectrumAssignment, Mesh mesh) {
		Modulation resMod = null;
		
		if(mesh.getPhysicalLayer().isActiveQoT()){
			resMod = selectModulationByQoT(circuit, route, spectrumAssignment, mesh);
		}else{
			resMod = selectModulationByDistance(circuit, route);
		}
		
		return resMod;
	}

	/**
	 * retorna modulacao robusta o suficiente para atender a requisicao com a maior taxa de bits por simbolo possivel
	 * @param request - Request
	 * @param route - Route
	 * @return Modulation
	 */
	public Modulation selectModulationByDistance (Circuit circut, Route route){
		double maxBPS = 0.0;
		Modulation resMod = null;
		
		for (Modulation mod : avaliableModulations) {
			if(mod.getMaxRange() >= route.getDistanceAllLinks()){//modulacao robusta o suficiente para esta requisicao
				if(mod.getBitsPerSymbol() > maxBPS){ //escolher a modulacao com maior quantidade de bits por simbolo possivel
					resMod = mod;
					maxBPS = mod.getBitsPerSymbol();
				}
			}
		}
		
		return resMod;
	}
	
	/**
	 * retorna modulacao robusta o suficiente para atender a requisicao com a maior taxa de bits por simbolo possivel
	 * considera a qualiade de transmissao
	 * @param request - Request
	 * @param route - Route
	 * @param spectrumAssignment - SpectrumAssignmentAlgoritm
	 * @return Modulation
	 */
	public Modulation selectModulationByQoT(Circuit circuit, Route route, SpectrumAssignmentInterface spectrumAssignment, Mesh mesh){
		Modulation resMod = null; //para QoT admissivel
		Modulation alternativeMod = null; //para alocar espectro
		
		for(int i = 0; i < avaliableModulations.size(); i++){
			Modulation mod = avaliableModulations.get(i);
			
			if(spectrumAssignment.assignSpectrum(mod.requiredSlots(circuit.getRequiredBandwidth()), circuit)){
				if(alternativeMod == null){
					alternativeMod = mod; //guarda a primeira modulacao que conseguiu alocar espectro
				}
				
				boolean flagQoT = mesh.getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, circuit.getSpectrumAssigned());
				if(flagQoT){
					resMod = mod; //guarda a modulacao que possui QoT admissivel
				}
			}
		}
		
		if(resMod == null){ //QoT inadimissivel para todas as modulacoes
			if(alternativeMod != null){ //alocou spectro usando alguma modulacao, mas a QoT estava inadimissivel
				resMod = alternativeMod;
				circuit.setQoT(false); //para marcar que o bloqueio foi por QoT inadimissivel
			}
		}
		
		return resMod;
	}
	
	/**
	 * Este metodo retorna os formatos de modulacao disponiveis
	 * @return the avaliableModulations - List<Modulation>
	 */
	public List<Modulation> getAvaliableModulations() {
		return avaliableModulations;
	}
	
}
