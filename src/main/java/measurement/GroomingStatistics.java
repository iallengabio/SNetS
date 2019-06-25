package measurement;

import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.GroomingStatisticsResultManager;

import java.util.HashSet;
import java.util.Iterator;

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

    /**
     * Creates a new instance of Measurement
     *
     * @param loadPoint   int
     * @param replication int
     */
    public GroomingStatistics(int loadPoint, int replication) {
        super(loadPoint, replication);
        this.resultManager = new GroomingStatisticsResultManager();
        observations = 0;
        sumReqByCirc = 0;
        maxVirtualHops = 0;
        attendedRequests = 0;
        maxReqByCirc = 0;
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
    }

    @Override
    public String getFileName() {
        return SimulationRequest.Result.FILE_GROOMING_STATISTICS;
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
