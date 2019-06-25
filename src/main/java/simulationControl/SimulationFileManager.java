package simulationControl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import simulationControl.parsers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SimulationFileManager {

    public SimulationRequest readSimulation(String path, String name) throws FileNotFoundException {
        SimulationRequest sr = new SimulationRequest();
        sr.setName(name);
        String separator = System.getProperty("file.separator");
        String filesPath = path + separator + name;
        File folder = new File(filesPath);
        File[] listOfFiles = folder.listFiles();
        Gson gson = new GsonBuilder().create();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                switch(listOfFiles[i].getName()){
                    case "network":
                        sr.setNetworkConfig(gson.fromJson(readFile(listOfFiles[i]),NetworkConfig.class));
                        break;
                    case "simulation":
                        sr.setSimulationConfig(gson.fromJson(readFile(listOfFiles[i]),SimulationConfig.class));
                        break;
                    case "physicalLayer":
                        sr.setPhysicalLayerConfig(gson.fromJson(readFile(listOfFiles[i]),PhysicalLayerConfig.class));
                        break;
                    case "others":
                        sr.setOthersConfig(gson.fromJson(readFile(listOfFiles[i]),OthersConfig.class));
                        break;
                    case "traffic":
                        sr.setTrafficConfig(gson.fromJson(readFile(listOfFiles[i]),TrafficConfig.class));
                        break;
                    default: //results
                        String metric = listOfFiles[i].getName();
                        String s[] = metric.split("_");
                        metric = s[s.length-1];
                        //metric = metric.split("" +"[.]")[0];

                        switch(metric){
                            case SimulationRequest.Result
                                    .FILE_BANDWIDTH_BLOCKING_PROBABILITY:
                                sr.getResult().bandwidthBlockingProbability = readFile(listOfFiles[i],true);
                                break;
                            case SimulationRequest.Result
                                    .FILE_BLOCKING_PROBABILITY:
                                sr.getResult().blockingProbability = readFile(listOfFiles[i],true);
                                break;
                            case SimulationRequest.Result
                                    .FILE_CONSUMEDEN_ERGY:
                                sr.getResult().consumedEnergy = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_ENERGY_CONSUMPTION:
                                sr.getResult().energyConsumption = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_EXTERNAL_FRAGMENTATION:
                                sr.getResult().externalFragmentation = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_GROOMING_STATISTICS:
                                sr.getResult().groomingStatistics = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_MODULATION_UTILIZATION:
                                sr.getResult().modulationUtilization = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_RELATIVE_FRAGMENTATION:
                                sr.getResult().relativeFragmentation = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_SPECTRUM_STATISTICS:
                                sr.getResult().spectrumStatistics = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_SPECTRUM_UTILIZATION:
                                sr.getResult().spectrumUtilization = readFile(listOfFiles[i],true);
                            case SimulationRequest.Result
                                    .FILE_TRANSMITTERS_RECEIVERS_REGENERATORS_UTILIZATION:
                                sr.getResult().transmittersReceiversRegeneratorsUtilization = readFile(listOfFiles[i],true);
                        }
                }
            }
        }

        return sr;
    }

    public void writeSimulation(String path, SimulationRequest sr) throws IOException {
        //criar uma pasta com o name e adicionar ao path.
        path = path+System.getProperty("file.separator")+sr.getName();
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            Files.createDirectories(p);
        }

        saveConfig(path,sr);
        saveResult(path,sr);
    }

    private String readFile(File f, boolean carriageReturn) throws FileNotFoundException {
        Scanner scanner = new Scanner(f);
        String res = "";
        while (scanner.hasNextLine()) {
            res += scanner.nextLine();
            if(carriageReturn) res+="\n";
        }
        scanner.close();
        return res;
    }

    private String readFile(File f) throws FileNotFoundException {
        return readFile(f,false);
    }

    private void saveConfig(String path, SimulationRequest sr) throws IOException {
        Gson gson = new GsonBuilder().create();
        saveConfig(path,"network",gson.toJson(sr.getNetworkConfig()));
        saveConfig(path,"simulation",gson.toJson(sr.getSimulationConfig()));
        saveConfig(path,"physicalLayer",gson.toJson(sr.getPhysicalLayerConfig()));
        saveConfig(path,"others",gson.toJson(sr.getOthersConfig()));
        saveConfig(path,"traffic",gson.toJson(sr.getTrafficConfig()));
    }

    private void saveResult(String path, SimulationRequest sr) throws IOException {
        if(sr.getSimulationConfig().getActiveMetrics().BlockingProbability){
            saveResult(path, SimulationRequest.Result.FILE_BLOCKING_PROBABILITY,sr.getResult().blockingProbability);
        }
        if(sr.getSimulationConfig().getActiveMetrics().BandwidthBlockingProbability){
            saveResult(path, SimulationRequest.Result.FILE_BANDWIDTH_BLOCKING_PROBABILITY,sr.getResult().bandwidthBlockingProbability);
        }
        if(sr.getSimulationConfig().getActiveMetrics().ExternalFragmentation){
            saveResult(path, SimulationRequest.Result.FILE_EXTERNAL_FRAGMENTATION,sr.getResult().externalFragmentation);
        }
        if(sr.getSimulationConfig().getActiveMetrics().RelativeFragmentation){
            saveResult(path, SimulationRequest.Result.FILE_RELATIVE_FRAGMENTATION,sr.getResult().relativeFragmentation);
        }
        if(sr.getSimulationConfig().getActiveMetrics().SpectrumUtilization){
            saveResult(path, SimulationRequest.Result.FILE_SPECTRUM_UTILIZATION,sr.getResult().spectrumUtilization);
        }
        if(sr.getSimulationConfig().getActiveMetrics().SpectrumSizeStatistics){
            saveResult(path, SimulationRequest.Result.FILE_SPECTRUM_STATISTICS,sr.getResult().spectrumStatistics);
        }
        if(sr.getSimulationConfig().getActiveMetrics().EnergyConsumption){
            saveResult(path, SimulationRequest.Result.FILE_ENERGY_CONSUMPTION,sr.getResult().energyConsumption);
        }
        if(sr.getSimulationConfig().getActiveMetrics().ModulationUtilization){
            saveResult(path, SimulationRequest.Result.FILE_MODULATION_UTILIZATION,sr.getResult().modulationUtilization);
        }
        if(sr.getSimulationConfig().getActiveMetrics().ConsumedEnergy){
            saveResult(path, SimulationRequest.Result.FILE_CONSUMEDEN_ERGY,sr.getResult().consumedEnergy);
        }
        if(sr.getSimulationConfig().getActiveMetrics().GroomingStatistics){
            saveResult(path, SimulationRequest.Result.FILE_GROOMING_STATISTICS,sr.getResult().groomingStatistics);
        }
        if(sr.getSimulationConfig().getActiveMetrics().TransmittersReceiversRegeneratorsUtilization){
            saveResult(path, SimulationRequest.Result.FILE_TRANSMITTERS_RECEIVERS_REGENERATORS_UTILIZATION,sr.getResult().transmittersReceiversRegeneratorsUtilization);
        }
    }

    private void saveFile(String path, String value) throws IOException {
        if(value==null) return;

        FileWriter fw;

        fw = new FileWriter(new File(path));
        fw.write(value);
        fw.close();
    }

    private void saveResult(String path, String metric, String resultCsv) throws IOException {
        String separador = System.getProperty("file.separator");
        String aux[] = path.split(Pattern.quote(separador));
        String nomePasta = aux[aux.length - 1];
        String p = path + separador + nomePasta +"_"+ metric;
        saveFile(p,resultCsv);
    }

    private void saveConfig(String path, String config, String value) throws IOException {
        String separador = System.getProperty("file.separator");
        String aux[] = path.split(Pattern.quote(separador));
        String nomePasta = aux[aux.length - 1];
        String p = path + separador + config;
        saveFile(p,value);
    }

}
