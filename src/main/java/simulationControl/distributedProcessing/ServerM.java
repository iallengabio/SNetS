package simulationControl.distributedProcessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import measurement.Measurement;
import measurement.Measurements;
import simulationControl.Main;
import simulationControl.SimulationManagement;
import simulationControl.parsers.SimulationRequest;
import simulator.Simulation;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerM extends UnicastRemoteObject implements ServerMInterface {

    private List<ServerSInterface> lazyServers = new ArrayList<>();
    private List<ServerSInterface> busyServers = new ArrayList<>();

    protected ServerM() throws RemoteException {
    }

    public static void main(String[] args){
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            // Creates an object of the HelloServer class.
            ServerMInterface obj = new ServerM();
            // Bind this object instance to the name "HelloServer".
            Naming.rebind("ServerM", obj);
            System.out.println("ServerM on");
        }
        catch (Exception ex) {
            System.out.println("error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void register(ServerSInterface server) throws RemoteException {
        System.out.println(server.getName() + " registered");
        lazyServers.add(server);

        /*ExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        server.isAlive();
                        System.out.println("está vivo");
                        Thread.sleep(1000);
                    } catch (RemoteException e) {
                        System.out.println("está morto");
                        break;
                        //e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        server.pseudoSimulation(200);*/

    }

    @Override
    public String simulationBundleRequest(String simReqJSON) throws Exception {
        Gson gson = new GsonBuilder().create();
        SimulationRequest sr = gson.fromJson(simReqJSON,SimulationRequest.class);
        List<List<Simulation>> simulations = Main.createAllSimulations(sr);
        Queue<Simulation> simulationQueue = new LinkedList<>();
        List<List<Measurements>> mainMeasuremens;
        mainMeasuremens = new ArrayList<>();
        for(List<Simulation> loadPoint : simulations){
            List<Measurements> aux = new ArrayList<>();
            mainMeasuremens.add(aux);
            for(Simulation replication : loadPoint){
                aux.add(null);
                simulationQueue.add(replication);
            }
        }
        ExecutorService executor = Executors.newScheduledThreadPool(100);

        int nSim = simulationQueue.size();
        final int[] nSimE = {0};
        while(nSimE[0] < nSim){
            if(!simulationQueue.isEmpty() && !lazyServers.isEmpty()){
                Simulation p = simulationQueue.poll();
                ServerSInterface server = lazyServers.remove(0);
                executor.execute(new SimulationTask(server,mainMeasuremens,nSimE,p,simulationQueue));
            }
            Thread.sleep(200);
        }

        SimulationManagement sm = new SimulationManagement(simulations, mainMeasuremens);

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

        return gson.toJson(sr);
    }

    public class SimulationTask implements Runnable{

        private ServerSInterface server;
        private List<List<Measurements>> mainMeasuremens;
        private int[] nSimE;
        private Simulation p;
        Queue<Simulation> simulationQueue;

        public SimulationTask(ServerSInterface server, List<List<Measurements>> mainMeasuremens, int[] nSimE, Simulation p, Queue<Simulation> simulationQueue) {
            this.server = server;
            this.mainMeasuremens = mainMeasuremens;
            this.nSimE = nSimE;
            this.p = p;
            this.simulationQueue = simulationQueue;
        }

        @Override
        public void run() {
            try {
                Measurements res =  server.simulate(p);
                mainMeasuremens.get(p.getLoadPoint()).set(p.getReplication(),res);
                nSimE[0]++;
                lazyServers.add(server);
            }catch (Exception ex){
                simulationQueue.add(p);
                ex.printStackTrace();
            }
        }
    }
}
