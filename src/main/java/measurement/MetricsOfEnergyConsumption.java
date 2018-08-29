package measurement;

import network.Circuit;
import network.ControlPlane;
import network.EnergyConsumption;
import network.Mesh;
import request.RequestForConnection;
import simulationControl.resultManagers.MetricsOfEnergyConsumptionResultManager;

public class MetricsOfEnergyConsumption  extends Measurement {

	public final static String SEP = "-";

	private double sumGeneralPc;
	private double obsGeneralPc;
	
	private double sumDataTransmitted;    
    private double tempoDeSimulacao;
    private double energyOXCsAndAmps;
    private double totalEnergyTransponders;

	public MetricsOfEnergyConsumption(int loadPoint, int rep) {
		super(loadPoint, rep);
		
		sumGeneralPc = 0.0;
		obsGeneralPc = 0.0;
		
		sumDataTransmitted = 0.0;
		tempoDeSimulacao = 0.0;
		energyOXCsAndAmps = 0.0;
		totalEnergyTransponders = 0.0;
		
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
		double tempoDeFinalizacao = request.getTimeOfFinalizeHours() * 3600.0; // Converting to seconds
		if(tempoDeFinalizacao > tempoDeSimulacao){
			tempoDeSimulacao = tempoDeFinalizacao; // Seconds
		}
		
		if(energyOXCsAndAmps == 0.0){
			double PcOxcsAndAmps = computeTotalPowerConsumptionOXCsAndAmps(cp.getMesh(), cp);
			energyOXCsAndAmps = PcOxcsAndAmps; // Watt
		}
		
		if(success){
			Double duracao = request.getTimeOfFinalizeHours() - request.getTimeOfRequestHours();
			duracao *= 3600.0; // Converting to seconds
			
			sumDataTransmitted += duracao * request.getRequiredBandwidth(); // Total data transmitted by the flow (bits)
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
	public double getAverageGeneralPowerConsumption(){
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
	 * Return the total power consumption of the OXCs and amplifiers
	 * 
	 * @param mesh Mesh
	 * @param cp ControlPlane
	 * @return double Watt
	 */
	public double computeTotalPowerConsumptionOXCsAndAmps(Mesh mesh, ControlPlane cp){
		double PCoxcs = EnergyConsumption.computeOxcsPowerConsumption(mesh.getNodeList());
		double PClinks = EnergyConsumption.computeLinksPowerConsumption(mesh.getLinkList(), cp);
		double PcOxcsAndAmps = PCoxcs + PClinks;
		return PcOxcsAndAmps;
	}
	
	/**
	 * Return the energy efficiency
	 * 
	 * @return double bits/Joule
	 */
	public double getEnergyEfficiencyGeneral(){
		double totalEcOXCsAndAmps = energyOXCsAndAmps * tempoDeSimulacao; // Joule
		double ef = sumDataTransmitted / (totalEnergyTransponders + totalEcOXCsAndAmps); // bits/Joule
		return ef;
	}
	
	/**
	 * Returns the total data transmitted
	 * 
	 * @return double bits
	 */
	public double getTotalDataTransmitted(){
		return sumDataTransmitted;
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
	 * Returns the energy consumption of the Oxcs and the amplifiers
	 * 
	 * @return double Joule
	 */
	public double getTotalEnergyConsumptionOxcsAndAmps(){
		return (energyOXCsAndAmps * tempoDeSimulacao);
	}
	
}
