package simulationControl.parsers;

/**
 * Esta classe representa o arquivo de configuração Simulation, sua representação na forma de entidade é importante para a armazenação e transmissão deste tipo de configução no formato JSON.
 * Created by Iallen on 04/05/2017.
 */
public class SimulationConfig {

    private int requests;
    private int rsaType;
    private String routing;
    private String spectrumAssignment;
    private String integratedRsa;
    private String modulationSelection;
    private String grooming;
    private int loadPoints;
    private int replications;

    public int getRequests() {
        return requests;
    }

    public void setRequests(int requests) {
        this.requests = requests;
    }

    public int getRsaType() {
        return rsaType;
    }

    public void setRsaType(int rsaType) {
        this.rsaType = rsaType;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public String getSpectrumAssignment() {
        return spectrumAssignment;
    }

    public void setSpectrumAssignment(String spectrumAssignment) {
        this.spectrumAssignment = spectrumAssignment;
    }

    public String getIntegratedRsa() {
        return integratedRsa;
    }

    public void setIntegratedRsa(String integratedRsa) {
        this.integratedRsa = integratedRsa;
    }

    public String getModulationSelection() {
        return modulationSelection;
    }

    public void setModulationSelection(String modulationSelection) {
        this.modulationSelection = modulationSelection;
    }

    public String getGrooming() {
        return grooming;
    }

    public void setGrooming(String grooming) {
        this.grooming = grooming;
    }

    public int getLoadPoints() {
        return loadPoints;
    }

    public void setLoadPoints(int loadPoints) {
        this.loadPoints = loadPoints;
    }

    public int getReplications() {
        return replications;
    }

    public void setReplications(int replications) {
        this.replications = replications;
    }
}
