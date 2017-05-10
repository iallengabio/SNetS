package grmlsa.integrated;

import grmlsa.NewKMenores;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import network.Circuit;
import network.Link;
import network.Mesh;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LoadBalancedDedicatedPartition implements IntegratedRSAAlgoritm {

    private NewKMenores kMenores;
    private ModulationSelector modulationSelector;

    private HashMap<Integer, int[]> zones;

    public LoadBalancedDedicatedPartition() {
        List<int[]> zones = null;
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
        if (kMenores == null) kMenores = new NewKMenores(mesh, 3); //este algoritmo utiliza 3 caminhos alternativos
        if (modulationSelector == null)
            modulationSelector = new ModulationSelector(mesh.getLinkList().get(0).getSlotSpectrumBand(), mesh.getGuardBand());


        List<Route> candidateRoutes = kMenores.getRoutes(request.getSource(), request.getDestination());
        Route rotaEscolhida = null;
        int faixaEscolhida[] = {999999, 999999}; //valor jamais atingido
        int menosUsado = 999999999;

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

            int ut = this.quantSlotsUsadosZona(r, zone);

            if (ff != null && ut < menosUsado) {
                faixaEscolhida = ff;
                rotaEscolhida = r;
                menosUsado = ut;
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

    /**
     * retorna o somatório do quadrado da quantidade de slots utilizados em cada link de uma rota em uma determinada zona
     *
     * @param r
     * @param zone
     * @return
     */
    private int quantSlotsUsadosZona(Route r, int zone[]) {
        int res = 0;
        List<int[]> zoneAux = new ArrayList<int[]>();
        zoneAux.add(zone);

        for (Link link : r.getLinkList()) {
            List<int[]> merge = IntersectionFreeSpectrum.merge(link.getFreeSpectrumBands(), zoneAux);

            int livres = 0;
            for (int[] is : merge) {
                livres += (is[1] - is[0] + 1);
            }
            int usados = link.getNumOfSlots() - livres;

            res += (usados * usados);

        }


        return res;
    }

}
