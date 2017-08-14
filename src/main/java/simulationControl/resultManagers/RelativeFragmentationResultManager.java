package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.Measurement;
import measurement.RelativeFragmentation;

/**
 * This class is responsible for formatting the file with results of relative fragmentation
 * 
 * @author Iallen
 */
public class RelativeFragmentationResultManager implements ResultManagerInterface {
	
	private HashMap<Integer, HashMap<Integer, RelativeFragmentation>> rfs; // Contains the relative fragmentation metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		rfs = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, RelativeFragmentation>  reps = new HashMap<>();
			rfs.put(load, reps);
			
			for (Measurement rf : loadPoint) {
				reps.put(rf.getReplication(), (RelativeFragmentation)rf);
			}			
		}
		loadPoints = new ArrayList<>(rfs.keySet());
		replications = new ArrayList<>(rfs.values().iterator().next().keySet());
		
	} 
	
	/**
	 * Returns a string corresponding to the result file for relative fragmentation
	 * 
	 * @return String
	 */
	public String result(List<List<Measurement>> llms){
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "link" + sep + "spectrum size (c)" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultGeneral());
		res.append("\n\n");
		
		return res.toString();
	}

	/**
	 * Returns the relative fragmentation per spectrum size (c)
	 * 
	 * @return String
	 */
	private String resultGeneral() {
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			for (Integer c : this.rfs.get(0).get(0).getCList()) {
				String aux = "Relative fragmentation per spectrum size (c)" + sep + loadPoint + sep + "all" + sep + c + sep + " ";
				for (Integer replication : replications) {
					aux = aux + sep + rfs.get(loadPoint).get(replication).getAverageRelativeFragmentation(c);
				}
				res.append(aux + "\n");
			}
		}
		return res.toString();
	}
	
}
