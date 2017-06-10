package measurement;

import network.Circuit;
import network.Link;
import network.Mesh;
import util.ComputesFragmentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the relative fragmentation metric
 * The metric represented by this class is associated with a load point and a replication
 *
 * @author Iallen
 */
public class RelativeFragmentation extends Measurement {

    public final static String SEP = "-";

    private HashMap<Integer, Double> relativeFrag;
    private int numberObservations;
    private Mesh mesh;

    /**
     * Creates a new instance of RelativeFragmentation
     * 
     * @param loadPoint int
     * @param rep int
     * @param mesh Mesh
     */
    public RelativeFragmentation(int loadPoint, int rep, Mesh mesh) {
        super(loadPoint, rep);
        this.mesh = mesh;
        relativeFrag = new HashMap<>();
        numberObservations = 0;
        // Configure the desired relative fragmentations
        relativeFrag.put(1, 0.0);
        relativeFrag.put(2, 0.0);
        relativeFrag.put(3, 0.0);
        relativeFrag.put(5, 0.0);
    }

    /**
     * Adds a new observation of external fragmentation of the network
     *
     * @param request
     */
    public void addNewObservation(Circuit request) {
        this.observationLinks();
        numberObservations++;
    }

    /**
     * Makes a observation of the average relative fragmentation on all links for each configured c value
     */
    private void observationLinks() {
        for (Integer c : relativeFrag.keySet()) {
            this.observationAllLinks(c);
        }
    }

    /**
     * Make a note of the average relative fragmentation on all links to the value of c passed as a parameter
     *
     * @param c
     */
    private void observationAllLinks(Integer c) {
        double averageFragLink = 0.0;
        ComputesFragmentation cf = new ComputesFragmentation();
        for (Link link : mesh.getLinkList()) {
            double fAux = cf.relativeFragmentation(link.getFreeSpectrumBands(), c);
            averageFragLink += fAux;
        }
        averageFragLink = averageFragLink / ((double) mesh.getLinkList().size());

        double fCurrent = this.relativeFrag.get(c);
        fCurrent += averageFragLink;
        this.relativeFrag.put(c, fCurrent);
    }

    /**
     * Returns the list of configured C values for the realization of observations relative 
     * of relative fragmentation
     * 
     * @return
     */
    public List<Integer> getCList() {
        return new ArrayList<>(relativeFrag.keySet());
    }

    /**
     * Returns the average relative fragmentation
     * 
     * @param c int
     * @return double
     */
    public double getAverageRelativeFragmentation(int c) {
        return this.relativeFrag.get(c) / ((double) this.numberObservations);
    }

}
