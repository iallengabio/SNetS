package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.Measurement;
import measurement.SpectrumUtilization;

/**
 * This class is responsible for formatting the file with results of spectrum utilization
 * 
 * @author Iallen
 */
public class SpectrumUtilizationResultManager implements ResultManagerInterface {
	
	private HashMap<Integer, HashMap<Integer, SpectrumUtilization>> sus; // Contains the spectrum utilization metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	private Integer slotsNumber;
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		sus = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, SpectrumUtilization>  reps = new HashMap<>();
			sus.put(load, reps);
			
			for (Measurement su : loadPoint) {
				reps.put(su.getReplication(), (SpectrumUtilization)su);
				
				if(slotsNumber == null){
					slotsNumber = ((SpectrumUtilization)su).getMaxSlotsByLinks();
				}
			}			
		}
		loadPoints = new ArrayList<>(sus.keySet());
		replications = new ArrayList<>(sus.values().iterator().next().keySet());
	} 
	
	/**
	 * Returns a string corresponding to the result file for spectrum utilization
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
		
		res.append(resultUtilizationGeneral());
		res.append("\n\n");
		res.append(resultUtilizationPerLink());
		res.append("\n\n");
		
		res.append(resultUtilizationPerSlot());
		
		return res.toString();
	}
	
	/**
	 * Returns the spectrum utilization general
	 * 
	 * @return String
	 */
	private String resultUtilizationGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Utilization" + sep + loadPoint + sep + "all" + sep + " - " + sep + " - " + sep + " "; // "all"+sep+" ";
			for (Integer replic : replications) {
					aux = aux + sep + sus.get(loadPoint).get(replic).getUtilizationGen();
			}
			res.append(aux + "\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the spectrum utilization per link
	 * 
	 * @return String
	 */
	private String resultUtilizationPerLink(){
		
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Utilization Per Link" + sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String link : sus.get(0).get(0).getLinkSet()) {
				String aux2 = aux + "<"+link+">" + sep + " - " + sep + " - " + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + sus.get(loadPoint).get(replic).getUtilizationPerLink(link);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the spectrum utilization per slot
	 * 
	 * @return String
	 */
	private String resultUtilizationPerSlot(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Utilization Per Slot" + sep + loadPoint + sep + "all" + sep + " - " + sep;
			for(int i = 1; i <= slotsNumber; i++){
				String aux2 = aux + i + sep + " ";
				for (Integer rep : replications) {
					aux2 = aux2 + sep + sus.get(loadPoint).get(rep).getUtilizationPerSlot(i);
				}
				res.append(aux2 + "\n");
			}
			
		}
		return res.toString();
	}
	
}
