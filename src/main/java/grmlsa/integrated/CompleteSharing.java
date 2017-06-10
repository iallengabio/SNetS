package grmlsa.integrated;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import network.Circuit;
import network.Mesh;
import util.IntersectionFreeSpectrum;

import java.util.List;


public class CompleteSharing implements IntegratedRSAAlgoritm {

    private NewKShortestPaths kMenores;
    private ModulationSelector modulationSelector;

    @Override
    public boolean rsa(Circuit request, Mesh mesh) {
        if (kMenores == null) kMenores = new NewKShortestPaths(mesh, 3); //este algoritmo utiliza 3 caminhos alternativos
        if (modulationSelector == null)
            modulationSelector = new ModulationSelector(mesh.getLinkList().get(0).getSlotSpectrumBand(), mesh.getGuardBand(), mesh);


        List<Route> candidateRoutes = kMenores.getRoutes(request.getSource(), request.getDestination());
        Route rotaEscolhida = null;
        int faixaEscolhida[] = {999999, 999999}; //valor jamais atingido

        for (Route r : candidateRoutes) {
            //calcular quantos slots são necessários para esta rota
            request.setRoute(r);
            Modulation mod = modulationSelector.selectModulation(request, mesh);

            List<int[]> merge = IntersectionFreeSpectrum.merge(r);

            int ff[] = FirstFit.firstFit(mod.requiredSlots(request.getRequiredBandwidth()), merge);

            if (ff != null && ff[0] < faixaEscolhida[0]) {
                faixaEscolhida = ff;
                rotaEscolhida = r;
            }
        }

        if (rotaEscolhida != null) { //se não houver rota escolhida é por que não foi encontrado recurso disponível em nenhuma das rotas candidatas
            request.setRoute(rotaEscolhida);
            request.setModulation(modulationSelector.selectModulation(request));
            request.setSpectrumAssigned(faixaEscolhida);


            return true;

        } else {
            request.setRoute(candidateRoutes.get(0));
            request.setModulation(modulationSelector.selectModulation(request));
            return false;
        }

    }

}
