package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.LastFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.Mesh;
import util.IntersectionFreeSpectrum;


public class ZonePartitionTopInvasion implements IntegratedRMLSAAlgorithmInterface{

	private NewKShortestPaths kMenores;
	private ModulationSelector modulationSelector;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment1;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment2;
	
	private HashMap<Integer, int[]> zones; 
	
	public ZonePartitionTopInvasion(){
		List<int[]> zones=null;
		try {
			//zones = ZonesFileReader.readTrafic(Util.projectPath + "/zones");
		} catch (Exception e) {
			System.out.println("não foi possível ler o arquivo com a especificação das zonas!");
			
			//e.printStackTrace();
		}
		this.zones = new HashMap<>();
		int aux[];
		for (int[] zone : zones) {
			aux = new int[2];
			aux[0] = zone[1];
			aux[1] = zone[2];
			this.zones.put(zone[0], aux);			
		}
		
	}
	
	@Override
	public boolean rsa(Circuit circuit, Mesh mesh) {
		if(kMenores == null){
			kMenores = new NewKShortestPaths(mesh, 3); //este algoritmo utiliza 3 caminhos alternativos
		}
		if(modulationSelector == null){
			modulationSelector = new ModulationSelector(mesh.getLinkList().get(0).getSlotSpectrumBand(), mesh.getGuardBand(), mesh);
		}
		if(spectrumAssignment1 == null && spectrumAssignment2 == null){
			spectrumAssignment1 = new FirstFit();
			spectrumAssignment2 = new LastFit();
		}
		
		List<Route> candidateRoutes = kMenores.getRoutes(circuit.getSource(), circuit.getDestination());
		Route rotaEscolhida = null;
		Modulation modEscolhida = null;
		int faixaEscolhida[] = {999999,999999}; //valor jamais atingido
		
		//tentar alocar na zona primária
		for (Route r : candidateRoutes) {
			//calcular quantos slots são necessários para esta rota
			circuit.setRoute(r);
			Modulation mod = modulationSelector.selectModulation(circuit, r, spectrumAssignment1, mesh);
			
			int quantSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			int zone[] = this.zones.get(quantSlots);
			List<int[]> primaryZone = new ArrayList<>();
			primaryZone.add(zone);			
			
			List<int[]> merge = IntersectionFreeSpectrum.merge(r);
			merge = IntersectionFreeSpectrum.merge(merge, primaryZone);
			
			int ff[] = spectrumAssignment1.policy(quantSlots, merge, circuit);
			
			if(ff!=null && ff[0]<faixaEscolhida[0]){
				faixaEscolhida = ff;
				rotaEscolhida = r;	
				modEscolhida = mod;
			}
		}
		//se não foi possível alocar nenhum recurso, tentar uma invasão na zona mais disponível
		if(rotaEscolhida==null){
			faixaEscolhida[0] = -1;
			faixaEscolhida[1] = -1;
			double maisLivre = 0;
			for (Route r : candidateRoutes) {
				//calcular quantos slots são necessários para esta rota
				circuit.setRoute(r);
				Modulation mod = modulationSelector.selectModulation(circuit, r, spectrumAssignment2, mesh);
				
				int quantSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
				int zone[] = this.zones.get(quantSlots);
				List<int[]> merge = IntersectionFreeSpectrum.merge(r);
				
				int zonaMaisLivre = this.buscarZonaMaisLivre(quantSlots, merge);
				
				if(zonaMaisLivre == -1){
					//System.out.println("impossível invasão");
					continue;
				};
				
				//System.out.println("invasão viável");
				List<int[]> secondaryZone = new ArrayList<>();
				secondaryZone.add(zones.get(zonaMaisLivre));
				
				merge = IntersectionFreeSpectrum.merge(merge, secondaryZone);
				
				double aux = ((double) quantLivre(merge)) / ((double)(zones.get(zonaMaisLivre)[1] - zones.get(zonaMaisLivre)[0] + 1));
				
				int lf[] = spectrumAssignment2.policy(quantSlots, merge, circuit);
				
				if(lf!=null && aux>maisLivre){
					faixaEscolhida = lf;
					rotaEscolhida = r;		
					maisLivre = aux;
					modEscolhida = mod;
				}
			}	
		}
		
		if(rotaEscolhida!=null){ //se não houver rota escolhida é por que não foi encontrado recurso disponível em nenhuma das rotas candidatas
			circuit.setRoute(rotaEscolhida);
			circuit.setModulation(modEscolhida);
			circuit.setSpectrumAssigned(faixaEscolhida);
			
			return true;
			
		}else{
			circuit.setRoute(candidateRoutes.get(0));
			circuit.setModulation(modulationSelector.getAvaliableModulations().get(0));
			return false;
		}
		
	}
	
	/**
	 * Este método retorna a zona mais livre onde será feita a invasão.
	 * O método irá retornar a zona que couber mais requisições do tipo dela mesma
	 * O inteiro retornado corresponde à quantidade de slots por requisição da zona selecionada
	 * 
	 * @return
	 */
	private int buscarZonaMaisLivre(int quantSlotsInvasor, List<int[]> merge){
		int res = -1;
		double maiorDisponibilidade = 0;
		List<int[]> aux1, aux2;
		for (Integer z : zones.keySet()) {
			aux1 = new ArrayList<>();
			aux1.add(zones.get(z));
			aux2 = IntersectionFreeSpectrum.merge(merge, aux1);
			int quantLivre = quantLivre(aux2);
			double aux = ((double) quantLivre) / ((double)(zones.get(z)[1] - zones.get(z)[0] + 1));
			
			if(aux > maiorDisponibilidade && quantLivre >= quantSlotsInvasor){
				maiorDisponibilidade = aux;
				res = z;
			}
		}
		
		//System.out.println("buscando zona para invasão, request: " + quantSlotsInvasor + ", zona: " + res);
		
		return res;
	}
	
	private int quantLivre(List<int[]> lF){
		int res = 0;
		
		if(lF.size()>0){
			res = res + lF.get(lF.size()-1)[1] - lF.get(lF.size()-1)[0] + 1;
		}
		
		return res;
	}
}
