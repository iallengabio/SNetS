package grmlsa.integrated;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentIterface;
import network.Circuit;
import network.Mesh;
import util.IntersectionFreeSpectrum;

import java.util.List;


public class CompleteSharing implements IntegratedRSAAlgoritm {

    private NewKShortestPaths kMenores;
    private ModulationSelector modulationSelector;
    private SpectrumAssignmentIterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit request, Mesh mesh) {
        if (kMenores == null){
        	kMenores = new NewKShortestPaths(mesh, 3); //este algoritmo utiliza 3 caminhos alternativos
        }
        if (modulationSelector == null){
            modulationSelector = new ModulationSelector(mesh.getLinkList().get(0).getSlotSpectrumBand(), mesh.getGuardBand(), mesh);
        }
        if(spectrumAssignment == null){
			spectrumAssignment = new FirstFit();
		}

        List<Route> candidateRoutes = kMenores.getRoutes(request.getSource(), request.getDestination());
        Route rotaEscolhida = null;
        Modulation modEscolhida = null;
        int faixaEscolhida[] = {999999, 999999}; //valor jamais atingido

        for (Route r : candidateRoutes) {
            //calcular quantos slots são necessários para esta rota
            request.setRoute(r);
            Modulation mod = modulationSelector.selectModulation(request, r, spectrumAssignment, mesh);

            List<int[]> merge = IntersectionFreeSpectrum.merge(r);

            int ff[] = FirstFit.firstFit(mod.requiredSlots(request.getRequiredBandwidth()), merge);

            if (ff != null && ff[0] < faixaEscolhida[0]) {
                faixaEscolhida = ff;
                rotaEscolhida = r;
                modEscolhida = mod;
            }
        }

        if (rotaEscolhida != null) { //se não houver rota escolhida é por que não foi encontrado recurso disponível em nenhuma das rotas candidatas
            request.setRoute(rotaEscolhida);
            request.setModulation(modEscolhida);
            request.setSpectrumAssigned(faixaEscolhida);

            return true;

        } else {
            request.setRoute(candidateRoutes.get(0));
            request.setModulation(modulationSelector.getAvaliableModulations().get(0));
            return false;
        }

    }

}
