package simulationControl;

import java.io.*;
import java.net.SocketImpl;
import java.util.*;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import network.Mesh;
import network.Pair;
import network.RequestGenerator;
import simulationControl.distributedProcessing.Client;
import simulationControl.distributedProcessing.ServerM;
import simulationControl.distributedProcessing.ServerS;
import simulationControl.parsers.*;
import simulator.Simulation;

/**
 * This class has the main method that will instantiate the parsers to read the 
 * configuration files and start the simulation
 * 
 * @author Iallen
 */
public class Main {

    /**
     * Main method
     * 
     * @param args String[] - arg[0] - Path of configuration files
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if(args.length==0){
            System.out.println("No option selected");
        }else{
            switch (args[0]){
                case "-fs":
                    simulationServer();
                break;
                case "-lm":
                    ServerM.runServerM();
                break;
                case "-ls":
                    ServerS.runServerS(args[1]);
                break;
                case "-lc":
                    Client.runClient(args[1],args[2]);
                break;
                default:
                    localSimulation(args[0]);
            }
        }
    }

    /**
     * Simulator runs in server mode
     * 
     * @throws IOException
     */
    private static void simulationServer() throws IOException {
        initFirebase();

        DatabaseReference simSerRef = FirebaseDatabase.getInstance().getReference("simulationServers").push();
        simSerRef.setValue(new SimulationServer(),1);
        System.out.println("SNetS Simulation Server Running");
        System.out.println("simulation server key: " + simSerRef.getKey());
        FirebaseDatabase.getInstance().getReference("simulationServers/" +simSerRef.getKey()+"/online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!(boolean)dataSnapshot.getValue()){
                    dataSnapshot.getRef().setValue(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); //sinalizates that server is alive

        FirebaseDatabase.getInstance().getReference("simulationServers/" +simSerRef.getKey()+"/simulationQueue").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Gson gson = new GsonBuilder().create();
                String srjson = dataSnapshot.getValue(false).toString();
                SimulationRequest sr = gson.fromJson(srjson, SimulationRequest.class);
                DatabaseReference newRef = FirebaseDatabase.getInstance().getReference("simulations").push();
                newRef.setValue(sr);
                dataSnapshot.getRef().removeValue();

                if(sr.getStatus().equals("new")) {
                    try {
                        newRef.child("status").setValue("started");
                        newRef.child("progress").setValue(0.0);
                        List<List<Simulation>> allSimulations = createAllSimulations(sr);
                        //remember to implement with thread
                        SimulationManagement sm = new SimulationManagement(allSimulations,1);
                        sm.startSimulations(new SimulationManagement.SimulationProgressListener() {
                            @Override
                            public void onSimulationProgressUpdate(double progress) {
                                newRef.child("progress").setValue(progress);
                            }

                            @Override
                            public void onSimulationFinished() {//do nothing
                            }
                        });
                        if(sr.getSimulationConfig().getActiveMetrics().BlockingProbability)sr.getResult().blockingProbability = sm.getBlockingProbabilityCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().BandwidthBlockingProbability)sr.getResult().bandwidthBlockingProbability = sm.getBandwidthBlockingProbabilityCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().ExternalFragmentation)sr.getResult().externalFragmentation = sm.getExternalFragmentationCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().RelativeFragmentation)sr.getResult().relativeFragmentation = sm.getRelativeFragmentationCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().SpectrumUtilization)sr.getResult().spectrumUtilization = sm.getSpectrumUtilizationCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().TransmittersReceiversRegeneratorsUtilization)sr.getResult().transceiversUtilization = sm.getTransceiversUtilizationCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().SpectrumSizeStatistics)sr.getResult().spectrumStatistics = sm.getSpectrumStatisticsCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().EnergyConsumption)sr.getResult().energyConsumption = sm.getEnergyConsumptionCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().ModulationUtilization)sr.getResult().modulationUtilization = sm.getModulationUtilizationCsv();
                        if(sr.getSimulationConfig().getActiveMetrics().ConsumedEnergy)sr.getResult().consumedEnergy = sm.getConsumedEnergyCsv();
                        sr.setProgress(1.0);
                        sr.setStatus("finished");
                        newRef.setValue(sr);

                    } catch (Exception e) {
                        e.printStackTrace();
                        newRef.child("status").setValue("failed");
                    }
                }else{//do nothing
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {//do nothing
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {//do nothing
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {//do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {//do nothing yet
            }
        });

        while(true){//Keep the server powered on
            try {
                Thread.sleep(1000);
                //System.out.println("thread");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Initialize Firebase
     * 
     * @throws IOException
     */
    private static void initFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("private-key-firebase.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl("https://snets-2905e.firebaseio.com").build();
        FirebaseApp.initializeApp(options);
    }

    /**
     * Simulator runs in local mode
     * 
     * @param path String Paths of the simulations configuration files
     * @throws Exception
     */
    private static void localSimulation(String path) throws Exception {
    	System.out.println("Path: " + path);
        System.out.println("Reading files");
        List<List<Simulation>> allSimulations = createAllSimulations(makeSR(path));
        
        String separator = System.getProperty("file.separator");
        String simulationFilePath = path + separator + "simulation";
        Scanner scanner = new Scanner(new File(simulationFilePath));
        String simulationConfigJSON = "";
        while (scanner.hasNext()) {
            simulationConfigJSON += scanner.next();
        }
        Gson gson = new GsonBuilder().create();
        SimulationConfig sc = gson.fromJson(simulationConfigJSON, SimulationConfig.class);
        scanner.close();
        System.out.println("Threads running: " + sc.getThreads());
        //Now start the simulations
        System.out.println("Starting simulations");
        SimulationManagement sm = new SimulationManagement(allSimulations, sc.getThreads());
        sm.startSimulations(new SimulationManagement.SimulationProgressListener() {
            @Override
            public void onSimulationProgressUpdate(double progress) {
                System.out.println("progress: "+ progress*100 + "%");
            }

            @Override
            public void onSimulationFinished() {

            }
        });
        System.out.println("saving results");
        sm.saveResults(path);
        System.out.println("finish!");
    }

    public static SimulationRequest makeSR(String path) throws FileNotFoundException {
        //Path of the simulation configuration files
        String separator = System.getProperty("file.separator");
        String filesPath = path;
        String networkFilePath = filesPath + separator + "network";
        String simulationFilePath = filesPath + separator + "simulation";
        String trafficFilePath = filesPath + separator + "traffic";
        //String routesFilePath = filesPath + separator + "fixedRoutes";
        String physicalLayerFilePath = filesPath + separator + "physicalLayer";
        String othersFilePath = filesPath + separator + "others";
        Util.projectPath = filesPath;

        //Read files
        Scanner scanner = new Scanner(new File(networkFilePath));
        String networkConfigJSON = "";
        while (scanner.hasNext()) {
            networkConfigJSON += scanner.next();
        }
        scanner = new Scanner(new File(simulationFilePath));
        String simulationConfigJSON = "";
        while (scanner.hasNext()) {
            simulationConfigJSON += scanner.next();
        }
        scanner = new Scanner(new File(trafficFilePath));
        String trafficConfigJSON = "";
        while (scanner.hasNext()) {
            trafficConfigJSON += scanner.next();
        }
        scanner = new Scanner(new File(physicalLayerFilePath));
        String physicalLayerConfigJSON = "";
        while (scanner.hasNext()) {
            physicalLayerConfigJSON += scanner.next();
        }
        scanner = new Scanner(new File(othersFilePath));
        String othersConfigJSON = "";
        while (scanner.hasNext()) {
            othersConfigJSON += scanner.next();
        }

        Gson gson = new GsonBuilder().create();
        NetworkConfig nc = gson.fromJson(networkConfigJSON, NetworkConfig.class);
        SimulationConfig sc = gson.fromJson(simulationConfigJSON, SimulationConfig.class);
        TrafficConfig tc = gson.fromJson(trafficConfigJSON, TrafficConfig.class);
        PhysicalLayerConfig plc = gson.fromJson(physicalLayerConfigJSON, PhysicalLayerConfig.class);
        OthersConfig oc = gson.fromJson(othersConfigJSON,OthersConfig.class);
        scanner.close();

        SimulationRequest sr = new SimulationRequest();
        sr.setNetworkConfig(nc);
        sr.setSimulationConfig(sc);
        sr.setTrafficConfig(tc);
        sr.setPhysicalLayerConfig(plc);
        sr.setOthersConfig(oc);
        sr.setStatus("new");
        sr.setProgress(0.0);

        return sr;
    }

    /**
     * This method creates all simulations of an experiment
     *
     * @throws Exception
     */
    public static List<List<Simulation>> createAllSimulations(SimulationRequest sr) throws Exception {

        NetworkConfig nc = sr.getNetworkConfig();
        SimulationConfig sc = sr.getSimulationConfig();
        TrafficConfig tc = sr.getTrafficConfig();
        PhysicalLayerConfig plc = sr.getPhysicalLayerConfig();
        OthersConfig oc = sr.getOthersConfig();
        
        System.out.println("Calculating the modulations transmission ranges");
        Mesh meshTemp = new Mesh(nc, tc, plc, oc, null);
        
        // Create list of simulations
        List<List<Simulation>> allSimulations = new ArrayList<>(); // Each element of this set is a list with 10 replications from the same load point
        int i, j;
        for (i = 0; i < sc.getLoadPoints(); i++) { // Create the simulations for each load point
            List<Simulation> reps = new ArrayList<>();
            for (j = 0; j < sc.getReplications(); j++) { // Create the simulations for each replication
                Mesh m = new Mesh(nc, tc, plc, oc, meshTemp.getModTrDistance());
                incArrivedRate(m.getPairList(), i);
                Simulation s = new Simulation(sc, m, i, j);
                reps.add(s);
            }
            allSimulations.add(reps);
        }

        return allSimulations;
    }


    /**
     * This method sets the loading point of the simulation in each request generator
     *
     * @param pairs Vector<Pair>
     * @param mult int
     */
    private static void incArrivedRate(Vector<Pair> pairs, int mult) {
        for (Pair pair : pairs) {
            for (RequestGenerator rg : pair.getRequestGenerators()) {
                rg.incArrivedRate(mult);
            }
        }
    }

}
