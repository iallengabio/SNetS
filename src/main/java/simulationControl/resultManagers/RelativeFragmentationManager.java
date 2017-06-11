package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.RelativeFragmentation;

/**
 * This class is responsible for formatting the file with results of relative fragmentation
 * 
 * @author Iallen
 */
public class RelativeFragmentationManager {
	
	private HashMap<Integer, HashMap<Integer, RelativeFragmentation>> frs; // Contains the relative fragmentation metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * Creates a new instance of RelativeFragmentationManager
	 * 
	 * @param lfr List<List<RelativeFragmentation>>
	 */
	public RelativeFragmentationManager(List<List<RelativeFragmentation>> lfr){
		frs = new HashMap<>();
		
		for (List<RelativeFragmentation> loadPoint : lfr) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, RelativeFragmentation>  reps = new HashMap<>();
			frs.put(load, reps);
			
			for (RelativeFragmentation fr : loadPoint) {
				reps.put(fr.getReplication(), fr);
			}			
		}
		loadPoints = new ArrayList<>(frs.keySet());
		replications = new ArrayList<>(frs.values().iterator().next().keySet());
		
	} 
	
	/**
	 * Returns a string corresponding to the result file for relative fragmentation
	 * 
	 * @return String
	 */
	public String result(){
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
			for (Integer c : this.frs.get(0).get(0).getCList()) {
				String aux = "Relative Fragmentation per spectrum size (c)" + sep + loadPoint + sep + "all" + sep + c + sep + " ";
				for (Integer replication : replications) {
					aux = aux + sep + frs.get(loadPoint).get(replication).getAverageRelativeFragmentation(c);
				}
				res.append(aux + "\n");
			}
		}
		return res.toString();
	}
	
}
