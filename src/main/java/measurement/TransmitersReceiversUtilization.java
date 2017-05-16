package measurement;

import network.ControlPlane;
import network.Mesh;
import network.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Esta classe armazena as medidas referentes a utilização de transmissores e receptores para um ponto de carga/replicação
 *
 * @author Iallen
 */
public class TransmitersReceiversUtilization extends Measurement {

    private Mesh mesh;

    private double numObs;

    private double avgTxUtilization;
    private double avgRxUtilization;

    private HashMap<String, Double> avgTxUtilizationPerNode;
    private HashMap<String, Double> avgRxUtilizationPerNode;

    private HashMap<String, Integer> maxTxUtilizationPerNode;
    private HashMap<String, Integer> maxRxUtilizationPerNode;


    public TransmitersReceiversUtilization(int loadPoint, int replication, Mesh mesh) {
        super(loadPoint, replication);
        this.mesh = mesh;
        numObs = 0;
        avgTxUtilization = 0.0;
        avgRxUtilization = 0.0;

        avgTxUtilizationPerNode = new HashMap<>();
        avgRxUtilizationPerNode = new HashMap<>();

        maxTxUtilizationPerNode = new HashMap<>();
        maxRxUtilizationPerNode = new HashMap<>();

    }

    /**
     * adiciona uma nova observação de utilização de transmiters e receivers
     */
    public void addNewObservation() {
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
        numObs++;
    }

    public double getAvgTxUtilizationGen() {

        return avgTxUtilization / numObs;
    }

    public double getAvgRxUtilizationGen() {
        return avgRxUtilization / numObs;
    }

    public double getAvgTxUtilizationPerNode(String node) {
        return avgTxUtilizationPerNode.get(node) / numObs;
    }

    public double getAvgRxUtilizationPerNode(String node) {
        return avgRxUtilizationPerNode.get(node) / numObs;
    }

    public int getMaxTxUtilizationPerNode(String node) {
        return maxTxUtilizationPerNode.get(node);
    }

    public int getMaxRxUtilizationPerNode(String node) {
        return maxRxUtilizationPerNode.get(node);
    }

    public Set<String> getNodeNamesSet() {
        return avgTxUtilizationPerNode.keySet();
    }


}
