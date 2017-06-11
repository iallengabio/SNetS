package measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import network.Mesh;
import network.Node;
import request.RequestForConnection;

/**
 * This class stores the measures regarding the use of transmitters and receivers for 
 * a load point / replication
 *
 * @author Iallen
 */
public class TransmittersReceiversUtilization extends Measurement {

    private Mesh mesh;

    private double numberObservations;

    private double avgTxUtilization;
    private double avgRxUtilization;

    private HashMap<String, Double> avgTxUtilizationPerNode;
    private HashMap<String, Double> avgRxUtilizationPerNode;

    private HashMap<String, Integer> maxTxUtilizationPerNode;
    private HashMap<String, Integer> maxRxUtilizationPerNode;

    /**
     * Creates a new instance of TransmitersReceiversUtilization
     * 
     * @param loadPoint int
     * @param replication int
     * @param mesh Mesh
     */
    public TransmittersReceiversUtilization(int loadPoint, int replication, Mesh mesh) {
        super(loadPoint, replication);
        this.mesh = mesh;
        numberObservations = 0;
        avgTxUtilization = 0.0;
        avgRxUtilization = 0.0;

        avgTxUtilizationPerNode = new HashMap<>();
        avgRxUtilizationPerNode = new HashMap<>();

        maxTxUtilizationPerNode = new HashMap<>();
        maxRxUtilizationPerNode = new HashMap<>();
    }

    /**
     * Adds a new observation to the use of transmitters and receivers
     */
    public void addNewObservation(boolean success, RequestForConnection request) {
        ArrayList<Node> nodes = new ArrayList<>(mesh.getNodeList());

        for (Node node : nodes) {
            avgTxUtilization += node.getTxs().getTxUtilization();
            avgRxUtilization += node.getRxs().getRxUtilization();

            Double txUtNo = avgTxUtilizationPerNode.get(node.getName());
            if (txUtNo == null) txUtNo = 0.0;
            txUtNo += node.getTxs().getTxUtilization();
            avgTxUtilizationPerNode.put(node.getName(), txUtNo);

            Double rxUtNo = avgRxUtilizationPerNode.get(node.getName());
            if (rxUtNo == null) rxUtNo = 0.0;
            rxUtNo += node.getRxs().getRxUtilization();
            avgRxUtilizationPerNode.put(node.getName(), txUtNo);

            Integer maxTxUtNo = maxTxUtilizationPerNode.get(node.getName());
            if (maxTxUtNo == null) maxTxUtNo = 0;
            if (node.getTxs().getTxUtilization() >= maxTxUtNo)
                maxTxUtilizationPerNode.put(node.getName(), node.getTxs().getTxUtilization());

            Integer maxRxUtNo = maxRxUtilizationPerNode.get(node.getName());
            if (maxRxUtNo == null) maxRxUtNo = 0;
            if (node.getRxs().getRxUtilization() >= maxRxUtNo)
                maxRxUtilizationPerNode.put(node.getName(), node.getRxs().getRxUtilization());

        }
        
        numberObservations++;
    }

    /**
     * Returns the average transmitters utilization
     * 
     * @return double
     */
    public double getAvgTxUtilizationGen() {
        return avgTxUtilization / numberObservations;
    }

    /**
     * Returns the average receivers utilization
     * 
     * @return double
     */
    public double getAvgRxUtilizationGen() {
        return avgRxUtilization / numberObservations;
    }

    /**
     * Returns the average transmitters utilization for the node reported as parameter
     * 
     * @param node String
     * @return double
     */
    public double getAvgTxUtilizationPerNode(String node) {
        return avgTxUtilizationPerNode.get(node) / numberObservations;
    }

    /**
     * Returns the average receivers utilization for the node reported as parameter
     * 
     * @param node String
     * @return double
     */
    public double getAvgRxUtilizationPerNode(String node) {
        return avgRxUtilizationPerNode.get(node) / numberObservations;
    }

    /**
     * Returns the maximum transmitters utilization for the node reported as parameter
     * 
     * @param node String
     * @return int
     */
    public int getMaxTxUtilizationPerNode(String node) {
        return maxTxUtilizationPerNode.get(node);
    }

    /**
     * Returns the maximum receivers utilization for the node reported as parameter
     * 
     * @param node String
     * @return int
     */
    public int getMaxRxUtilizationPerNode(String node) {
        return maxRxUtilizationPerNode.get(node);
    }

    /**
     * Returns the HashMap key set
     * The key set corresponds to the nodes that were analyzed by the metric
     * 
     * @return Set<String>
     */
    public Set<String> getNodeNamesSet() {
        return avgTxUtilizationPerNode.keySet();
    }

}
