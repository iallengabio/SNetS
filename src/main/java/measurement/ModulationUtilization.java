package measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import grmlsa.modulation.Modulation;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;
import simulationControl.Util;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.ModulationUtilizationResultManager;

/**
 * This class stored the metrics related to the use of modulation.
 * 
 * @author Alexandre
 */
public class ModulationUtilization extends Measurement {

	private int numObservations;
	
	private HashMap<String, Integer> numCircuitsPerMod;
	private HashMap<String, HashMap<Double, Integer>> numCircuitsPerModPerBw;
	
	private List<String> modulationList;
	private Util util;
	
	public ModulationUtilization(int loadPoint, int replication, Util util) {
		super(loadPoint, replication);
		this.util = util;
		this.numObservations = 0;
		
		this.numCircuitsPerMod = new HashMap<>();
		this.numCircuitsPerModPerBw = new HashMap<>();

		resultManager = new ModulationUtilizationResultManager();
	}
	
	/**
    * Adds a new usage observation of modulation utilization
    *
    * @param cp ControlPlane
    * @param success boolean
    * @param request RequestForConnection
    */
   public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
	   if(modulationList == null){
		   modulationList = new ArrayList<String>();
		   List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
		   
		   for(int i = 0; i < avaliableModulations.size(); i++){
			   modulationList.add(avaliableModulations.get(i).getName());
		   }
	   }
	   
	   if(success){
	   	for(Circuit circuit : request.getCircuits()) {
			numObservations++;

			double bandwidth = circuit.getRequiredBandwidth();
			List<Modulation> modList = cp.getModulationsUsedByCircuit(circuit);

			for (int i = 0; i < modList.size(); i++) {
				String modName = modList.get(i).getName();

				// Number of circuits per modulation
				Integer numCirc = numCircuitsPerMod.get(modName);
				if (numCirc == null) numCirc = 0;
				numCircuitsPerMod.put(modName, numCirc + 1);

				// Number of circuits per modulation and bandwidth
				HashMap<Double, Integer> numCircBw = numCircuitsPerModPerBw.get(modName);
				if (numCircBw == null) {
					numCircBw = new HashMap<>();
					numCircuitsPerModPerBw.put(modName, numCircBw);
				}
				numCirc = numCircBw.get(bandwidth);
				if (numCirc == null) numCirc = 0;
				numCircBw.put(bandwidth, numCirc + 1);
			}
		}
		}
	}

	@Override
	public String getFileName() {
		return SimulationRequest.Result.FILE_MODULATION_UTILIZATION;
	}

	/**
    * Returns the percentage of circuits per modulation
    *
    * @return double
    */
	public double getPercentageCircuitsPerModulation(String modName){
		Integer numCirc = numCircuitsPerMod.get(modName);
		if(numCirc == null) numCirc = 0;
		return (double)numCirc / (double)numObservations;
	}
	
	/**
	 * Returns the percentage of circuits per modulation e per bandwidth
	 * 
	 * @param modName String
	 * @param bandwidth double
	 * @return double
	 */
	public double getPercentageCircuitPerModPerBw(String modName, double bandwidth){
		Integer numCirc = 0;
		HashMap<Double, Integer> numCircBw = numCircuitsPerModPerBw.get(modName);
		if(numCircBw != null && numCircBw.get(bandwidth) != null){
			numCirc = numCircBw.get(bandwidth);
		}
		return (double)numCirc / (double)numObservations;
	}
	
	/**
	 * Returns the modulation list
	 * 
	 * @return List<String>
	 */
	public List<String> getModulationList(){
		return modulationList;
	}

	public Util getUtil() {
		return util;
	}
}
