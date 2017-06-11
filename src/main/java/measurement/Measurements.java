package measurement;

import java.util.*;

import network.*;
import java.io.Serializable;

/**
 * This class manages the performance metrics used in the simulations.
 * 
 * @author Iallen
 */
@SuppressWarnings("serial")
public class Measurements implements Serializable {

	/**
     * Minimum number of requests to be generated
     */
    private int numMinRequest;

    /**
     * Indicates whether the simulation is in the transient phase or not
     */
    private boolean transientStep;
    
    /**
     * Used to count the number of requests generated until then, objective of verifying the transient state
     */
	private double numGeneratedReq;  
	
	/**
     * Replication number
     */
    private int replication;
    
    /**
     * Loading point
     */
    private int loadPoint;
    
    /**
     * Calculates the blocking probability of the circuits
     */
    private BlockingProbability blockingProbabilityMeasurement;    
    
    /**
     * Calculates the bandwidth blocking probability of ghe circuits
     */
    private BandwidthBlockingProbability bandwidthBlockingProbabilityMeasurement;
    
    /**
     * Calculates the external fragmentation
     */
    private ExternalFragmentation externalFragmentation;
    
    /**
     * Calculates the relative fragmentation
     */
    private RelativeFragmentation relativeFragmentation;
    
    /**
     * Calculates metrics for spectrum usage
     */
    private SpectrumUtilization spectrumUtilization;
    
    /**
     * Currently used only to analyze the percentage of generated requests that require 
     * each free spectrum band size
     */
    private SpectrumSizeStatistics spectrumSizeStatistics;
    
    /**
     * Calculates metrics for the use of transmitters and receivers
     */
    private TransmittersReceiversUtilization transmitersReceiversUtilization;

    /**
     * The network mesh
     */
    private Mesh mesh;
    
    /**
     * List of performance metrics
     */
    List<Measurement> metricsList;
    
    /**
     * List of metrics to be considered during the simulation
     */
    ArrayList<String> measuringMetrics;
    
    /**
     * Creates a new instance of Measurements
     * 
     * @param numMinRequest int
     * @param loadPoint int
     * @param replication int
     * @param mesh Mesh
     */
    public Measurements(int numMinRequest, int loadPoint, int replication, Mesh mesh) {
        this.loadPoint = loadPoint;
        this.replication = replication;
    	this.transientStep = true;
        this.numMinRequest = numMinRequest;  
        this.mesh = mesh;
        
        initializeMetrics(mesh);
    }
    
    /**
     * Initialize the metrics
     * 
     * @param mesh Mesh
     */
    private void initializeMetrics(Mesh mesh){
    	this.numGeneratedReq = 0.0;
        this.blockingProbabilityMeasurement = new BlockingProbability(loadPoint, replication); 
        this.bandwidthBlockingProbabilityMeasurement = new BandwidthBlockingProbability(loadPoint, replication);
        this.externalFragmentation = new ExternalFragmentation(loadPoint, replication, mesh);
        this.spectrumUtilization = new SpectrumUtilization(loadPoint, replication, mesh);
        this.relativeFragmentation = new RelativeFragmentation(loadPoint, replication, mesh);
        this.spectrumSizeStatistics = new SpectrumSizeStatistics(loadPoint, replication);
        this.transmitersReceiversUtilization = new TransmittersReceiversUtilization(loadPoint, replication, mesh);
    }

    /**
     * Returns the replication
     * 
     * @return int
     */
    public int getReplication() {
        return this.replication;
    }

    /**
     * Increases the number of generated requests.
     */
    public void incNumGeneratedReq() {
        this.numGeneratedReq++;      
    }
    
    /**
     * Verify the transient phase
     * 
     * @param nodeList Vector<Node>
     */
    public void transientStepVerify(Vector<Node> nodeList) {
        if ((transientStep) && (numGeneratedReq >= 0.1 * numMinRequest)) {//ao atingir 10% do número de requisições da simulação o sistema deve estar estabilizado
            this.transientStep = false;

            initializeMetrics(mesh);
            numGeneratedReq = 0;
        }
    }

    /**
     * Responsible for determining the end of the simulation.
     * If it returns true no event should be scheduled, but those already scheduled will be performed.
     * 
     * @return boolean
     */
    public boolean finished() {
        if (this.numGeneratedReq >= this.numMinRequest) {
            return true;
        }
        return false;
    }

    /**
     * Returns the blocking probability measurement
     * 
	 * @return the blockingProbabilityMeasurement
	 */
	public BlockingProbability getProbabilidadeDeBloqueioMeasurement() {
		return blockingProbabilityMeasurement;
	}

	/**
	 * Returns the bandwidth blocking probability measurement
	 * 
	 * @return the bandwidthBlockingProbabilityMeasurement
	 */
	public BandwidthBlockingProbability getProbabilidadeDeBloqueioDeBandaMeasurement() {
		return bandwidthBlockingProbabilityMeasurement;
	}

	/**
	 * Returns the external fragmentation
	 * 
	 * @return the externalFragmentation
	 */
	public ExternalFragmentation getFragmentacaoExterna() {
		return externalFragmentation;
	}
	
	/**
	 * Returns the relative fragmentation
	 * 
	 * @return the relativeFragmentation
	 */
	public RelativeFragmentation getFragmentacaoRelativa() {
		return relativeFragmentation;
	}

	/**
	 * Returns the spectrum utilization
	 * 
	 * @return the SpectrumUtilization
	 */
	public SpectrumUtilization getUtilizacaoSpectro() {
		return spectrumUtilization;
	}

	/**
	 * Returns the spectrum size statistics
	 * 
	 * @return spectrumSizeStatistics
	 */
	public SpectrumSizeStatistics getSpectrumSizeStatistics() {
		return spectrumSizeStatistics;
	}

	/**
	 * Returns the transmitters receivers utilization
	 * 
	 * @return transmitersReceiversUtilization
	 */
	public TransmittersReceiversUtilization getTransmitersReceiversUtilization() {
		return transmitersReceiversUtilization;
	}
	
}
