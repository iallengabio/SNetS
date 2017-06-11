package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.Mesh;
import util.IntersectionFreeSpectrum;


public class DedicatedPartition implements IntegratedRMLSAAlgorithmInterface{

	private NewKShortestPaths kMenores;
	private ModulationSelector modulationSelector;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
	
	private HashMap<Integer, int[]> zones; 
	
	public DedicatedPartition(){
		
		List<int[]> zones = null;
		try {
			
			//zones = ZonesFileReader.readTrafic(Util.projectPath + "/zones");
		} catch (Exception e) {
			System.out.println("n�o foi poss�vel ler o arquivo com a especifica��o das zonas!");
			
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
		if(kMenores==null){
			kMenores = new NewKShortestPaths(mesh, 3); //este algoritmo utiliza 3 caminhos alternativos
		}
		if(modulationSelector==null){
			modulationSelector = new ModulationSelector(mesh.getLinkList().get(0).getSlotSpectrumBand(), mesh.getGuardBand(), mesh);
		}
		if(spectrumAssignment == null){
			spectrumAssignment = new FirstFit();
		}
		
		List<Route> candidateRoutes = kMenores.getRoutes(request.getSource(), request.getDestination());
		Route rotaEscolhida = null;
		Modulation modEscolhida = null;
		int faixaEscolhida[] = {999999,999999}; //valor jamais atingido
		
		for (Route r : candidateRoutes) {
			//calcular quantos slots s�o necess�rios para esta rota
			request.setRoute(r);
			Modulation mod = modulationSelector.selectModulation(request, r, spectrumAssignment, mesh);
			
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
				modEscolhida = mod;
			}
		}
		
		if(rotaEscolhida!=null){ //se n�o houver rota escolhida � por que n�o foi encontrado recurso dispon�vel em nenhuma das rotas candidatas
			request.setRoute(rotaEscolhida);
			request.setModulation(modEscolhida);
			request.setSpectrumAssigned(faixaEscolhida);
			
			return true;
			
		}else{
			request.setRoute(candidateRoutes.get(0));
			request.setModulation(modulationSelector.getAvaliableModulations().get(0));
			return false;
		}
		
	}

}
