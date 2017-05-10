package measurement;

import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import util.CalculadorFragmentacao;
import util.IntersectionFreeSpectrum;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Esta classe representa a métrica de fragmentação externa
 * A métrica representada por esta classe está associada a um ponto de carga e uma replicação
 *
 * @author Iallen
 */
public class FragmentacaoExterna extends Measurement {

    public final static String SEP = "-";


    private int quantObs;
    private double FEVertical;
    private double FEHorizontal;
    private Mesh mesh;

    private HashMap<String, Double> FELinks;


    public FragmentacaoExterna(int loadPoint, int rep, Mesh mesh) {
        super(loadPoint, rep);
        this.mesh = mesh;
        this.loadPoint = loadPoint;
        this.replication = rep;
        this.quantObs = 0;
        FELinks = new HashMap<>();

    }


    /**
     * adiciona uma nova observação de fragmentação externa da rede
     *
     * @param request
     */
    public void addNewObservation(Circuit request) {
        this.observacaoFEVertical();
        this.observacaoFEHorizontal(request);

        quantObs++;
    }

    /**
     * Retorna a Fragmentação média entre todos os enlaces da rede
     *
     * @return
     */
    public double getFEVertical() {
        return FEVertical / (double) quantObs;
    }

    /**
     * Retorna a Fragmentaçao média observada para a intersecção das faixas de espectro livres em cada enlace das rotas de cada requisição
     *
     * @return
     */
    public double getFEHorizontal() {
        return FEHorizontal / (double) quantObs;
    }

    /**
     * Retorna a fragmentação externa média calculada para cada link individualmente
     *
     * @param link
     * @return
     */
    public double getFeLink(String link) {
        Double aux = FELinks.get(link);

        return aux / (double) quantObs;
    }

    /**
     * Este método soma a fragmentação observada em cada link e também a média de fragmentação externa da rede
     */
    private void observacaoFEVertical() {
        Double aux, aux2;
        double feMedia = 0.0;
        CalculadorFragmentacao cf = new CalculadorFragmentacao();
        for (Link link : mesh.getLinkList()) {
            aux = FELinks.get(link.getSource().getName() + SEP + link.getDestination().getName());
            if (aux == null) aux = 0.0;
            aux2 = cf.fragmentacaoExterna(link.getFreeSpectrumBands(), link.getNumOfSlots());
            aux += aux2;
            FELinks.put(link.getSource().getName() + SEP + link.getDestination().getName(), aux);
            feMedia += aux2;
        }
        feMedia = feMedia / (double) mesh.getLinkList().size();
        FEVertical += feMedia;
    }

    /**
     * Este método calcula a fragmentação externa de forma horizontal, ou seja, a fragmentação externa observada na intersecsão das faixas de espectro livres em uma determinada rota de uma requisição
     */
    private void observacaoFEHorizontal(Circuit request) {
        if (request.getRoute() == null) return;
        List<Link> links = request.getRoute().getLinkList();

        List<int[]> composition;
        composition = links.get(0).getFreeSpectrumBands();
        int i;

        for (i = 1; i < links.size(); i++) {
            composition = IntersectionFreeSpectrum.merge(composition, links.get(i).getFreeSpectrumBands());
        }
        CalculadorFragmentacao cf = new CalculadorFragmentacao();

        FEHorizontal += cf.fragmentacaoExterna(composition, links.get(0).getNumOfSlots());

    }

    public Set<String> getLinkSet() {
        return this.FELinks.keySet();
    }


}
