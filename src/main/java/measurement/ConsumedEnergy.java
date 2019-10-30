package measurement;

import network.ControlPlane;
import request.RequestForConnection;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.ConsumedEnergyResultManager;

/**
 * This class is responsible for metrics of consumed energy
 * The metric represented by this class is associated with a load point and a replication
 * 
 * @author Iallen, Alexandre
 *
 */
public class ConsumedEnergy extends Measurement {

    private double totalConsumedEnergy;
    private double lastInstantTime;
    private double totalNetworkOperationTime;
    
    private double totalConsumedEnergyTransponders;
    private double totalConsumedEnergyOXCs;
    private double totalConsumedEnergyAmplifiers;
    
    private double totalDataTransmitted;

    /**
     * Creates a new instance of ConsumedEnergy
     * 
     * @param loadPoint int
     * @param rep int
     */
    public ConsumedEnergy(int loadPoint, int rep) {
        super(loadPoint, rep);
        
        totalConsumedEnergy = 0.0;
        lastInstantTime = 0.0;
        totalNetworkOperationTime = 0.0;
        
        totalConsumedEnergyTransponders = 0.0;
        totalConsumedEnergyOXCs = 0.0;
        totalConsumedEnergyAmplifiers = 0.0;
        
        totalDataTransmitted = 0.0;

        resultManager = new ConsumedEnergyResultManager();
    }

    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request){
    	double instantTime = request.getTimeOfRequestHours();
        instantTime *= 3600.0; // Converting to seconds
        
        if(instantTime > totalNetworkOperationTime){
            totalNetworkOperationTime = instantTime;
        }
        
        double timeDiffer = instantTime - lastInstantTime;
        
        totalConsumedEnergy += timeDiffer * cp.getMesh().getTotalPowerConsumption();
        totalConsumedEnergyTransponders += timeDiffer * cp.getMesh().getTotalPowerConsumptionTransponders();
        totalConsumedEnergyOXCs += timeDiffer * cp.getMesh().getTotalPowerConsumptionOXCs();
        totalConsumedEnergyAmplifiers += timeDiffer * cp.getMesh().getTotalPowerConsumptionAmplifiers();
        
        totalDataTransmitted += timeDiffer * cp.getMesh().getTotalDataTransmitted();
        
        lastInstantTime = instantTime;
    }

    @Override
    public String getFileName() {
        return SimulationRequest.Result.FILE_CONSUMEDEN_ERGY;
    }

    /**
     * Returns the total consumed energy
     * 
     * @return double
     */
    public double getTotalConsumedEnergy() {
        return totalConsumedEnergy;
    }
    
    /**
     * Returns the total consumed energy by transponders
     * 
     * @return double
     */
    public double getTotalConsumedEnergyTransponders(){
    	return totalConsumedEnergyTransponders;
    }
    
    /**
     * Returns the total consumed energy by OXCs
     * 
     * @return double
     */
    public double getTotalConsumedEnergyOXCs(){
    	return totalConsumedEnergyOXCs;
    }
    
    /**
     * Returns the total consumed energy by amplifiers
     * 
     * @return double
     */
    public double getTotalConsumedEnergyAmplifiers(){
    	return totalConsumedEnergyAmplifiers;
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
     * Returns the total power consumption
     * 
     * @return double
     */
    public double getTotalPowerConsumption(){
    	return (totalConsumedEnergy / totalNetworkOperationTime);
    }
    
    /**
     * Returns the energy efficiency
     * 
     * @return double
     */
    public double getEnergyEfficiency(){
    	return (totalDataTransmitted / totalConsumedEnergy);
    }
}
