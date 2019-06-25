package simulationControl.distributedProcessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import grmlsa.modulation.Modulation;
import measurement.*;
import network.Mesh;
import simulationControl.Main;
import simulationControl.SimulationFileManager;
import simulationControl.parsers.SimulationRequest;
import simulator.Simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.regex.Pattern;

public class Client {

    public static void runClient(String serverMLocation, String path){
        try {
            ServerMInterface server = (ServerMInterface) Naming.lookup("//"+serverMLocation+"/ServerM");

            File f = new File(path);
            String name = f.getName();
            path = f.getAbsoluteFile().getParentFile().getPath();

            System.out.println("Path: " + path);
            System.out.println("Simulation: " + name);
            System.out.println("Reading files");

            SimulationFileManager sfm = new SimulationFileManager();
            SimulationRequest sr = sfm.readSimulation(path, name);

            Gson gson = new GsonBuilder().create();
            String simReqJSON = gson.toJson(sr);
            simReqJSON = server.simulationBundleRequest(simReqJSON, new ClientProgressCallback());
            sr = gson.fromJson(simReqJSON,SimulationRequest.class);
            System.out.println("Saving results.");
            sfm.writeSimulation(path,sr);
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

    public static class ClientProgressCallback extends UnicastRemoteObject implements ClientProgressCallbackInterface {

        protected ClientProgressCallback() throws RemoteException {

        }

        @Override
        public void updateProgress(double progress) throws RemoteException {
            System.out.println("progress: "+progress*100 + "%");
        }
    }

}
