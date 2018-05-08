package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.Measurement;
import measurement.SpectrumSizeStatistics;

/**
 * This class is responsible for formatting the file with results of spectrum size statistics
 * 
 * @author Iallen
 */
public class SpectrumSizeStatisticsResultManager implements ResultManagerInterface {
	
	private HashMap<Integer, HashMap<Integer, SpectrumSizeStatistics>> ssss; // Contains the spectrum size statistics metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		ssss = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, SpectrumSizeStatistics>  reps = new HashMap<>();
			ssss.put(load, reps);
			
			for (Measurement sss : loadPoint) {
				reps.put(sss.getReplication(), (SpectrumSizeStatistics)sss);
			}			
		}
		loadPoints = new ArrayList<>(ssss.keySet());
		replications = new ArrayList<>(ssss.values().iterator().next().keySet());
	} 
	
	/**
	 * Returns a string corresponding to the result file for spectrum size statistics
	 * 
	 * @return String
	 */
	public String result(List<List<Measurement>> llms){
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "Link" + sep + "Number of slots" + sep + "Slot" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultSpectrumBandwidthGeneral());
		res.append("\n\n");
		res.append(resultSpectrumBandwidthPerLink());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * Format the result of the portion of requests that each spectrum width requires
	 * 
	 * @return String
	 */
	private String resultSpectrumBandwidthGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Requisitons per number of slots" + sep + loadPoint + sep + "all" + sep; // "all"+sep+" ";
			for (Integer numSlots : ssss.get(0).get(0).getNumberOfSlotsList()) {
				String aux2 = aux + numSlots + sep + "-" + sep + " ";
				for (Integer replic : replications) {
					aux2 = aux2 + sep + ssss.get(loadPoint).get(replic).getPercentageReq(numSlots);
				}			
				res.append(aux2 + "\n");
			}		
		}
		return res.toString();
	}
	
	/**
	 * Formats the result of the portion of requests that each spectrum bandwidth per link requires
	 * 
	 * @return String
	 */
	private String resultSpectrumBandwidthPerLink(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Requisitons per number of slots per link" + sep + loadPoint + sep; // "all"+sep+" ";
			for (String link : ssss.get(0).get(0).getLinkSet()) {
				String aux2 = aux + "<"+link+">" + sep;
				for (Integer numSlots : ssss.get(0).get(0).getNumberOfSlotsPerLink(link)) {
					String aux3 = aux2 + numSlots + sep + "-" + sep + " ";
					for (Integer replic : replications) {
						aux3 = aux3 + sep + ssss.get(loadPoint).get(replic).getPercentageReq(link, numSlots);
					}	
					res.append(aux3 + "\n");
				}					
			}
		}
		return res.toString();	
	}
	
}
