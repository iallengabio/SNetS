package simulationControl.parsers;

/**
 * This class represents the request for a simulation when the simulator is in server mode.
 * 
 * Created by Iallen on 23/05/2017.
 */
public class SimulationRequest {

    private String name;

    private NetworkConfig networkConfig;
    private TrafficConfig trafficConfig;
    private SimulationConfig simulationConfig;
    private PhysicalLayerConfig physicalLayerConfig;
    private OthersConfig othersConfig;

    private String status;
    private Double progress = 0.0;
    private Result result = new Result();
    
    /**
     * This class represents the results obtained by the performance metrics in the execution of the simulation
     * 
     * @author Iallen
     */
    public static class Result {
        public final static String FILE_BLOCKING_PROBABILITY = "BlockingProbability.csv";
        public final static String FILE_BANDWIDTH_BLOCKING_PROBABILITY = "BandwidthBlockingProbability.csv";
        public final static String FILE_EXTERNAL_FRAGMENTATION = "ExternalFragmentation.csv";
        public final static String FILE_RELATIVE_FRAGMENTATION = "RelativeFragmentation.csv";
        public final static String FILE_SPECTRUM_UTILIZATION = "SpectrumUtilization.csv";
        public final static String FILE_TRANSMITTERS_RECEIVERS_REGENERATORS_UTILIZATION = "TransmittersReceiversRegeneratorsUtilization.csv";
        public final static String FILE_GROOMING_STATISTICS = "GroomingStatistics.csv";
        public final static String FILE_SPECTRUM_STATISTICS = "SpectrumStatistics.csv";
        public final static String FILE_MODULATION_UTILIZATION = "ModulationUtilization.csv";
        public final static String FILE_CONSUMEDEN_ERGY = "ConsumedEnergy.csv";

        public String blockingProbability;
        public String bandwidthBlockingProbability;
        public String externalFragmentation;
        public String relativeFragmentation;
        public String spectrumUtilization;
        public String spectrumStatistics;
        public String modulationUtilization;
        public String consumedEnergy;
        public String transmittersReceiversRegeneratorsUtilization;
        public String groomingStatistics;
    }

    /**
     * Return the result
     * 
     * @return Result
     */
    public Result getResult() {
        return result;
    }

    /**
     * Sets the result
     * 
     * @param result Result
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * Returns the networkConfig
     * 
     * @return NetworkConfig
     */
    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    /**
     * Sets the networkConfig
     * 
     * @param networkConfig NetworkConfig
     */
    public void setNetworkConfig(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    /**
     * Returns the trafficConfig
     * 
     * @return trafficConfig
     */
    public TrafficConfig getTrafficConfig() {
        return trafficConfig;
    }

    /**
     * Sets the trafficConfig
     * 
     * @param trafficConfig TrafficConfig
     */
    public void setTrafficConfig(TrafficConfig trafficConfig) {
        this.trafficConfig = trafficConfig;
    }

    /**
     * Returns the simulationConfig
     * 
     * @return SimulationConfig
     */
    public SimulationConfig getSimulationConfig() {
        return simulationConfig;
    }

    /**
     * Sets the simulationConfig
     * 
     * @param simulationConfig SimulationConfig
     */
    public void setSimulationConfig(SimulationConfig simulationConfig) {
        this.simulationConfig = simulationConfig;
    }

    /**
     * Returns the physicalLayerConfig
     * 
	 * @return the physicalLayerConfig
	 */
	public PhysicalLayerConfig getPhysicalLayerConfig() {
		return physicalLayerConfig;
	}

	/**
	 * Sets the physicalLayerConfig
	 * 
	 * @param physicalLayerConfig the physicalLayerConfig to set
	 */
	public void setPhysicalLayerConfig(PhysicalLayerConfig physicalLayerConfig) {
		this.physicalLayerConfig = physicalLayerConfig;
	}

	/**
     * Returns the status
     * 
     * @return String
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status
     * 
     * @param status String
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the name
     * 
     * @return String name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the progress
     * 
     * @return Double
     */
    public Double getProgress() {
        return progress;
    }

    /**
     * Sets the progress
     * 
     * @param progress Double
     */
    public void setProgress(Double progress) {
        this.progress = progress;
    }

    /**
     * Returns the OthersConfig
     * 
     * @return OthersConfig
     */
    public OthersConfig getOthersConfig() {
        return othersConfig;
    }

    /**
     * Sets the OthersConfig
     * 
     * @param othersConfig OthersConfig
     */
    public void setOthersConfig(OthersConfig othersConfig) {
        this.othersConfig = othersConfig;
    }
}
