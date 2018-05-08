package measurement;

import java.util.HashMap;
import java.util.TreeSet;

import network.Circuit;
import network.ControlPlane;
import network.Pair;
import request.RequestForConnection;
import simulationControl.resultManagers.MetricsOfEnergyConsumptionResultManager;

public class MetricsOfEnergyConsumption  extends Measurement {

	public final static String SEP = "-";
	
	private double sumGeneralPc;
	private double obsGeneralPc;
	private double sumPcEstablished;
    private double obsPcEstablished;
    
    // Per pair
  	private HashMap<String, Double> sumPcPair;
  	private HashMap<String, Integer> numPcPair;

	public MetricsOfEnergyConsumption(int loadPoint, int rep) {
		super(loadPoint, rep);
		
		this.sumGeneralPc = 0.0;
		this.obsGeneralPc = 0.0;
		this.sumPcEstablished = 0.0;
		this.obsPcEstablished = 0.0;
		
		this.sumPcPair = new HashMap<String, Double>();
		this.numPcPair = new HashMap<String, Integer>();
		
		fileName = "_EnergyConsumption.csv";
		resultManager = new MetricsOfEnergyConsumptionResultManager();
	}

	/**
	 * Adds a new observation of block or not a request
	 * 
	 * @param cp - ControlPlane
	 * @param success - boolean
	 * @param request - Request
	 */
	public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
		if(success){


			// It is only possible to calculate the energy consumed from the circuits that were established
			double pc = 0;
			for(Circuit circuit : request.getCircuits()){
				pc += cp.getPowerConsumption(circuit);
			}
			
			// Per pair
			StringBuilder sbPair = new StringBuilder();
			sbPair.append(request.getPair().getSource().getName());
			sbPair.append(SEP);
			sbPair.append(request.getPair().getDestination().getName());
			String pairName = sbPair.toString();
			
			Double pcPair = this.sumPcPair.get(pairName);
			if(pcPair == null)
				pcPair = pc;
			else
				pcPair += pc;
			this.sumPcPair.put(pairName, pcPair);
			
			Integer num = this.numPcPair.get(pairName);
			if(num == null)
				num = 0;
			this.numPcPair.put(pairName, num+1);
			
			sumPcEstablished += pc;
			obsPcEstablished++;
		}
		
		// Calculates the total power consumption of the network
		computeGeneralPowerConsumption(cp);
	}
	
	/**
	 * Computes the total power consumption of the network at a given moment
	 * 
	 * @param cp ControlPlane
	 */
	public void computeGeneralPowerConsumption(ControlPlane cp){
		double sumPc = 0.0;
		
		TreeSet<Circuit> circuitList = cp.getConnections();
        for(Circuit circuit : circuitList){
        	sumPc += cp.getPowerConsumption(circuit);
        }
        
        sumGeneralPc += sumPc;
        obsGeneralPc++;
    }
	
	/**
	 * Returns the average power consumption of the network
	 * 
	 * @return double
	 */
	public double getGeneralPc(){
        return (this.sumGeneralPc / this.obsGeneralPc);
    }
	
	/**
	 * Returns the average power consumption of all circuits that were established
	 * 
	 * @return double
	 */
	public double getAveragePcEstablished(){
		return (this.sumPcEstablished / this.obsPcEstablished);
	}
	
	/**
	 * Returns the average power consumption for a given pair
	 * 
	 * @param p Pair
	 * @return double
	 */
	public double getAveragePcPair(Pair p){
		double res = 0.0;
		
		StringBuilder sbPair = new StringBuilder();
		sbPair.append(p.getSource().getName());
		sbPair.append(SEP);
		sbPair.append(p.getDestination().getName());
		String pairName =  sbPair.toString();
		
		Integer num = this.numPcPair.get(pairName);
		if(num == null)
			num = 0;
		
		Double pc = this.sumPcPair.get(pairName);
		if(pc == null)
			pc = 0.0;
		
		if(num > 0)
			res = ((double)pc / (double)num);		
		
		return res;
	}
}
