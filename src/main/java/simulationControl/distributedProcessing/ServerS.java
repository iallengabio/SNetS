package simulationControl.distributedProcessing;

import measurement.Measurements;
import simulator.Simulation;
import simulator.Simulator;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerS extends UnicastRemoteObject implements ServerSInterface {
    String name = "anonimous";

    private int numJobs = 0;

    public static void runServerS(String serverMLocation){
        try {
            ServerMInterface server = (ServerMInterface) Naming.lookup("//"+serverMLocation+"/ServerM");
            ServerS severS = new ServerS();
            server.register(severS);
            System.out.println("registered");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected ServerS() throws RemoteException {

    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Measurements simulate(Simulation simulation) throws Exception {
        Simulator simulator = new Simulator(simulation);
        //System.out.println("init simulation");
        Measurements res = simulator.start();
        //System.out.println("end simulation");
        numJobs++;
        System.out.println(numJobs);
        return res;
    }


}
