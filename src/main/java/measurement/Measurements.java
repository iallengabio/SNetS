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
			BlockingProbability probabilidadeDeBloqueioMeasurement = new BlockingProbability(loadPoint, replication);
			this.metricsList.add(probabilidadeDeBloqueioMeasurement);

		}
		if(measuringMetrics.BandwidthBlockingProbability){
			BandwidthBlockingProbability probabilidadeDeBloqueioDeBandaMeasurement = new BandwidthBlockingProbability(loadPoint, replication);
			this.metricsList.add(probabilidadeDeBloqueioDeBandaMeasurement);

		}
		if(measuringMetrics.ExternalFragmentation){
			ExternalFragmentation fragmentacaoExterna = new ExternalFragmentation(loadPoint, replication, mesh);
			this.metricsList.add(fragmentacaoExterna);

		}
		if(measuringMetrics.SpectrumUtilization){
			SpectrumUtilization utilizacaoSpectro = new SpectrumUtilization(loadPoint, replication, mesh);
			this.metricsList.add(utilizacaoSpectro);

		}
		if(measuringMetrics.RelativeFragmentation){
			RelativeFragmentation fragmentacaoRelativa = new RelativeFragmentation(loadPoint, replication, mesh);
			this.metricsList.add(fragmentacaoRelativa);

		}
		if(measuringMetrics.SpectrumSizeStatistics){
			SpectrumSizeStatistics metricsOfQoT = new SpectrumSizeStatistics(loadPoint, replication);
			this.metricsList.add(metricsOfQoT);

		}
		if(measuringMetrics.TransmittersReceiversUtilization){
			TransmittersReceiversUtilization metricsOfBAM = new TransmittersReceiversUtilization(loadPoint, replication, mesh);
			this.metricsList.add(metricsOfBAM);
		}

    }
    
    /**
     * Adds a new note for all enabled performance metrics
     * 
     * @param success boolean
     * @param request RequestForConnection
     */
    public void addNewObservation(boolean success, RequestForConnection request){
    	for(Measurement metric : metricsList){
    		metric.addNewObservation(success, request);
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
        if ((transientStep) && (numGeneratedReq >= 0.1 * numMinRequest)) {//ao atingir 10% do número de requisições da simulação o sistema deve estar estabilizado
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

	public BlockingProbability getProbabilidadeDeBloqueioMeasurement(){
		for(Measurement metric : metricsList){
			if(metric instanceof BlockingProbability){
				return (BlockingProbability)metric;
			}
		}
		return null;
	}
 	
 	public BandwidthBlockingProbability getProbabilidadeDeBloqueioDeBandaMeasurement(){
		for(Measurement metric : metricsList){
			if(metric instanceof BandwidthBlockingProbability){
				return (BandwidthBlockingProbability)metric;
			}
		}
		return null;
	}
 	
 	public ExternalFragmentation getFragmentacaoExterna(){
		for(Measurement metric : metricsList){
			if(metric instanceof ExternalFragmentation){
				return (ExternalFragmentation)metric;
			}
		}
		return null;
	}
 	
 	public RelativeFragmentation getFragmentacaoRelativa(){
		for(Measurement metric : metricsList){
			if(metric instanceof RelativeFragmentation){
				return (RelativeFragmentation)metric;
			}
		}
		return null;
	}
 	
 	public SpectrumUtilization getUtilizacaoSpectro(){
		for(Measurement metric : metricsList){
			if(metric instanceof SpectrumUtilization){
				return (SpectrumUtilization)metric;
			}
		}
		return null;
	}
 	
 	public SpectrumSizeStatistics getSpectrumSizeStatistics(){
		for(Measurement metric : metricsList){
			if(metric instanceof SpectrumSizeStatistics){
				return (SpectrumSizeStatistics)metric;
			}
		}
		return null;
	}
 	
 	public TransmittersReceiversUtilization getTransmitersReceiversUtilization(){
		for(Measurement metric : metricsList){
			if(metric instanceof TransmittersReceiversUtilization){
				return (TransmittersReceiversUtilization)metric;
			}
		}
		return null;
	}
 	
}
