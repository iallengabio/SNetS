package simulationControl.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import grmlsa.GRMLSA;
import simulator.Simulation;

/**
 * Created by Iallen on 04/05/2017.
 */
public class TempMain {

    public static void main(String args[]){
        System.out.println("Hello gson");
        Gson gson = new GsonBuilder().create();

        NetworkConfig nc = new NetworkConfig();
        nc.setGuardBand(1);
        nc.getNodes().add(new NetworkConfig.NodeConfig("1",100,100));
        nc.getNodes().add(new NetworkConfig.NodeConfig("2",100,100));
        nc.getLinks().add(new NetworkConfig.LinkConfig("1","2",401,12500000000.0, 100));
        nc.getLinks().add(new NetworkConfig.LinkConfig("2","1",401,12500000000.0, 100));

        String jnet = gson.toJson(nc);
        System.out.println(jnet);

        SimulationConfig sc = new SimulationConfig();
        sc.setGrooming(GRMLSA.GROOMING_OPT_NOTRAFFICGROOMING);
        sc.setIntegratedRsa(null);
        sc.setModulation(null);
        sc.setRouting(GRMLSA.ROUTING_DJK);
        sc.setSpectrumAssignment(GRMLSA.SPECTRUM_ASSIGNMENT_FISTFIT);
        sc.setRsaType(GRMLSA.RSA_SEQUENCIAL);
        sc.setLoadPoints(3);
        sc.setReplications(10);
        sc.setRequests(100000);

        String jsim = gson.toJson(sc);
        System.out.println(jsim);

        TrafficConfig tc = new TrafficConfig();
        tc.getRequestGenerators().add(new TrafficConfig.RequestGeneratorConfig("1","2",10000000000.0, 4.0, 2.0, 1.0));
        tc.getRequestGenerators().add(new TrafficConfig.RequestGeneratorConfig("2","1",10000000000.0, 4.0, 2.0, 1.0));

        String jtraf = gson.toJson(tc);
        System.out.println(jtraf);


    }
}
