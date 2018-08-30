package simulationControl.distributedProcessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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



    public static void main(String args[]){
        try {
            ServerMInterface server = (ServerMInterface) Naming.lookup("//127.0.0.1/ServerM");
            ServerS severS = new ServerS();
            server.register(severS);
            System.out.println("registered");
        }catch (RemoteException ex){
            ex.printStackTrace();
        }catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (NotBoundException e) {
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
    public String simulate(String simulation) throws Exception {
        Gson gson = new GsonBuilder().create();
        Simulator simulator = new Simulator(gson.fromJson(simulation,Simulation.class));
        System.out.println("init simulation");
        Measurements res = simulator.start();
        System.out.println("end simulation");
        return gson.toJson(res);
    }


}
