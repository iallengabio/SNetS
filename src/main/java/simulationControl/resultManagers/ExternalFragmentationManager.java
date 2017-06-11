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
public class ExternalFragmentationManager {
	
	private HashMap<Integer, HashMap<Integer, ExternalFragmentation>> fes; // Contains the external fragmentation metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * Creates a new instance of ExternalFragmentationManager
	 * 
	 * @param lfe List<List<ExternalFragmentation>>
	 */
	public ExternalFragmentationManager(List<List<ExternalFragmentation>> lfe){
		fes = new HashMap<>();
		
		for (List<ExternalFragmentation> loadPoint : lfe) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, ExternalFragmentation>  reps = new HashMap<>();
			fes.put(load, reps);
			
			for (ExternalFragmentation fe : loadPoint) {
				reps.put(fe.getReplication(), fe);
			}			
		}
		loadPoints = new ArrayList<>(fes.keySet());
		replications = new ArrayList<>(fes.values().iterator().next().keySet());
	} 
	
	/**
	 * Returns a string corresponding to the result file for external fragmentation
	 * 
	 * @return String
	 */
	public String result(){
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
				res.append(sep + fes.get(loadPoint).get(replic).getExternalFragVertical());
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
				res.append(sep + fes.get(loadPoint).get(replic).getExternalFragHorizontal());
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
			
			for (String link : fes.get(1).get(1).getLinkSet()) {
				String aux2 = aux + "<" + link + ">" + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + fes.get(loadPoint).get(replic).getExternalFragLink(link);
				}
				res.append(aux2 + "\n");
			}
			
		}
		return res.toString();
	}
	
}