package simulator;

import grmlsa.GRMLSA;
import measurement.Measurements;
import network.ControlPlane;
import network.Mesh;
import simulationControl.parsers.SimulationConfig;

import java.io.Serializable;

/**
 * This class represents a simulation
 * 
 * @author Iallen
 */
@SuppressWarnings("serial")
public class Simulation implements Serializable {

    private int numReply;
    private int totalNumberOfRequests;
    private int rmlsaType;
    private String routingAlgorithm;
    private String spectrumAssignmentAlgorithm;
    private String integratedRmlsaAlgorithm;
    private String groomingAlgorithm;
    private String modulationSelectionAlgorithm;

    private Mesh mesh;
    private ControlPlane controlPlane;
    private Measurements measurements;

    private int loadPoint;
    private int replication;

    /**
     * Creates a new instance of Simulation
     * 
     * @param sc SimulationConfig
     * @param mesh Mesh
     * @param loadPoint int
     * @param replication int
     * @throws Exception
     */
    public Simulation(SimulationConfig sc, Mesh mesh, int loadPoint, int replication) throws Exception {
        this.loadPoint = loadPoint;
        this.replication = replication;
        this.numReply = sc.getReplications();
        this.totalNumberOfRequests = sc.getRequests();
        this.rmlsaType = sc.getRmlsaType();
        this.routingAlgorithm = sc.getRouting();
        this.spectrumAssignmentAlgorithm = sc.getSpectrumAssignment();
        this.integratedRmlsaAlgorithm = sc.getIntegratedRmlsa();
        this.groomingAlgorithm = sc.getGrooming();
        this.modulationSelectionAlgorithm = sc.getModulationSelection();
        this.measurements = new Measurements(sc.getRequests(), loadPoint, replication, mesh, sc.getMeasuringMetrics());
        this.mesh = mesh;
        controlPlane = new ControlPlane();
        controlPlane.setMesh(mesh);
        
        switch (rmlsaType) {
            case GRMLSA.RSA_INTEGRATED:
                controlPlane.setGrmlsa(new GRMLSA(this.integratedRmlsaAlgorithm, mesh.getLinkList().get(0).getSlotSpectrumBand(), controlPlane));
                break;
            case GRMLSA.RSA_SEQUENCIAL:
                controlPlane.setGrmlsa(new GRMLSA(this.routingAlgorithm, this.spectrumAssignmentAlgorithm, mesh.getLinkList().get(0).getSlotSpectrumBand(), controlPlane));
                break;
                
            default:
                throw new Exception("unknow RMLSA type");
        }
    }

    /**
     * Returns the number of replications
     * 
     * @return the numReply
     */
    public int getNumReply() {
        return numReply;
    }
    
    /**
     * Sets the number of replications
     * 
     * @param numReply the numReply to set
     */
    public void setNumReply(int numReply) {
        this.numReply = numReply;
    }

    /**
     * Returns the total number of requests
     * 
     * @return the totalNumberOfRequests
     */
    public int getTotalNumberOfRequests() {
        return totalNumberOfRequests;
    }

    /**
     * Sets the total number of requests
     * 
     * @param totalNumberOfRequests the totalNumberOfRequests to set
     */
    public void setTotalNumberOfRequests(int totalNumberOfRequests) {
        this.totalNumberOfRequests = totalNumberOfRequests;
    }

    /**
     * Returns the type of rmlsa
     * 
     * @return the rsaType
     */
    public int getRmlsaType() {
        return rmlsaType;
    }

    /**
     * Sets the type of rmlsa
     * 
     * @param rmlsaType the rsaType to set
     */
    public void setRmlsaType(int rmlsaType) {
        this.rmlsaType = rmlsaType;
    }

    /**
     * Returns the routing algorithm
     * 
     * @return the routing Algorithm
     */
    public String getRoutingAlgorithm() {
        return routingAlgorithm;
    }

    /**
     * Sets the routing algorithm
     * 
     * @param routingAlgorithm the routingAlgorithm to set
     * @throws Exception
     */
    public void setRoutingAlgorithm(String routingAlgorithm) throws Exception {
        this.routingAlgorithm = routingAlgorithm;
    }

    /**
     * Returns the spectrum assignment algorithm
     * 
     * @return the spectrumAssignmentAlgorithm
     */
    public String getSpectrumAssignmentAlgorithm() {
        return spectrumAssignmentAlgorithm;
    }
    
    /**
     * Sets the spectrum assignment algorithm
     * 
     * @param spectrumAssignmentAlgorithm the spectrumAssignmentAlgorithm to set
     * @throws Exception
     */
    public void setSpectrumAssignmentAlgorithm(String spectrumAssignmentAlgorithm) throws Exception {
        this.spectrumAssignmentAlgorithm = spectrumAssignmentAlgorithm;
    }

    /**
     * Returns the integrated RMLSA algorithm
     * 
     * @return the integratedRmlsaAlgorithm
     */
    public String getIntegratedRmlsaAlgorithm() {
        return integratedRmlsaAlgorithm;
    }

    /**
     * Sets the integrated RMLSA algorithm
     * 
     * @param integratedRmlsaAlgorithm the integratedRmlsaAlgorithm to set
     * @throws Exception
     */
    public void setIntegratedRmlsaAlgorithm(String integratedRmlsaAlgorithm) throws Exception {
        this.integratedRmlsaAlgorithm = integratedRmlsaAlgorithm;
    }

    /**
     * Returns the grooming algorithm
     * 
     * @return the groomingAlgorithm
     */
    public String getGroomingAlgorithm() {
        return groomingAlgorithm;
    }

    /**
     * Sets the grooming algorithm
     * 
     * @param groomingAlgorithm
     */
    public void setGroomingAlgorithm(String groomingAlgorithm) {
        this.groomingAlgorithm = groomingAlgorithm;
    }

    /**
     * Returns the modulation selection algorithm
     * 
     * @return modulationSelectionAlgorithm
     */
    public String getModulationSelectionAlgorithm() {
        return modulationSelectionAlgorithm;
    }

    /**
     * Sets the modulation selection algorithm
     * 
     * @param modulationSelectionAlgorithm
     */
    public void setModulationSelectionAlgorithm(String modulationSelectionAlgorithm) {
        this.modulationSelectionAlgorithm = modulationSelectionAlgorithm;
    }

    /**
     * Returns the load point
     * 
     * @return the loadPoint
     */
    public int getLoadPoint() {
        return loadPoint;
    }

    /**
     * Sets the replication
     * 
     * @param replication
     */
    public int getReplication() {
        return replication;
    }

    /**
     * Returns the mesh of the network
     * 
     * @return the mesh
     */
    public Mesh getMesh() {
        return mesh;
    }
    
    /**
     * Sets the mesh of the network
     * 
     * @param mesh the mesh to set
     */
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    /**
     * Returns the control plane
     * 
     * @return controlPlane
     */
    public ControlPlane getControlPlane() {
        return controlPlane;
    }

    /**
     * Returns the measurements
     * 
     * @return measurements
     */
    public Measurements getMeasurements() {
        return measurements;
    }

    /**
     * Sets the measurements
     * 
     * @return measurements
     */
    public void setMeasurements(Measurements measurements) {
        this.measurements = measurements;
    }
}
