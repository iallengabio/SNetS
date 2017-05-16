package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grmlsa.NewKMenores;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.LastFit;
import network.Circuit;
import network.Mesh;
import util.IntersectionFreeSpectrum;


public class ZonePartitionTopInvasion implements IntegratedRSAAlgoritm{

	private NewKMenores kMenores;
	private ModulationSelector modulationSelector;
	
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
	public boolean rsa(Circuit request, Mesh mesh) {
		if(kMenores==null) kMenores = new NewKMenores(mesh, 3); //este algoritmo utiliza 3 caminhos alternativos
		if(modulationSelector==null) modulationSelector = new ModulationSelector(mesh.getLinkList().get(0).getSlotSpectrumBand(), mesh.getGuardBand());
		
		
		List<Route> candidateRoutes = kMenores.getRoutes(request.getSource(), request.getDestination());
		Route rotaEscolhida = null;
		int faixaEscolhida[] = {999999,999999}; //valor jamais atingido
		
		//tentar alocar na zona primária
		for (Route r : candidateRoutes) {
			//calcular quantos slots são necessários para esta rota
			request.setRoute(r);
			Modulation mod = modulationSelector.selectModulation(request);
			
			int quantSlots = mod.requiredSlots(request.getRequiredBandwidth());
			int zone[] = this.zones.get(quantSlots);
			List<int[]> primaryZone = new ArrayList<>();
			primaryZone.add(zone);			
			
			List<int[]> merge = IntersectionFreeSpectrum.merge(r);
			merge = IntersectionFreeSpectrum.merge(merge, primaryZone);
			
			int ff[] = FirstFit.firstFit(quantSlots, merge);
			
			if(ff!=null && ff[0]<faixaEscolhida[0]){
				faixaEscolhida = ff;
				rotaEscolhida = r;				
			}
		}
		//se não foi possível alocar nenhum recurso, tentar uma invasão na zona mais disponível
		if(rotaEscolhida==null){
			faixaEscolhida[0] = -1;
			faixaEscolhida[1] = -1;
			double maisLivre = 0;
			for (Route r : candidateRoutes) {
				//calcular quantos slots são necessários para esta rota
				request.setRoute(r);
				Modulation mod = modulationSelector.selectModulation(request);
				
				int quantSlots = mod.requiredSlots(request.getRequiredBandwidth());
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
				
				int lf[] = LastFit.lastFit(quantSlots, merge);
				
				if(lf!=null && aux>maisLivre){
					faixaEscolhida = lf;
					rotaEscolhida = r;		
					maisLivre = aux;
					
				}
			}
			
		}
		
		if(rotaEscolhida!=null){ //se não houver rota escolhida é por que não foi encontrado recurso disponível em nenhuma das rotas candidatas
			request.setRoute(rotaEscolhida);
			request.setModulation(modulationSelector.selectModulation(request));
			request.setSpectrumAssigned(faixaEscolhida);
			
			return true;
			
		}else{
			request.setRoute(candidateRoutes.get(0));
			request.setModulation(modulationSelector.selectModulation(request));
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
