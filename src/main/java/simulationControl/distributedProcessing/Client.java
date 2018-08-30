package simulationControl.distributedProcessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import grmlsa.modulation.Modulation;
import measurement.*;
import network.Mesh;
import simulationControl.Main;
import simulationControl.parsers.SimulationRequest;
import simulator.Simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.regex.Pattern;

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
            System.out.println("Saving results.");
            saveResults(sr,path);
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

    public static void saveResults(SimulationRequest sr, String path){

        if(sr.getSimulationConfig().getActiveMetrics().BlockingProbability){
            BlockingProbability bp = new BlockingProbability(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().blockingProbability);
        }
        if(sr.getSimulationConfig().getActiveMetrics().BandwidthBlockingProbability){
            BandwidthBlockingProbability bp = new BandwidthBlockingProbability(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().bandwidthBlockingProbability);
        }
        if(sr.getSimulationConfig().getActiveMetrics().ExternalFragmentation){
            ExternalFragmentation bp = new ExternalFragmentation(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().externalFragmentation);
        }
        if(sr.getSimulationConfig().getActiveMetrics().RelativeFragmentation){
            RelativeFragmentation bp = new RelativeFragmentation(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().relativeFragmentation);
        }
        if(sr.getSimulationConfig().getActiveMetrics().SpectrumUtilization){
            SpectrumUtilization bp = new SpectrumUtilization(0,0,new Mesh(sr.getNetworkConfig(),sr.getTrafficConfig(),sr.getPhysicalLayerConfig(),sr.getOthersConfig()));
            saveResult(path,bp.getFileName(),sr.getResult().spectrumUtilization);
        }
        if(sr.getSimulationConfig().getActiveMetrics().SpectrumSizeStatistics){
            SpectrumSizeStatistics bp = new SpectrumSizeStatistics(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().spectrumStatistics);
        }
        if(sr.getSimulationConfig().getActiveMetrics().EnergyConsumption){
            MetricsOfEnergyConsumption bp = new MetricsOfEnergyConsumption(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().energyConsumption);
        }
        if(sr.getSimulationConfig().getActiveMetrics().ModulationUtilization){
            ModulationUtilization bp = new ModulationUtilization(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().modulationUtilization);
        }
        if(sr.getSimulationConfig().getActiveMetrics().ConsumedEnergy){
            ConsumedEnergy bp = new ConsumedEnergy(0,0);
            saveResult(path,bp.getFileName(),sr.getResult().consumedEnergy);
        }
    }

    public static void saveResult(String path, String metric, String resultCsv)  {
        String separador = System.getProperty("file.separator");
        String aux[] = path.split(Pattern.quote(separador));
        String nomePasta = aux[aux.length - 1];
        FileWriter fw;
        String p = path + separador + nomePasta + metric;

        try {
            fw = new FileWriter(new File(p));
            fw.write(resultCsv);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
