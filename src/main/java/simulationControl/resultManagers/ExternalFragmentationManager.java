package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.ExternalFragmentation;
import measurement.Measurement;

/**
 * This class is responsible for formatting the file with results of external fragmentation
 * 
 * @author Iallen
 */
public class ExternalFragmentationManager implements ResultManagerInterface {
	
	private HashMap<Integer, HashMap<Integer, ExternalFragmentation>> efs; // Contains the external fragmentation metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		efs = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, ExternalFragmentation>  reps = new HashMap<>();
			efs.put(load, reps);
			
			for (Measurement ef : loadPoint) {
				reps.put(ef.getReplication(), (ExternalFragmentation)ef);
			}			
		}
		loadPoints = new ArrayList<>(efs.keySet());
		replications = new ArrayList<>(efs.values().iterator().next().keySet());
	} 
	
	/**
	 * Returns a string corresponding to the result file for external fragmentation
	 * 
	 * @return String
	 */
	public String result(List<List<Measurement>> llms){
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "link" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultGeneralVertical());
		res.append("\n\n");
		res.append(resultGeneralHorizontal());
		res.append("\n\n");
		res.append(resultPerLink());
		res.append("\n\n");
		
		return res.toString();
	}

	/**
	 * Returns the external fragmentation (vertical)
	 * 
	 * @return String
	 */
	private String resultGeneralVertical() {
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("External Fragmentation (Vertical)" + sep + loadPoint + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + efs.get(loadPoint).get(replic).getExternalFragVertical());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the external fragmentation (horizontal)
	 * 
	 * @return String
	 */
	private String resultGeneralHorizontal() {
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("External Fragmentation (Horizontal)" + sep + loadPoint + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + efs.get(loadPoint).get(replic).getExternalFragHorizontal());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the external fragmentation per link
	 * 
	 * @return String
	 */
	private String resultPerLink(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "External Fragmentation Per Link" + sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String link : efs.get(0).get(0).getLinkSet()) {
				String aux2 = aux + "<" + link + ">" + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + efs.get(loadPoint).get(replic).getExternalFragLink(link);
				}
				res.append(aux2 + "\n");
			}
			
		}
		return res.toString();
	}
	
}