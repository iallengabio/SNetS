package simulationControl;

import simulationControl.parsers.SimulationRequest;

import java.io.FileNotFoundException;
import java.io.IOException;

public class SNetSAPI {

    public SimulationRequest runSimulation(SimulationRequest sr, SimulationManagement.SimulationProgressListener spl){
        SimulationManagement sm = new SimulationManagement(sr);
        sm.startSimulations(spl);
        return sr;
    }

    public SimulationRequest readSimulation(String path, String name) throws FileNotFoundException {
        SimulationFileManager sfm = new SimulationFileManager();
        return sfm.readSimulation(path,name);
    }

    public void writeSimulation(SimulationRequest sr,String path) throws IOException {
        SimulationFileManager sfm = new SimulationFileManager();
        sfm.writeSimulation(path,sr);
    }

}
