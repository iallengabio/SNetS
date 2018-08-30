package simulationControl.distributedProcessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import simulationControl.Main;
import simulationControl.parsers.SimulationRequest;
import simulator.Simulation;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class Client {

    public static void main(String args[]){
        try {
            ServerMInterface server = (ServerMInterface) Naming.lookup("//127.0.0.1/ServerM");

            String path = args[0];
            System.out.println("Reading files");
            SimulationRequest sr = Main.makeSR(path);
            Gson gson = new GsonBuilder().create();
            String simReqJSON = gson.toJson(sr);
            simReqJSON = server.simulationBundleRequest(simReqJSON);
            sr = gson.fromJson(simReqJSON,SimulationRequest.class);

            System.out.println("Simulation ends.");



        }catch (RemoteException ex){
            ex.printStackTrace();
        }catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
