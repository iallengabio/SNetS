package measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import network.ControlPlane;
import network.Node;
import request.RequestForConnection;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.TransmittersReceiversRegeneratorsUtilizationResultManager;

/**
 * This class stores the measures regarding the use of transmitters, receivers, and 
 * regenerators for a load point / replication
 *
 * @author Iallen
 */
public class TransmittersReceiversRegeneratorsUtilization extends Measurement {

    private double numberObservations;

    private double avgTxUtilization;
    private double avgRxUtilization;
    private double avgRegenUtilization;

    private HashMap<String, Double> avgTxUtilizationPerNode;
    private HashMap<String, Double> avgRxUtilizationPerNode;
    private HashMap<String, Double> avgRegenUtilizationPerNode;

    private HashMap<String, Integer> maxTxUtilizationPerNode;
    private HashMap<String, Integer> maxRxUtilizationPerNode;
    private HashMap<String, Integer> maxRegenUtilizationPerNode;

    /**
     * Creates a new instance of TransmitersReceiversUtilization
     * 
     * @param loadPoint int
     * @param replication int
     * @param mesh Mesh
     */
    public TransmittersReceiversRegeneratorsUtilization(int loadPoint, int replication) {
        super(loadPoint, replication);
        
        numberObservations = 0;
        avgTxUtilization = 0.0;
        avgRxUtilization = 0.0;
        avgRegenUtilization = 0.0;

        avgTxUtilizationPerNode = new HashMap<>();
        avgRxUtilizationPerNode = new HashMap<>();
        avgRegenUtilizationPerNode = new HashMap<>();

        maxTxUtilizationPerNode = new HashMap<>();
        maxRxUtilizationPerNode = new HashMap<>();
        maxRegenUtilizationPerNode = new HashMap<>();

		resultManager = new TransmittersReceiversRegeneratorsUtilizationResultManager();
    }

    /**
     * Adds a new observation to the use of transmitters, receivers and regenerators
     * 
     * @param cp ControlPlane
     * @param success boolean
     * @param request RequestForConnection
     */
    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
        ArrayList<Node> nodes = new ArrayList<>(cp.getMesh().getNodeList());

        if(nodes.isEmpty())
            System.out.print("ooo");

        for (Node node : nodes) {
            avgTxUtilization += node.getTxs().getTxUtilization();
            avgRxUtilization += node.getRxs().getRxUtilization();
            avgRegenUtilization += node.getRegenerators().getRegenUtilization();

            Double txUtNo = avgTxUtilizationPerNode.get(node.getName());
            if (txUtNo == null) txUtNo = 0.0;
            txUtNo += node.getTxs().getTxUtilization();
            avgTxUtilizationPerNode.put(node.getName(), txUtNo);

            Double rxUtNo = avgRxUtilizationPerNode.get(node.getName());
            if (rxUtNo == null) rxUtNo = 0.0;
            rxUtNo += node.getRxs().getRxUtilization();
            avgRxUtilizationPerNode.put(node.getName(), txUtNo);
            
            Double regenUtNo = avgRegenUtilizationPerNode.get(node.getName());
			if(regenUtNo == null) regenUtNo = 0.0;
			regenUtNo += node.getRegenerators().getRegenUtilization();
			avgRegenUtilizationPerNode.put(node.getName(), regenUtNo);

            Integer maxTxUtNo = maxTxUtilizationPerNode.get(node.getName());
            if (maxTxUtNo == null) maxTxUtNo = 0;
            if (node.getTxs().getTxUtilization() >= maxTxUtNo)
                maxTxUtilizationPerNode.put(node.getName(), node.getTxs().getTxUtilization());

            Integer maxRxUtNo = maxRxUtilizationPerNode.get(node.getName());
            if (maxRxUtNo == null) maxRxUtNo = 0;
            if (node.getRxs().getRxUtilization() >= maxRxUtNo)
                maxRxUtilizationPerNode.put(node.getName(), node.getRxs().getRxUtilization());
            
            Integer maxRegenUtNo = maxRegenUtilizationPerNode.get(node.getName());
			if(maxRegenUtNo == null) maxRegenUtNo = 0;
			if(node.getRegenerators().getRegenUtilization() >= maxRegenUtNo)
				maxRegenUtilizationPerNode.put(node.getName(), node.getRegenerators().getRegenUtilization());
        }
        
        numberObservations++;
    }

    @Override
    public String getFileName() {
        return SimulationRequest.Result.FILE_TRANSMITTERS_RECEIVERS_REGENERATORS_UTILIZATION;
    }

    /**
     * Returns the average transmitters utilization
     * 
     * @return double
     */
    public double getAvgTxUtilizationGen() {
        return avgTxUtilization / (double)numberObservations;
    }

    /**
     * Returns the average receivers utilization
     * 
     * @return double
     */
    public double getAvgRxUtilizationGen() {
        return avgRxUtilization / (double)numberObservations;
    }
    
    /**
     * Returns the average renerators utilization
     * 
     * @return double
     */
    public double getAvgRegenUtilizationGen (){
    	return avgRegenUtilization / (double)numberObservations;
    }

    /**
     * Returns the average transmitters utilization for the node reported as parameter
     * 
     * @param node String
     * @return double
     */
    public double getAvgTxUtilizationPerNode(String node) {
        return avgTxUtilizationPerNode.get(node) / (double)numberObservations;
    }

    /**
     * Returns the average receivers utilization for the node reported as parameter
     * 
     * @param node String
     * @return double
     */
    public double getAvgRxUtilizationPerNode(String node) {
        return avgRxUtilizationPerNode.get(node) / (double)numberObservations;
    }
    
    /**
     * Returns the average regenerators utilization for the node reported as parameter
     * 
     * @param node String
     * @return double
     */
    public double getAvgRegenUtilizationPerNode(String node) {
        return avgRegenUtilizationPerNode.get(node) / (double)numberObservations;
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
     * Returns the maximum regenerators utilization for the node reported as parameter
     * 
     * @param node String
     * @return int
     */
    public int getMaxRegenUtilizationPerNode(String node) {
        return maxRegenUtilizationPerNode.get(node);
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
