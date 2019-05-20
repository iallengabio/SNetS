package measurement;

import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;
import simulationControl.resultManagers.GroomingStatisticsResultManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * This measurement computes grooming statistics like rate of requests attended by circuit and mean of virtual hops.
 */
public class GroomingStatistics extends Measurement {

    private int observations;
    private double sumReqByCirc;
    private int maxVirtualHops;
    private double sumVirtualHops;
    private int attendedRequests;
    private int maxReqByCirc;
    
    private double averageActiveCircuits;
    private double maximumActiveCircuits;

    /**
     * Creates a new instance of Measurement
     *
     * @param loadPoint   int
     * @param replication int
     */
    public GroomingStatistics(int loadPoint, int replication) {
        super(loadPoint, replication);
        this.resultManager = new GroomingStatisticsResultManager();
        fileName = "_GroomingStatistics.csv";
        observations = 0;
        sumReqByCirc = 0.0;
        maxVirtualHops = 0;
        sumVirtualHops = 0.0;
        attendedRequests = 0;
        maxReqByCirc = 0;
        
        averageActiveCircuits = 0.0;
        maximumActiveCircuits = 0.0;
    }

    @Override
    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
        observations++;
        sumReqByCirc += computeReqByCirc(cp);
        if(success){
            attendedRequests++;
            sumVirtualHops += request.getCircuits().size();
            if(request.getCircuits().size()>maxVirtualHops){
                maxVirtualHops = request.getCircuits().size();
            }
        }
        
        TreeSet<Circuit> circuitList = cp.getConnections();
		averageActiveCircuits += circuitList.size();
		if(circuitList.size() > maximumActiveCircuits) {
			maximumActiveCircuits = circuitList.size();
		}
    }

    public double getReqByCirc(){
        return sumReqByCirc/(double)observations;
    }

    public double getVirtualHops(){
        return sumVirtualHops/(double)attendedRequests;
    }
    
    public int getMaxVirtualHops() {
        return maxVirtualHops;
    }

    public int getMaxReqByCirc() {
        return maxReqByCirc;
    }
    
    public double getAverageActiveCircuits() {
    	return averageActiveCircuits/(double)observations;
    }
    
    public double getMaximumActiveCircuits() {
    	return maximumActiveCircuits;
    }

    private double computeReqByCirc(ControlPlane cp){
        Iterator<Circuit> iterator = cp.getConnections().iterator();
        HashSet<RequestForConnection> activeClients = new HashSet<>(); //HashSet will ignore same requests in more than one circuit.
        while(iterator.hasNext()){
            Circuit next = iterator.next();
            activeClients.addAll(next.getRequests());
            if(next.getRequests().size()>maxReqByCirc){
                maxReqByCirc = next.getRequests().size();
            }
        }
        
        double acli = activeClients.size();
        double acirc = cp.getConnections().size();
        
        if(Double.isNaN(acli/acirc)){
            return 0.0;
        }
        
        return acli/acirc;
    }
}
