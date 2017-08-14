package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.Measurement;
import measurement.MetricsOfEnergyConsumption;
import network.Pair;
import simulationControl.Util;

/**
 * This class is responsible for formatting the file with results of energy consumption
 * 
 * @author Alexandre
 */
public class MetricsOfEnergyConsumptionResultManager implements ResultManagerInterface {

	private HashMap<Integer, HashMap<Integer, MetricsOfEnergyConsumption>> mec; // Contains the energy consumption metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private List<Pair> pairs;
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
		pairs = new ArrayList<>(Util.pairs);
	}
	
	/**
	 * Returns a string corresponding to the result file for blocking probabilities
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
		
		res.append(ressultAveragePcGeneral());
		res.append("\n\n");
		res.append(resultAveragePcEstablished());
		res.append("\n\n");
		res.append(resultPcPair());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * Returns the general power consumption
	 * 
	 * @return String
	 */
	private String ressultAveragePcGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("General power consumption" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + mec.get(loadPoint).get(replic).getGeneralPc());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the average power consumption for established circuits
	 * 
	 * @return String
	 */
	private String resultAveragePcEstablished(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Average power consumption established" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + mec.get(loadPoint).get(replic).getAveragePcEstablished());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the power consumption per pair
	 * 
	 * @return String
	 */
	private String resultPcPair(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Average power consumption per pair" + sep + loadPoint + sep + "all";
			
			for (Pair pair : this.pairs) {
				String aux2 = aux + sep + pair.getSource().getName() + sep + pair.getDestination().getName() + sep + " ";
				for (Integer replic : replications) {
					aux2 = aux2 + sep + mec.get(loadPoint).get(replic).getAveragePcPair(pair);
				}
				res.append(aux2 + "\n");		
			}
		}
		return res.toString();
	}

}
