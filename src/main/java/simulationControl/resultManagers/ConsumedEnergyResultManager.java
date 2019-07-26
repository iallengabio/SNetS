package simulationControl.resultManagers;

import measurement.ConsumedEnergy;
import measurement.Measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible for formatting the file with results of consumed energy
 * 
 * @author Iallen, Alexandre
 *
 */
public class ConsumedEnergyResultManager implements ResultManagerInterface {
    private final static String sep = ",";
    private HashMap<Integer, HashMap<Integer, ConsumedEnergy>> ces; // Contains the spectrum utilization metric for all load points and replications
    private List<Integer> loadPoints;
    private List<Integer> replications;

    /**
     * This method organizes the data by load point and replication.
     *
     * @param llms List<List<Measurement>>
     */
    public void config(List<List<Measurement>> llms){
        ces = new HashMap<>();

        for (List<Measurement> loadPoint : llms) {
            int load = loadPoint.get(0).getLoadPoint();
            HashMap<Integer, ConsumedEnergy>  reps = new HashMap<>();
            ces.put(load, reps);

            for (Measurement su : loadPoint) {
                reps.put(su.getReplication(), (ConsumedEnergy) su);
            }
        }
        loadPoints = new ArrayList<>(ces.keySet());
        replications = new ArrayList<>(ces.values().iterator().next().keySet());
    }
    
    @Override
    public String result(List<List<Measurement>> llms) {
        config(llms);
        
        StringBuilder res = new StringBuilder();
        res.append("Metrics" + sep + "LoadPoint" + sep + " ");
        
        for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
            res.append(sep + "rep" + rep);
        }
        res.append("\n");
        
        res.append(resultTotalConsumedEnergy());
		res.append("\n\n");
		
		res.append(ressultTotalPowerConsumption());
		res.append("\n\n");
		
		res.append(ressultEnergyEfficiency());
		res.append("\n\n");
		
		res.append(resultTotalDataTransmitted());
		res.append("\n\n");
		
		res.append(resultTotalConsumedEnergyTransponders());
		res.append("\n\n");
		
		res.append(resultTotalConsumedEnergyOXCs());
		res.append("\n\n");
		
		res.append(resultTotalConsumedEnergyAmplifiers());
		res.append("\n\n");
		
        return res.toString();
    }

    /**
	 * Returns the total energy consumption of the network
	 * 
	 * @return String
	 */
	private String resultTotalConsumedEnergy(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Total consumed energy (Joule)" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + ces.get(loadPoint).get(replic).getTotalConsumedEnergy());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the average power consumption
	 * 
	 * @return String
	 */
	private String ressultTotalPowerConsumption(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Average power consumption (Watt)" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + ces.get(loadPoint).get(replic).getTotalPowerConsumption());
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
	private String resultTotalConsumedEnergyTransponders(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Total energy consumption by transponders (Joule)" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + ces.get(loadPoint).get(replic).getTotalConsumedEnergyTransponders());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the energy consumption of the OXCs
	 * 
	 * @return String
	 */
	private String resultTotalConsumedEnergyOXCs(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Total energy consumption by OXCs (Joule)" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + ces.get(loadPoint).get(replic).getTotalConsumedEnergyOXCs());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the energy consumption of the amplifiers
	 * 
	 * @return String
	 */
	private String resultTotalConsumedEnergyAmplifiers(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Total energy consumption by Amplifiers (Joule)" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + ces.get(loadPoint).get(replic).getTotalConsumedEnergyAmplifiers());
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
			res.append("Energy efficiency (bits/Joule)" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + ces.get(loadPoint).get(replic).getEnergyEfficiency());
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
			res.append("Total data transmitted (bits)" + sep + loadPoint + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + ces.get(loadPoint).get(replic).getTotalDataTransmitted());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
}
