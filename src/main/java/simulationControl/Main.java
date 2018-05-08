package simulationControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import network.Mesh;
import network.Pair;
import network.RequestGenerator;
import simulationControl.parsers.NetworkConfig;
import simulationControl.parsers.PhysicalLayerConfig;
import simulationControl.parsers.SimulationConfig;
import simulationControl.parsers.SimulationRequest;
import simulationControl.parsers.TrafficConfig;
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
        if (args.length > 0) { // To run in local mode
            localSimulation(args[0]);
            
        } else { // To run in Server mode
            simulationServer();
        }
    }

    /**
     * Simulator runs in server mode
     * 
     * @throws IOException
     */
    private static void simulationServer() throws IOException {
        initFirebase();
        System.out.println("SNetS Simulation Server Running");
        FirebaseDatabase.getInstance().getReference("simulations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Gson gson = new GsonBuilder().create();
                SimulationRequest sr = gson.fromJson(dataSnapshot.getValue(false).toString(), SimulationRequest.class);
                //SimulationRequest sr = (SimulationRequest) dataSnapshot.getValue();
                if(sr.getStatus().equals("new")) {
                    try {
                        dataSnapshot.getRef().child("status").setValue("started");
                        dataSnapshot.getRef().child("progress").setValue(0.0);
                        List<List<Simulation>> allSimulations = createAllSimulations(sr.getNetworkConfig(), sr.getSimulationConfig(), sr.getTrafficConfig(), sr.getPhysicalLayerConfig());
                        //remember to implement with thread
                        SimulationManagement sm = new SimulationManagement(allSimulations);
                        sm.startSimulations(new SimulationManagement.SimulationProgressListener() {
                            @Override
                            public void onSimulationProgressUpdate(double progress) {
                                dataSnapshot.getRef().child("progress").setValue(progress);
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
                        sr.setProgress(1.0);
                        sr.setStatus("finished");
                        dataSnapshot.getRef().setValue(sr);

                    } catch (Exception e) {
                        e.printStackTrace();
                        dataSnapshot.getRef().child("status").setValue("failed");
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
        System.out.println("Reading files");
        System.out.println("Path: " + path);
        List<List<Simulation>> allSimulations = createAllSimulations(path);
        //Now start the simulations
        System.out.println("Starting simulations");
        SimulationManagement sm = new SimulationManagement(allSimulations);
        sm.startSimulations(new SimulationManagement.SimulationProgressListener() {
            @Override
            public void onSimulationProgressUpdate(double progress) {
                System.out.println("progress: "+ progress*100 + "%");
            }

            @Override
            public void onSimulationFinished() {
            	// Nothing at the moment
            }
        });
        System.out.println("saving results");
        sm.saveResults(path);
        System.out.println("finish!");
    }

    /**
     * This method creates the simulations from the local mode
     * 
     * @param args String Paths of the simulations configuration files
     * @return List<List<Simulation>>
     * @throws Exception
     */
    private static List<List<Simulation>> createAllSimulations(String path) throws Exception {

        //Path of the simulation configuration files
    	String separator = System.getProperty("file.separator");
        String filesPath = path;
        String networkFilePath = filesPath + separator + "network";
        String simulationFilePath = filesPath + separator + "simulation";
        String traficFilePath = filesPath + separator + "traffic";
        //String routesFilePath = filesPath + separator + "fixedRoutes";
        String physicalLayerFilePath = filesPath + separator + "physicalLayer";
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
        scanner = new Scanner(new File(traficFilePath));
        String trafficConfigJSON = "";
        while (scanner.hasNext()) {
            trafficConfigJSON += scanner.next();
        }
        scanner = new Scanner(new File(physicalLayerFilePath));
        String physicalLayerConfigJSON = "";
        while (scanner.hasNext()) {
        	physicalLayerConfigJSON += scanner.next();
        }
        
        Gson gson = new GsonBuilder().create();
        NetworkConfig nc = gson.fromJson(networkConfigJSON, NetworkConfig.class);
        SimulationConfig sc = gson.fromJson(simulationConfigJSON, SimulationConfig.class);
        TrafficConfig tc = gson.fromJson(trafficConfigJSON, TrafficConfig.class);
        PhysicalLayerConfig plc = gson.fromJson(physicalLayerConfigJSON, PhysicalLayerConfig.class);
        
        scanner.close();
        
        return createAllSimulations(nc, sc, tc, plc);
    }

    /**
     * This method creates the simulations from server mode
     * 
     * @param nc NetworkConfig
     * @param sc SimulationConfig
     * @param tc TrafficConfig
     * @return List<List<Simulation>>
     * @throws Exception
     */
    private static List<List<Simulation>> createAllSimulations(NetworkConfig nc, SimulationConfig sc, TrafficConfig tc, PhysicalLayerConfig plc) throws Exception {
        // Create list of simulations
        List<List<Simulation>> allSimulations = new ArrayList<>(); // Each element of this set is a list with 10 replications from the same load point
        int i, j;
        for (i = 0; i < sc.getLoadPoints(); i++) { // Create the simulations for each load point
            List<Simulation> reps = new ArrayList<>();
            for (j = 0; j < sc.getReplications(); j++) { // Create the simulations for each replication
                Mesh m = new Mesh(nc, tc, plc);
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
