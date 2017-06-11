package simulator;

import grmlsa.GRMLSA;
import measurement.Measurements;
import network.Mesh;
import network.controlPlane.TransparentControlPlane;
import simulationControl.parsers.SimulationConfig;

import java.io.Serializable;

/**
 * This class represents a single simulation
 * 
 * @author Iallen
 */
@SuppressWarnings("serial")
public class Simulation implements Serializable {

    private Mesh mesh;
    private TransparentControlPlane controlPlane;
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
        this.measurements = new Measurements(sc.getRequests(), loadPoint, replication, mesh, sc.getActiveMetrics());
        this.mesh = mesh;
        GRMLSA grmlsa = new GRMLSA(sc.getGrooming(),sc.getIntegratedRmlsa(),sc.getRouting(),sc.getModulationSelection(),sc.getSpectrumAssignment());
        controlPlane = new TransparentControlPlane(mesh, sc.getRmlsaType(), grmlsa.instantiateGrooming(), grmlsa.instantiateIntegratedRSA(), grmlsa.instantiateRouting(), grmlsa.instantiateSpectrumAssignment());
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
     * @return replication
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
    public TransparentControlPlane getControlPlane() {
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

}
