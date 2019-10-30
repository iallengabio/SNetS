package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.Measurement;
import measurement.ModulationUtilization;
import simulationControl.Util;

/**
 * This class is responsible for formatting the file with results of modulation utilization
 * 
 * @author Alexandre
 */
public class ModulationUtilizationResultManager implements ResultManagerInterface {

	private HashMap<Integer, HashMap<Integer, ModulationUtilization>> mus; // Contains the modulation utilization metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	private List<String> modulationList;
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		mus = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, ModulationUtilization>  reps = new HashMap<>();
			mus.put(load, reps);
			
			for (Measurement mu : loadPoint) {
				reps.put(mu.getReplication(), (ModulationUtilization)mu);
				
				if(modulationList == null){
					modulationList = ((ModulationUtilization)mu).getModulationList();
				}
			}
		}
		loadPoints = new ArrayList<>(mus.keySet());
		replications = new ArrayList<>(mus.values().iterator().next().keySet());
	} 
	
	/**
	 * Returns a string corresponding to the result file for modulation utilization
	 * 
	 * @return String
	 */
	@Override
	public String result(List<List<Measurement>> llms) {
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "Modulation" + sep + "Bandwidth" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultPercentageCircuitsPerModulation());
		res.append("\n\n");
		res.append(resultPercentageCircuitsPerModuPerBw());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * Returns the percentage of circuits per modulation
	 * 
	 * @return String
	 */
	private String resultPercentageCircuitsPerModulation() {
		StringBuilder res = new StringBuilder();
		for(Integer loadPoint : loadPoints){
			String aux = "Percentage of cicuits per modulation" + sep + loadPoint;
			
			for (String modName : modulationList) {
				String aux2 = aux + sep + modName + sep + "all" + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + mus.get(loadPoint).get(replic).getPercentageCircuitsPerModulation(modName);
				}
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the percentage of circuits per modulation and per bandwidth
	 * 
	 * @return String
	 */
	private String resultPercentageCircuitsPerModuPerBw() {
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Percentage of cicuits per modulation and bandwidth" + sep + loadPoint;
			
			for(String modName : modulationList){
				String aux2 = aux + sep + modName;
				
				for (Double bandwidth : mus.get(0).get(0).getUtil().bandwidths) {
					String aux3 = aux2 + sep + (bandwidth/1000000000.0) + sep + " ";
					
					for (Integer replic : replications) {
						aux3 = aux3 + sep + mus.get(loadPoint).get(replic).getPercentageCircuitPerModPerBw(modName, bandwidth);
					}
					res.append(aux3 + "\n");
				}
			}
		}
		return res.toString();
	}

}
