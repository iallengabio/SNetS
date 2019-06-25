package measurement;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import request.RequestForConnection;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.ExternalFragmentationResultManager;
import util.ComputesFragmentation;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the external fragmentation metric.
 * The metric represented by this class is associated with a load point and a replication.
 *
 * @author Iallen
 */
public class ExternalFragmentation extends Measurement {

    public final static String SEP = "-";

    private int numberObservations;
    private double ExternalFragVertical;
    private double ExternalFragHorizontal;

    private HashMap<String, Double> ExternalFragLinks;

    /**
     * Creates a new instance of ExternalFragmentation
     * 
     * @param loadPoint int
     * @param rep int
     * @param mesh Mesh
     */
    public ExternalFragmentation(int loadPoint, int rep) {
        super(loadPoint, rep);
        this.loadPoint = loadPoint;
        this.replication = rep;
        this.numberObservations = 0;
        this.ExternalFragLinks = new HashMap<>();

		resultManager = new ExternalFragmentationResultManager();
    }

    /**
     * Adds a new observation of external fragmentation of the network
     *
     * @param cp ControlPlane
     * @param success boolean
     * @param request RequestForConnection
     */
    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
        this.observationExternalFragVertical(cp.getMesh());
        for(Circuit circuit : request.getCircuits()) {
            this.observationExternalFragHorizontal(circuit);
        }
        numberObservations++;
    }

    @Override
    public String getFileName() {
        return SimulationRequest.Result.FILE_EXTERNAL_FRAGMENTATION;
    }

    /**
     * Returns the average Fragmentation between all network links
     *
     * @return double
     */
    public double getExternalFragVertical() {
        return ExternalFragVertical / (double) numberObservations;
    }

    /**
     * Returns the observed mean Fragmentation for the intersection of the free spectrum bands 
     * in each link of the routes of each request
     *
     * @return double
     */
    public double getExternalFragHorizontal() {
        return ExternalFragHorizontal / (double) numberObservations;
    }

    /**
     * Returns the average external fragmentation calculated for each link individually
     *
     * @param link String
     * @return double
     */
    public double getExternalFragLink(String link) {
        Double aux = ExternalFragLinks.get(link);

        return aux / (double) numberObservations;
    }

    /**
     * This method sums the fragmentation observed in each link and also the average 
     * external fragmentation of the network
     */
    private void observationExternalFragVertical(Mesh mesh) {
        Double aux, aux2;
        double externalFragAverage = 0.0;
        ComputesFragmentation cf = new ComputesFragmentation();
        for (Link link : mesh.getLinkList()) {
            aux = ExternalFragLinks.get(link.getSource().getName() + SEP + link.getDestination().getName());
            if (aux == null) aux = 0.0;
            aux2 = cf.externalFragmentation(link.getFreeSpectrumBands());
            aux += aux2;
            ExternalFragLinks.put(link.getSource().getName() + SEP + link.getDestination().getName(), aux);
            externalFragAverage += aux2;
        }
        externalFragAverage = externalFragAverage / (double) mesh.getLinkList().size();
        ExternalFragVertical += externalFragAverage;
    }

    /**
     * This method calculates the external fragmentation horizontally, that is, 
     * the external fragmentation observed at the intersection of the free spectrum 
     * bands in a given route of a request
     */
    private void observationExternalFragHorizontal(Circuit circuit) {
        if (circuit.getRoute() == null) return;
        List<Link> links = circuit.getRoute().getLinkList();

        List<int[]> composition;
        composition = links.get(0).getFreeSpectrumBands();
        int i;

        for (i = 1; i < links.size(); i++) {
            composition = IntersectionFreeSpectrum.merge(composition, links.get(i).getFreeSpectrumBands());
        }
        ComputesFragmentation cf = new ComputesFragmentation();

        ExternalFragHorizontal += cf.externalFragmentation(composition);
    }

    /**
     * Returns the HashMap key set
     * The key set corresponds to the links that were analyzed by the metric
     * 
     * @return Set<String>
     */
    public Set<String> getLinkSet() {
        return this.ExternalFragLinks.keySet();
    }

}
