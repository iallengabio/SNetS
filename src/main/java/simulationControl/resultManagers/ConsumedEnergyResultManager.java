package simulationControl.resultManagers;

import measurement.ConsumedEnergy;
import measurement.Measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConsumedEnergyResultManager implements ResultManagerInterface {
    private final static String sep = ",";
    private HashMap<Integer, HashMap<Integer, ConsumedEnergy>> ces; // Contains the spectrum utilization metric for all load points and replications
    private List<Integer> loadPoints;
    private List<Integer> replications;

    @Override
    public String result(List<List<Measurement>> llms) {
        config(llms);
        StringBuilder res = new StringBuilder();
        res.append("Metrics" + sep + "LoadPoint" + sep + " ");
        for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
            res.append(sep + "rep" + rep);
        }
        res.append("\n");

        for (Integer loadPoint : loadPoints) {
            String aux = "Total consumed energy" + sep + loadPoint + sep + " "; // "all"+sep+" ";
            for (Integer replic : replications) {
                aux = aux + sep + ces.get(loadPoint).get(replic).getTotalConsumedEnergy();
            }
            res.append(aux + "\n");
        }

        return res.toString();
    }

    /**
     * This method organizes the data by load point and replication.
     *
     * @param llms List<List<Measurement>>
     */
    public void config(List<List<Measurement>> llms){
        ces = new HashMap<>();

        for (List<Measurement> loadPoint : llms) {
            int load = loadPoint.get(0).getLoadPoint();
            HashMap<Integer, ConsumedEnergy>  reps = new HashMap<>();
            ces.put(load, reps);

            for (Measurement su : loadPoint) {
                reps.put(su.getReplication(), (ConsumedEnergy) su);
            }
        }
        loadPoints = new ArrayList<>(ces.keySet());
        replications = new ArrayList<>(ces.values().iterator().next().keySet());
    }
}
