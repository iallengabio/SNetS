package simulationControl.resultManagers;

import measurement.GroomingStatistics;
import measurement.Measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible for formatting the file with results of spectrum size statistics
 * 
 * @author Iallen
 */
public class GroomingStatisticsResultManager implements ResultManagerInterface {
	
	private HashMap<Integer, HashMap<Integer, GroomingStatistics>> gss; // Contains the grooming statistics for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		gss = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, GroomingStatistics>  reps = new HashMap<>();
			gss.put(load, reps);
			
			for (Measurement gs : loadPoint) {
				reps.put(gs.getReplication(), (GroomingStatistics) gs);
			}			
		}
		loadPoints = new ArrayList<>(gss.keySet());
		replications = new ArrayList<>(gss.values().iterator().next().keySet());
	} 
	
	/**
	 * Returns a string corresponding to the result file for spectrum size statistics
	 * 
	 * @return String
	 */
	public String result(List<List<Measurement>> llms){
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");

		res.append(resultReqByCircGeneral());
		res.append("\n\n");
		res.append(resultMaxReqByCircGeneral());
		res.append("\n\n");
		res.append(resultVirtualHopsGeneral());
		res.append("\n\n");
		res.append(resultMaxVirtualHopsGeneral());
		res.append("\n\n");
		return res.toString();
	}
	
	/**
	 * Format the result of rate of requests by circuit
	 *
	 * @return String
	 */
	private String resultReqByCircGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Rate of requests by circuit" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + gss.get(loadPoint).get(replic).getReqByCirc());
			}
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * Format the result of max requests by circuit
	 *
	 * @return String
	 */
	private String resultMaxReqByCircGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Maximum requests by circuit" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + gss.get(loadPoint).get(replic).getMaxReqByCirc());
			}
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * Format the result of mean virtual hops
	 *
	 * @return String
	 */
	private String resultVirtualHopsGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Virtual hops" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + gss.get(loadPoint).get(replic).getVirtualHops());
			}
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * Format the result of maximum virtual hops
	 *
	 * @return String
	 */
	private String resultMaxVirtualHopsGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Maximum virtual hops" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + gss.get(loadPoint).get(replic).getMaxVirtualHops());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
}
