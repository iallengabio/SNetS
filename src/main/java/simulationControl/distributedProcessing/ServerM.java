package simulationControl.distributedProcessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import measurement.Measurements;
import simulationControl.SimulationManagement;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.ResultManager;
import simulator.Simulation;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerM extends UnicastRemoteObject implements ServerMInterface {

    private List<ServerSInterface> lazyServers = new ArrayList<>();

    protected ServerM() throws RemoteException {
    }

    public static void runServerM(){
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
    }

    @Override
    public String simulationBundleRequest(String simReqJSON, ClientProgressCallbackInterface cpci) throws Exception {
        Gson gson = new GsonBuilder().create();
        SimulationRequest sr = gson.fromJson(simReqJSON,SimulationRequest.class);
        List<List<Simulation>> simulations = SimulationManagement.createAllSimulations(sr);
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
            double progress = (double)nSimE[0]/(double)nSim;
            cpci.updateProgress(progress);
        }

        ResultManager sm = new ResultManager(mainMeasuremens);
        sr.setResult(sm.getResults());

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
                System.out.println("Server desconectado!");
            }
        }
    }
}
