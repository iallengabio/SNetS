package simulationControl.distributedProcessing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientProgressCallbackInterface extends Remote {

    public void updateProgress(double progress) throws RemoteException;

}
