package simulationControl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import grmlsa.integrated.CompleteSharing;
import network.ControlPlane;
import simulationControl.distributedProcessing.Client;
import simulationControl.distributedProcessing.ServerM;
import simulationControl.distributedProcessing.ServerS;
import simulationControl.parsers.SimulationConfig;
import simulationControl.parsers.SimulationRequest;
import simulationControl.parsers.SimulationServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
                        newRef.child("status").setValue("started",null);
                        newRef.child("progress").setValue(0.0,null);
                        //remember to implement with thread
                        SimulationManagement sm = new SimulationManagement(sr);
                        sm.startSimulations(new SimulationManagement.SimulationProgressListener() {
                            @Override
                            public void onSimulationProgressUpdate(double progress) {
                                newRef.child("progress").setValue(progress);
                            }

                            @Override
                            public void onSimulationFinished() {//do nothing
                            }
                        });
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

        File f = new File(path);
        String name = f.getName();
        path = f.getAbsoluteFile().getParentFile().getPath();

        System.out.println("Path: " + path);
        System.out.println("Simulation: " + name);
        System.out.println("Reading files");

        SimulationFileManager sfm = new SimulationFileManager();
        SimulationRequest simulationRequest = sfm.readSimulation(path, name);
        SimulationConfig sc = simulationRequest.getSimulationConfig();
        System.out.println("Threads running: " + sc.getThreads());
        
        //Now start the simulations
        System.out.println("Starting simulations");
        SimulationManagement sm = new SimulationManagement(simulationRequest);
        
        long start = System.nanoTime();
        
        sm.startSimulations(new SimulationManagement.SimulationProgressListener() {
            @Override
            public void onSimulationProgressUpdate(double progress) {
                System.out.println("progress: "+ (progress * 100) + "%");
            }

            @Override
            public void onSimulationFinished() {

            }
        });
        
        long end = System.nanoTime();
        
        //Salvando os resultados para a base de dados
        //ControlPlane.SalvarCSV();
        
        System.out.println("saving results");
        sfm.writeSimulation(path,simulationRequest);
        //sm.saveResults(path);
        System.out.println("finish!");
        
        long time = end - start;
		System.out.println("Total simulation time (s): " + (time / 1000000000.0));
    }


}
