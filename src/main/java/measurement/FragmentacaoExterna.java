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
 * Esta classe representa a m�trica de fragmenta��o externa
 * A m�trica representada por esta classe est� associada a um ponto de carga e uma replica��o
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
     * adiciona uma nova observa��o de fragmenta��o externa da rede
     *
     * @param request
     */
    public void addNewObservation(Circuit request) {
        this.observacaoFEVertical();
        this.observacaoFEHorizontal(request);

        quantObs++;
    }

    /**
     * Retorna a Fragmenta��o m�dia entre todos os enlaces da rede
     *
     * @return
     */
    public double getFEVertical() {
        return FEVertical / (double) quantObs;
    }

    /**
     * Retorna a Fragmenta�ao m�dia observada para a intersec��o das faixas de espectro livres em cada enlace das rotas de cada requisi��o
     *
     * @return
     */
    public double getFEHorizontal() {
        return FEHorizontal / (double) quantObs;
    }

    /**
     * Retorna a fragmenta��o externa m�dia calculada para cada link individualmente
     *
     * @param link
     * @return
     */
    public double getFeLink(String link) {
        Double aux = FELinks.get(link);

        return aux / (double) quantObs;
    }

    /**
     * Este m�todo soma a fragmenta��o observada em cada link e tamb�m a m�dia de fragmenta��o externa da rede
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
     * Este m�todo calcula a fragmenta��o externa de forma horizontal, ou seja, a fragmenta��o externa observada na intersecs�o das faixas de espectro livres em uma determinada rota de uma requisi��o
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
