package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.Measurement;
import measurement.MetricsOfEnergyConsumption;

/**
 * This class is responsible for formatting the file with results of energy consumption
 * 
 * @author Alexandre
 */
public class MetricsOfEnergyConsumptionResultManager implements ResultManagerInterface {

	private HashMap<Integer, HashMap<Integer, MetricsOfEnergyConsumption>> mec; // Contains the energy consumption metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		mec = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, MetricsOfEnergyConsumption>  reps = new HashMap<>();
			mec.put(load, reps);
			
			for (Measurement bp : loadPoint) {
				reps.put(bp.getReplication(), (MetricsOfEnergyConsumption)bp);
			}			
		}
		loadPoints = new ArrayList<>(mec.keySet());
		replications = new ArrayList<>(mec.values().iterator().next().keySet());
	}
	
	/**
	 * Returns a string corresponding to the result file for energy consumption
	 * 
	 * @return String
	 */
	public String result(List<List<Measurement>> llms){
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "Bandwidth" + sep + "src" + sep + "dest" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(ressultAveragePowerConsumption());
		res.append("\n\n");
		
		res.append(ressultEnergyEfficiency());
		res.append("\n\n");
		
		res.append(resultTotalDataTransmitted());
		res.append("\n\n");
		
		res.append(resultTotalEnergyTransporders());
		res.append("\n\n");
		
		res.append(resultTotalEnergyOXCsAndAmplifiers());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * Returns the average power consumption
	 * 
	 * @return String
	 */
	private String ressultAveragePowerConsumption(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Average power consumption (Watt)" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + mec.get(loadPoint).get(replic).getAveragePowerConsumption());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the energy efficiency
	 * 
	 * @return String
	 */
	private String ressultEnergyEfficiency(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Energy efficiency (bits/Joule)" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + mec.get(loadPoint).get(replic).getEnergyEfficiencyGeneral());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the total data transmitted
	 * 
	 * @return String
	 */
	private String resultTotalDataTransmitted(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Total data transmitted (bits)" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + mec.get(loadPoint).get(replic).getTotalDataTransmitted());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the energy consumption of the transponders 
	 * 
	 * @return String
	 */
	private String resultTotalEnergyTransporders(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Total energy consumption by transponders (Joule)" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + mec.get(loadPoint).get(replic).getTotalEnergyConsumptionTransponders());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the energy consumption of the OXCs and amplifiers
	 * 
	 * @return String
	 */
	private String resultTotalEnergyOXCsAndAmplifiers(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Total energy consumption by OXCs and Amplifiers (Joule)" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + mec.get(loadPoint).get(replic).getTotalEnergyConsumptionOxcsAndAmps());
			}
			res.append("\n");
		}
		return res.toString();
	}
}
