package simulationControl.distributedProcessing;

import measurement.Measurements;
import simulator.Simulation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerSInterface extends Remote {

    public boolean isAlive() throws RemoteException;
    public String getName() throws RemoteException;
    public Measurements simulate(Simulation simulation) throws Exception;

}
