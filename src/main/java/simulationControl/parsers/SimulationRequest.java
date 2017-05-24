package simulationControl.parsers;

/**
 * Created by Iallen on 23/05/2017.
 */
public class SimulationRequest {
    private NetworkConfig networkConfig;
    private TrafficConfig trafficConfig;
    private SimulationConfig simulationConfig;
    private String status;

    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    public void setNetworkConfig(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public TrafficConfig getTrafficConfig() {
        return trafficConfig;
    }

    public void setTrafficConfig(TrafficConfig trafficConfig) {
        this.trafficConfig = trafficConfig;
    }

    public SimulationConfig getSimulationConfig() {
        return simulationConfig;
    }

    public void setSimulationConfig(SimulationConfig simulationConfig) {
        this.simulationConfig = simulationConfig;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
