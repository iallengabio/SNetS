package measurement;

import java.util.*;

import network.*;
import request.RequestForConnection;
import simulationControl.parsers.SimulationConfig;

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
     * The network mesh
     */
    private Mesh mesh;
    
    /**
     * List of performance metrics
     */
    private List<Measurement> metricsList;

	/**
	 * Ajust this!
	 */
	private ConsumedEnergy consumedEnergyMetric;
	private SpectrumUtilizationNew spectrumUtilizationMetric;

    /**
     * List of metrics to be considered during the simulation
     */
    private SimulationConfig.Metrics measuringMetrics;


	/**
     * Creates a new instance of Measurements
     * 
     * @param numMinRequest int
     * @param loadPoint int
     * @param replication int
     * @param mesh Mesh
     */
    public Measurements(int numMinRequest, int loadPoint, int replication, Mesh mesh, SimulationConfig.Metrics measuringMetrics) {
        this.loadPoint = loadPoint;
        this.replication = replication;
    	this.transientStep = true;
        this.numMinRequest = numMinRequest;  
        this.mesh = mesh;
        
        this.measuringMetrics = measuringMetrics;
        
        initializeMetrics(mesh);
    }
    
    /**
     * Initialize the metrics
     * 
     * @param mesh Mesh
     */
    private void initializeMetrics(Mesh mesh){
    	this.numGeneratedReq = 0.0;
    	
        this.metricsList = new ArrayList<Measurement>();
        
        // Activates the metrics set up in the SimulationConfig file
		if(measuringMetrics.BlockingProbability){
			BlockingProbability probabilidadeDeBloqueio = new BlockingProbability(loadPoint, replication, mesh.getUtil());
			this.metricsList.add(probabilidadeDeBloqueio);
		}
		if(measuringMetrics.BandwidthBlockingProbability){
			BandwidthBlockingProbability probabilidadeDeBloqueioDeBanda = new BandwidthBlockingProbability(loadPoint, replication, mesh.getUtil());
			this.metricsList.add(probabilidadeDeBloqueioDeBanda);
		}
		if(measuringMetrics.ExternalFragmentation){
			ExternalFragmentation fragmentacaoExterna = new ExternalFragmentation(loadPoint, replication);
			this.metricsList.add(fragmentacaoExterna);
		}
		if(measuringMetrics.SpectrumUtilization){
			SpectrumUtilizationNew utilizacaoSpectro = new SpectrumUtilizationNew(loadPoint, replication, mesh);
			this.spectrumUtilizationMetric = utilizacaoSpectro; //because this metric needs to be updated when requests holds
			this.metricsList.add(utilizacaoSpectro);
		}
		if(measuringMetrics.RelativeFragmentation){
			RelativeFragmentation fragmentacaoRelativa = new RelativeFragmentation(loadPoint, replication);
			this.metricsList.add(fragmentacaoRelativa);
		}
		if(measuringMetrics.SpectrumSizeStatistics){
			SpectrumSizeStatistics spectrumSizeStatistics = new SpectrumSizeStatistics(loadPoint, replication);
			this.metricsList.add(spectrumSizeStatistics);
		}
		if(measuringMetrics.TransmittersReceiversRegeneratorsUtilization){
			TransmittersReceiversRegeneratorsUtilization transmittersReceiversUtilization = new TransmittersReceiversRegeneratorsUtilization(loadPoint, replication);
			this.metricsList.add(transmittersReceiversUtilization);
		}
		if(measuringMetrics.ModulationUtilization){
			ModulationUtilization modulationUtilization = new ModulationUtilization(loadPoint, replication, mesh.getUtil());
			this.metricsList.add(modulationUtilization);
		}
		if(measuringMetrics.ConsumedEnergy){
			ConsumedEnergy consumedEnergy = new ConsumedEnergy(loadPoint,replication);
			this.consumedEnergyMetric = consumedEnergy; //because this metric needs to be updated when requests holds
			this.metricsList.add(consumedEnergy);
		}
		if(measuringMetrics.GroomingStatistics){
			GroomingStatistics groomingStatistics = new GroomingStatistics(loadPoint,replication);
			this.metricsList.add(groomingStatistics);
		}
    }
    
    /**
     * Adds a new note for all enabled performance metrics
     * 
     * @param cp ControlPlane
     * @param success boolean
     * @param request RequestForConnection
     */
    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request){
    	for(Measurement metric : metricsList){
    		metric.addNewObservation(cp, success, request);
    	}
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
     */
    public void transientStepVerify() {
    	// // when it reaches 10% of the number of simulation requests, the system must be stabilized
        if (transientStep && (numGeneratedReq >= 0.1 * numMinRequest)) {
            this.transientStep = false;

            initializeMetrics(mesh);
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
     * Request the list of performance metrics
     * 
 	 * @return List<Measurement> metrics
 	 */
 	public List<Measurement> getMetrics(){
 		return metricsList;
 	}
 	
 	/**
	 * @return the measuringMetrics
	 */
	public SimulationConfig.Metrics getMeasuringMetrics() {
		return measuringMetrics;
	}
	
	public ConsumedEnergy getConsumedEnergyMetric() {
		return consumedEnergyMetric;
	}

	public SpectrumUtilizationNew getSpectrumUtilization(){
		return spectrumUtilizationMetric;
	}

	public void setConsumedEnergyMetric(ConsumedEnergy consumedEnergyMetric) {
		this.consumedEnergyMetric = consumedEnergyMetric;
	}
}
