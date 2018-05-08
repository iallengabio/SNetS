package simulationControl.parsers;

import java.util.ArrayList;
import java.util.List;

public class SimulationServer {

    private boolean online = false;
    private List<SimulationRequest> simulationQueue = new ArrayList<>();

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public List<SimulationRequest> getSimulationQueue() {
        return simulationQueue;
    }

    public void setSimulationQueue(List<SimulationRequest> simulationQueue) {
        this.simulationQueue = simulationQueue;
    }
}
