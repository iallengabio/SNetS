package simulationControl.parsers;

/**
 * This class represents the Simulation configuration file, its representation in entity form is 
 * important for the storage and transmission of this type of configuration in the JSON format.
 * 
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

    /**
     * Returns the minimum number of requests
     * 
     * @return int
     */
    public int getRequests() {
        return requests;
    }

    /**
     * Sets the minimum number of requests
     * 
     * @param requests int
     */
    public void setRequests(int requests) {
        this.requests = requests;
    }

    /**
     * Returns the RMLSA type
     * 
     * @return int
     */
    public int getRsaType() {
        return rsaType;
    }

    /**
     * Sets the RMLSA type
     * 
     * @param rsaType int
     */
    public void setRsaType(int rsaType) {
        this.rsaType = rsaType;
    }

    /**
     * Returns the routing algorithm
     * 
     * @return String
     */
    public String getRouting() {
        return routing;
    }

    /**
     * Sets the routing algorithm
     * 
     * @param routing String
     */
    public void setRouting(String routing) {
        this.routing = routing;
    }

    /**
     * Returns the spectrum assignment algorithm
     * 
     * @return String
     */
    public String getSpectrumAssignment() {
        return spectrumAssignment;
    }

    /**
     * Sets the spectrum assignment algorithm
     * 
     * @param spectrumAssignment String
     */
    public void setSpectrumAssignment(String spectrumAssignment) {
        this.spectrumAssignment = spectrumAssignment;
    }

    /**
     * Returns the integrated RMLSA algorithm
     * 
     * @return String
     */
    public String getIntegratedRsa() {
        return integratedRsa;
    }

    /**
     * Sets the integrated RMLSA algorithm
     * 
     * @param integratedRmlsa String
     */
    public void setIntegratedRsa(String integratedRsa) {
        this.integratedRsa = integratedRsa;
    }

    /**
     * Returns the modulation
     * 
     * @return String
     */
    public String getModulationSelection() {
        return modulationSelection;
    }

    /**
     * Sets the modulation
     * 
     * @param modulation String
     */
    public void setModulationSelection(String modulationSelection) {
        this.modulationSelection = modulationSelection;
    }

    /**
     * Returns the grooming algorithm
     * 
     * @return String
     */
    public String getGrooming() {
        return grooming;
    }

    /**
     * Sets the grooming algorithm
     * 
     * @param grooming String
     */
    public void setGrooming(String grooming) {
        this.grooming = grooming;
    }

    /**
     * Return the number of load points
     * 
     * @return int
     */
    public int getLoadPoints() {
        return loadPoints;
    }

    /**
     * Sets the number of load points
     * 
     * @param loadPoints int
     */
    public void setLoadPoints(int loadPoints) {
        this.loadPoints = loadPoints;
    }

    /**
     * Returns the number of replications
     * 
     * @return int
     */
    public int getReplications() {
        return replications;
    }

    /**
     * Sets the number of replications
     * 
     * @param replications int
     */
    public void setReplications(int replications) {
        this.replications = replications;
    }
}
