package grmlsa.integrated;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.LastFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgoritm;
import network.Circuit;
import network.Mesh;
import util.IntersectionFreeSpectrum;

import java.util.HashSet;
import java.util.List;


public class PseudoPartition implements IntegratedRSAAlgoritm {

    /**
     * Larguras de banda que utilizam o espectro de cima para baixo
     */
    private HashSet<Double> largBandSuperiores;

    private NewKShortestPaths kMenores;
    private ModulationSelector modulationSelector;
    private SpectrumAssignmentAlgoritm spectrumAssignment;

    public PseudoPartition() {
        largBandSuperiores = new HashSet<>();
        largBandSuperiores.add(343597383680.0); //320Gbps
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
        int faixaEscolhida[] = new int[2];
        
        //verificar se o firstfit deve ser aplicado de baixo para cima ou de cima para baixo
        if (!largBandSuperiores.contains(request.getRequiredBandwidth())) { // alocar de baixo para cima
            faixaEscolhida[0] = 9999999;
            faixaEscolhida[1] = 9999999;

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

        } else { //alocar de cima para baixo


            faixaEscolhida[0] = -1;
            faixaEscolhida[1] = -1;

            for (Route r : candidateRoutes) {
                //calcular quantos slots são necessários para esta rota
                request.setRoute(r);
                Modulation mod = modulationSelector.selectModulation(request, r, spectrumAssignment, mesh);

                List<int[]> merge = IntersectionFreeSpectrum.merge(r);

                int lf[] = LastFit.lastFit(mod.requiredSlots(request.getRequiredBandwidth()), merge);

                if (lf != null && lf[1] > faixaEscolhida[1]) {
                    faixaEscolhida = lf;
                    rotaEscolhida = r;
                    modEscolhida = mod;
                }
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
