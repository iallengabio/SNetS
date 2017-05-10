package simulator;

import grmlsa.GRMLSA;
import measurement.Measurements;
import network.ControlPlane;
import network.Mesh;
import simulationControl.parsers.SimulationConfig;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Simulation implements Serializable {


    private int numReply;
    private int totalNumberOfRequests;
    private int rsaType;
    private String rountingAlgorithm;
    private String spectrumAssignmentAlgorithm;
    private String integratedRSAAlgorithm;
    private String groomingAlgorithm;
    private String modulationLevelAlgorithm;

    private Mesh mesh;
    private ControlPlane controlPlane;
    private Measurements measurements;


    public Simulation(SimulationConfig sc, Mesh mesh, int loadPoint, int replication) throws Exception {
        this.numReply = sc.getReplications();
        this.totalNumberOfRequests = sc.getRequests();
        this.rsaType = sc.getRsaType();
        this.rountingAlgorithm = sc.getRouting();
        this.spectrumAssignmentAlgorithm = sc.getSpectrumAssignment();
        this.integratedRSAAlgorithm = sc.getIntegratedRsa();
        this.groomingAlgorithm = sc.getGrooming();
        this.modulationLevelAlgorithm = sc.getModulation();
        this.measurements = new Measurements(sc.getRequests(),loadPoint,replication,mesh);
        this.mesh = mesh;
        controlPlane = new ControlPlane();
        controlPlane.setMesh(mesh);
        switch (rsaType) {
            case GRMLSA.RSA_INTEGRATED:
                controlPlane.setRsa(new GRMLSA(this.integratedRSAAlgorithm, mesh.getLinkList().get(0).getSlotSpectrumBand(), controlPlane));
                break;
            case GRMLSA.RSA_SEQUENCIAL:
                controlPlane.setRsa(new GRMLSA(this.rountingAlgorithm, this.spectrumAssignmentAlgorithm, mesh.getLinkList().get(0).getSlotSpectrumBand(), controlPlane));
                break;
        }

    }


    /**
     * @return the numReply
     */
    public int getNumReply() {
        return numReply;
    }


    /**
     * @param numReply the numReply to set
     */
    public void setNumReply(int numReply) {
        this.numReply = numReply;
    }


    /**
     * @return the totalNumberOfRequests
     */
    public int getTotalNumberOfRequests() {
        return totalNumberOfRequests;
    }


    /**
     * @param totalNumberOfRequests the totalNumberOfRequests to set
     */
    public void setTotalNumberOfRequests(int totalNumberOfRequests) {
        this.totalNumberOfRequests = totalNumberOfRequests;
    }


    /**
     * @return the rsaType
     */
    public int getRsaType() {
        return rsaType;
    }


    /**
     * @param rsaType the rsaType to set
     */
    public void setRsaType(int rsaType) {
        this.rsaType = rsaType;
    }


    /**
     * @return the rountingAlgorithm
     */
    public String getRountingAlgorithm() {
        return rountingAlgorithm;
    }


    /**
     * @param rountingAlgorithm the rountingAlgorithm to set
     * @throws Exception
     */
    public void setRountingAlgorithm(String rountingAlgorithm) throws Exception {
        this.rountingAlgorithm = rountingAlgorithm;

    }


    /**
     * @return the spectrumAssignmentAlgorithm
     */
    public String getSpectrumAssignmentAlgorithm() {
        return spectrumAssignmentAlgorithm;
    }


    /**
     * @param spectrumAssignmentAlgorithm the spectrumAssignmentAlgorithm to set
     * @throws Exception
     */
    public void setSpectrumAssignmentAlgorithm(String spectrumAssignmentAlgorithm) throws Exception {
        this.spectrumAssignmentAlgorithm = spectrumAssignmentAlgorithm;

    }


    /**
     * @return the integratedRSAAlgorithm
     */
    public String getIntegratedRSAAlgorithm() {
        return integratedRSAAlgorithm;
    }


    /**
     * @param integratedRSAAlgorithm the integratedRSAAlgorithm to set
     * @throws Exception
     */
    public void setIntegratedRSAAlgorithm(String integratedRSAAlgorithm) throws Exception {

        this.integratedRSAAlgorithm = integratedRSAAlgorithm;
    }

    public String getGroomingAlgorithm() {
        return groomingAlgorithm;
    }

    public void setGroomingAlgorithm(String groomingAlgorithm) {
        this.groomingAlgorithm = groomingAlgorithm;
    }

    public String getModulationLevelAlgorithm() {
        return modulationLevelAlgorithm;
    }

    public void setModulationLevelAlgorithm(String modulationLevelAlgorithm) {
        this.modulationLevelAlgorithm = modulationLevelAlgorithm;
    }

    /**
     * @return the mesh
     */
    public Mesh getMesh() {
        return mesh;
    }


    /**
     * @param mesh the mesh to set
     */
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public ControlPlane getControlPlane() {
        return controlPlane;
    }

    public Measurements getMeasurements() {
        return measurements;
    }

    public void setMeasurements(Measurements measurements) {
        this.measurements = measurements;
    }
}
