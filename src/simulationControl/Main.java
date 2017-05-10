package simulationControl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.Mesh;
import network.Pair;
import network.RequestGenerator;
import simulationControl.parsers.NetworkConfig;
import simulationControl.parsers.SimulationConfig;
import simulationControl.parsers.TrafficConfig;
import simulator.Simulation;

import java.io.File;
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
        System.out.println("Reading files");
        List<List<Simulation>> allSimulations = createAllSimulations(args);

        //agora dar o start nas simulações
        System.out.println("Starting simulations");
        SimulationManagement sm = new SimulationManagement(allSimulations, args[0]);
        sm.startSimulations();
        System.out.println("saving results");
        sm.saveResults();
        System.out.println("finish!");

    }


    private static List<List<Simulation>> createAllSimulations(String[] args) throws Exception {

        //path dos arquivos de configuração da simulação
        String filesPath = args[0];
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
