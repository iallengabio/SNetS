package simulationControl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.google.firebase.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.Mesh;
import network.Pair;
import network.RequestGenerator;
import simulationControl.parsers.NetworkConfig;
import simulationControl.parsers.SimulationConfig;
import simulationControl.parsers.SimulationRequest;
import simulationControl.parsers.TrafficConfig;
import simulator.Simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/**
 * esta classe possui o método main que irá instanciar os parsers para leitura dos arquivos de configuração e iniciar a simulação
 *
 * @author Iallen
 */
public class Main {

    /**
     * @param args arg[0] - > path dos arquivos de configura��o
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            localSimulation(args[0]);
        } else {//funcionar em modo Servidor
            simulationServer();
        }

    }

    private static void simulationServer() throws IOException {
        initFirebase();
        System.out.println("SNetS Simulation Server Running");
        FirebaseDatabase.getInstance().getReference("simulations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Gson gson = new GsonBuilder().create();
                SimulationRequest sr = gson.fromJson(dataSnapshot.getValue(false).toString(),SimulationRequest.class);
                //SimulationRequest sr = (SimulationRequest) dataSnapshot.getValue();
                if(sr.getStatus().equals("new")) {
                    try {
                        dataSnapshot.getRef().child("status").setValue("started");
                        dataSnapshot.getRef().child("progress").setValue(0.0);
                        List<List<Simulation>> allSimulations = createAllSimulations(sr.getNetworkConfig(), sr.getSimulationConfig(), sr.getTrafficConfig());
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
                        sr.getResult().blockingProbability = sm.getBlockingProbabilityCsv();
                        sr.getResult().bandwidthBlockingProbability = sm.getBandwidthBlockingProbabilityCsv();
                        sr.getResult().externalFragmentation = sm.getExternalFragmentationCsv();
                        sr.getResult().relativeFragmentation = sm.getRelativeFragmentationCsv();
                        sr.getResult().spectrumUtilization = sm.getSpectrumUtilizationCsv();
                        sr.getResult().transceiversUtilization = sm.getTransceiversUtilizationCsv();
                        sr.getResult().spectrumStatistics = sm.getSpectrumStatisticsCsv();
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

        while(true){//manter o servidor ligado
            try {
                Thread.sleep(1000);
                //System.out.println("thread");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initFirebase() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("private-key-firebase.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl("https://snets-2905e.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);
    }

    private static void localSimulation(String path) throws Exception {
        System.out.println("Reading files");
        List<List<Simulation>> allSimulations = createAllSimulations(path);
        //agora dar o start nas simulações
        System.out.println("Starting simulations");
        SimulationManagement sm = new SimulationManagement(allSimulations);
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

    private static List<List<Simulation>> createAllSimulations(String path) throws Exception {

        //path dos arquivos de configuração da simulação
        String filesPath = path;
        String networkFilePath = filesPath + "/network";
        String simulationFilePath = filesPath + "/simulation";
        String traficFilePath = filesPath + "/traffic";
        String routesFilePath = filesPath + "/fixedRoutes";
        Util.projectPath = filesPath;
        //ler arquivos
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
        Gson gson = new GsonBuilder().create();
        NetworkConfig nc = gson.fromJson(networkConfigJSON, NetworkConfig.class);
        SimulationConfig sc = gson.fromJson(simulationConfigJSON, SimulationConfig.class);
        TrafficConfig tc = gson.fromJson(trafficConfigJSON, TrafficConfig.class);

        return createAllSimulations(nc, sc, tc);

    }

    private static List<List<Simulation>> createAllSimulations(NetworkConfig nc, SimulationConfig sc, TrafficConfig tc) throws Exception {
        //criar a lista de simulações
        List<List<Simulation>> allSimulations = new ArrayList<>(); // cada elemento deste conjunto é uma lista com 10 replicações de um mesmo ponto de carga
        int i, j;
        for (i = 0; i < sc.getLoadPoints(); i++) { //criar as simulações para cada ponto de carga
            List<Simulation> reps = new ArrayList<>();
            for (j = 0; j < sc.getReplications(); j++) { //criar as simulações para cada replicação
                Mesh m = new Mesh(nc, tc);
                incArrivedRate(m.getPairList(), i);
                Simulation s = new Simulation(sc, m, i, j);
                reps.add(s);
            }
            allSimulations.add(reps);
        }

        return allSimulations;
    }

    /**
     * este método seta o ponto de carga da simulação em cada gerador de requisições
     *
     * @param pairs
     */
    private static void incArrivedRate(Vector<Pair> pairs, int mult) {
        for (Pair pair : pairs) {
            for (RequestGenerator rg : pair.getRequestGenerators()) {
                rg.incArrivedRate(mult);
            }
        }
    }

}
