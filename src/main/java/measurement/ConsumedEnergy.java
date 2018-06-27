package measurement;

import network.ControlPlane;
import request.RequestForConnection;
import simulationControl.resultManagers.ConsumedEnergyResultManager;
import simulationControl.resultManagers.MetricsOfEnergyConsumptionResultManager;

import java.util.HashMap;

public class ConsumedEnergy extends Measurement {

    private double totalConsumedEnergy;
    private double lastInstantTime;

    public ConsumedEnergy(int loadPoint, int rep) {
        super(loadPoint, rep);
        totalConsumedEnergy = 0;
        fileName = "_ConsumedEnergy.csv";
        lastInstantTime = 0;
        resultManager = new ConsumedEnergyResultManager();
    }

    public void addNewObservation(ControlPlane cp, double instantTime){
        totalConsumedEnergy += (instantTime - lastInstantTime) * cp.getMesh().getTotalPowerConsumption();
        lastInstantTime = instantTime;
    }

    @Override
    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
        throw new UnsupportedOperationException();
    }

    public double getTotalConsumedEnergy() {
        return totalConsumedEnergy;
    }
}
