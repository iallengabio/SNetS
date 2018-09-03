package simulationControl.distributedProcessing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerMInterface extends Remote {

    public void register(ServerSInterface server) throws RemoteException;

    public String simulationBundleRequest(String simReqJSON, ClientProgressCallbackInterface cpci) throws Exception;
}
