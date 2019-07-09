package simulationControl;

import measurement.Measurements;
import network.Mesh;
import network.Pair;
import network.RequestGenerator;
import simulationControl.parsers.*;
import simulationControl.resultManagers.ResultManager;
import simulator.Simulation;
import simulator.Simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is responsible for managing the executions of the simulations
 * 
 * @author Iallen
 */
public class SimulationManagement {

    private SimulationRequest simulationRequest;

    private List<List<Simulation>> simulations;
    private int done;
    private int numOfSimulations;
    /**
     * Stores the results for all points with all replicas
     */
    private List<List<Measurements>> mainMeasuremens;

    /**
     * Creates a new instance of SimulationManagement
     *
     */
    public SimulationManagement(SimulationRequest simulationRequest) {
        this.simulationRequest = simulationRequest;
        //numberOfThreads = simulationRequest.getSimulationConfig().getThreads();
        this.simulations = createAllSimulations(simulationRequest);
        done = 0;
        numOfSimulations = 0;
        mainMeasuremens = new ArrayList<>();
        
        for(List<Simulation> loadPoint : simulations){
            List<Measurements> aux = new ArrayList<>();
            mainMeasuremens.add(aux);
            for(int replication = 0; replication < loadPoint.size(); replication++){
                aux.add(null);
                numOfSimulations++;
            }
        }
    }

    /**
     * This method creates all simulations of an experiment
     *
     */
    public static List<List<Simulation>> createAllSimulations(SimulationRequest sr){
        Util util = new Util();

        NetworkConfig nc = sr.getNetworkConfig();
        SimulationConfig sc = sr.getSimulationConfig();
        TrafficConfig tc = sr.getTrafficConfig();
        PhysicalLayerConfig plc = sr.getPhysicalLayerConfig();
        OthersConfig oc = sr.getOthersConfig();

        System.out.println("Calculating the modulations transmission ranges");
        Mesh meshTemp = new Mesh(nc, tc, plc, oc, null,util);

        // Create list of simulations
        List<List<Simulation>> allSimulations = new ArrayList<>(); // Each element of this set is a list with 10 replications from the same load point
        int i, j;
        for (i = 0; i < sc.getLoadPoints(); i++) { // Create the simulations for each load point
            List<Simulation> reps = new ArrayList<>();
            for (j = 0; j < sc.getReplications(); j++) { // Create the simulations for each replication
                Mesh m = new Mesh(nc, tc, plc, oc, meshTemp.getModTrDistance(),util);
                incArrivedRate(m.getPairList(), i);
                Simulation s = new Simulation(sc, m, i, j, util);
                reps.add(s);
            }
            allSimulations.add(reps);
        }

        util.pairs.addAll(allSimulations.get(0).get(0).getMesh().getPairList());

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

    /**
     * Runs the simulations centrally
     */
    public void startSimulations(SimulationProgressListener simulationProgressListener) {


        ExecutorService executor = Executors.newScheduledThreadPool(simulationRequest.getSimulationConfig().getThreads());
        done = 0;
        for(List<Simulation> loadPoint : simulations){
            for(Simulation replication : loadPoint){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Simulator simulator = new Simulator(replication);
                            mainMeasuremens.get(replication.getLoadPoint()).set(replication.getReplication(), simulator.start());
                            done++;
                            simulationProgressListener.onSimulationProgressUpdate((double) done / numOfSimulations);
                        }catch (Exception ex){
                            ex.printStackTrace();
                            executor.shutdown();
                        }
                    }
                });
            }
        }
        while(done<numOfSimulations){ //wait untill all simulations have done
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        ResultManager rm = new ResultManager(mainMeasuremens);
        simulationRequest.setResult(rm.getResults());
        simulationProgressListener.onSimulationFinished();
    }

    public static interface SimulationProgressListener {

        public void onSimulationProgressUpdate(double progress);

        public void onSimulationFinished();
    }

}
