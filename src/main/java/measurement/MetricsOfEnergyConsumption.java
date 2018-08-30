package measurement;

import network.Circuit;
import network.ControlPlane;
import network.EnergyConsumption;
import request.RequestForConnection;
import simulationControl.resultManagers.MetricsOfEnergyConsumptionResultManager;


/**
 * This class is responsible for metrics of energy consumption
 * The metric represented by this class is associated with a load point and a replication
 * 
 * @author Alexandre
 */
public class MetricsOfEnergyConsumption  extends Measurement {

	public final static String SEP = "-";

	private double sumGeneralPc;
	private double obsGeneralPc;
	
	private double totalDataTransmitted;    
    private double totalNetworkOperationTime;
    private double totalEnergyTransponders;
    private double totalEnergyOXCs;
    private double totalEnergyAmplifiers;

    /**
     * Creates a new instance of MetricsOfEnergyConsumption
     * 
     * @param loadPoint int
     * @param rep int
     */
	public MetricsOfEnergyConsumption(int loadPoint, int rep) {
		super(loadPoint, rep);
		
		sumGeneralPc = 0.0;
		obsGeneralPc = 0.0;
		
		totalDataTransmitted = 0.0;
		totalNetworkOperationTime = 0.0;
		totalEnergyTransponders = 0.0;
		totalEnergyOXCs = 0.0;
		totalEnergyAmplifiers = 0.0;
		
		fileName = "_EnergyConsumption.csv";
		resultManager = new MetricsOfEnergyConsumptionResultManager();
	}

	/**
	 * Adds a new observation of block or not a request
	 *
	 * @param cp - ControlPlane
	 * @param success - boolean
	 * @param request - RequestForConnection
	 */
	public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
		double finalizeTime = request.getTimeOfFinalizeHours() * 3600.0; // Converting to seconds
		if(finalizeTime > totalNetworkOperationTime){
			totalNetworkOperationTime = finalizeTime; // Seconds
		}
		
		if(totalEnergyOXCs == 0.0){
			totalEnergyOXCs = EnergyConsumption.computeOxcsPowerConsumption(cp.getMesh().getNodeList()); // Watt
			totalEnergyAmplifiers = EnergyConsumption.computeLinksPowerConsumption(cp.getMesh().getLinkList(), cp); // Watt
		}
		
		if(success){
			Double duracao = request.getTimeOfFinalizeHours() - request.getTimeOfRequestHours();
			duracao *= 3600.0; // Converting to seconds
			
			totalDataTransmitted += duracao * request.getRequiredBandwidth(); // Total data transmitted by the flow (bits)
			totalEnergyTransponders += computeEnergyConsumptionTransponders(request, duracao, cp); // Total energy consumed by the flow (Joule = Watt * second)
		}
		
		computeGeneralPowerConsumption(cp); // Calculates the total power consumption of the network
	}
	
	/**
	 * Computes the power consumption of the network
	 * 
	 * @param cp ControlPlane
	 */
	public void computeGeneralPowerConsumption(ControlPlane cp){
		sumGeneralPc += EnergyConsumption.computeNetworkPowerConsumption(cp); // Watt
		obsGeneralPc++;
	}
	
	/**
	 * Returns the average power consumption
	 * 
	 * @return double Watt
	 */
	public double getAveragePowerConsumption(){
		return (sumGeneralPc / obsGeneralPc); // Watt
	}
	
	/**
	 * Returns the energy consumption of the transponders
	 * 
	 * @param request RequestForConnection
	 * @param duration double
	 * @param cp ControlPlane
	 * @return double Joule
	 */
	public double computeEnergyConsumptionTransponders(RequestForConnection request, double duration, ControlPlane cp){
		double PCtrans = 0.0;
		for(Circuit circuit : request.getCircuits()){
			PCtrans += 2.0 * EnergyConsumption.computeTransponderPowerConsumption(circuit); // Power consumption of the transmitter and receiver
		}
		double flowPCtrans = duration * PCtrans; // Flow energy consumption
		return flowPCtrans;
	}
	
	/**
	 * Return the energy efficiency
	 * 
	 * @return double bits/Joule
	 */
	public double getEnergyEfficiencyGeneral(){
		double totalEcOXCsAndAmps = (totalEnergyOXCs + totalEnergyAmplifiers) * totalNetworkOperationTime; // Joule
		double ef = totalDataTransmitted / (totalEnergyTransponders + totalEcOXCsAndAmps); // bits/Joule
		return ef;
	}
	
	/**
	 * Returns the total data transmitted
	 * 
	 * @return double bits
	 */
	public double getTotalDataTransmitted(){
		return totalDataTransmitted;
	}
	
	/**
	 * Returns the energy consumption of the transponders
	 * 
	 * @return double Joule
	 */
	public double getTotalEnergyConsumptionTransponders(){
		return totalEnergyTransponders;
	}
	
	/**
	 * Returns the energy consumption of the OXCs
	 * 
	 * @return double Joule
	 */
	public double getTotalEnergyConsumptionOXCs(){
		return (totalEnergyOXCs * totalNetworkOperationTime);
	}
	
	/**
	 * Returns the energy consumption of the amplifiers
	 * 
	 * @return double Joule
	 */
	public double getTotalEnergyConsumptionAmplifiers(){
		return (totalEnergyAmplifiers * totalNetworkOperationTime);
	}
	
	/**
	 * Returns the energy consumption of the network
	 * 
	 * @return double Joule
	 */
	public double getTotalEnergyConsumption(){
		return (totalEnergyTransponders + ((totalEnergyOXCs + totalEnergyAmplifiers) * totalNetworkOperationTime));
	}
}
