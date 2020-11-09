package measurement;

import network.ControlPlane;
import network.Link;
import network.Mesh;
import request.RequestForConnection;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.SpectrumUtilizationResultManager;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class stored the metrics related to the use of spectrum.
 * 
 * @author Iallen
 */
public class SpectrumUtilizationNew extends Measurement {
    public final static String SEP = "-";

    private Mesh mesh;

    private double lastInstantTime;
    private double totalNetworkOperationTime;

    private double totalSpectrumUsage;
    private HashMap<String, Double> utilizationPerLink;
    private HashMap<String, Double> numSlotsPerLink;
    private int[] unusePerSlot;

    private Integer maxSlotsByLinks;

    /**
     * Creates a new instance of Measurement
     *
     * @param loadPoint   int
     * @param replication int
     */
    public SpectrumUtilizationNew(int loadPoint, int replication, Mesh mesh) {
        super(loadPoint, replication);
        this.mesh = mesh;

        maxSlotsByLinks = this.mesh.maximumSlotsByLinks();
        unusePerSlot = new int[maxSlotsByLinks];

        utilizationPerLink = new HashMap<>();
        numSlotsPerLink = new HashMap<>();
        for(Link l : mesh.getLinkList()){
            String ln = l.getName();
            utilizationPerLink.put(ln,0.0);
            numSlotsPerLink.put(ln,(double)l.getNumOfSlots());
        }

        totalSpectrumUsage = 0.0;
        lastInstantTime = 0.0;
        totalNetworkOperationTime = 0.0;

        resultManager = new SpectrumUtilizationResultManager();
    }


    @Override
    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
        double instantTime = request.getTimeOfRequestHours();
        instantTime *= 3600.0; // Converting to seconds

        if(instantTime > totalNetworkOperationTime){
            totalNetworkOperationTime = instantTime;
        }


        double deltaTime = instantTime - lastInstantTime;

        double usedSpectrumSlots = 0;
        Iterator<Link> itLinks = cp.getMesh().getLinkList().iterator();
        while(itLinks.hasNext()){
            Link l = itLinks.next();
            double us = l.getUsedSlots();
            usedSpectrumSlots+=us; //general utilization

            Double luAt = utilizationPerLink.get(l.getName());
            utilizationPerLink.put(l.getName(),luAt+(us*deltaTime));//utilization per link

            for(int[] fsb : l.getFreeSpectrumBands(0)){//parameter guardband=0 because we dont want to stablish a new circuit. ps: this don't interfer with the guard bands of stablished circuits.
                int i;
                for (i = fsb[0] - 1; i < fsb[1]; i++) {
                    unusePerSlot[i]+=deltaTime;
                }
            }
        }

        totalSpectrumUsage += usedSpectrumSlots * deltaTime;

        lastInstantTime = instantTime;
    }

    /**
     * Returns the utilization
     *
     * @return
     */
    public double getUtilizationGen() {
        double totalSlots = 0;
        for(Link l : mesh.getLinkList()){
            totalSlots+=l.getNumOfSlots();
        }
        return this.totalSpectrumUsage / (totalSlots*totalNetworkOperationTime);
    }

    /**
     * Return the slot usage time in slots*s. Can be used to calculate the spectrum efficiency. SE=DT/SUT where DT is data transmited.
     * @return
     */
    public double getSlotUsageTime(){
        return this.totalSpectrumUsage;
    }

    /**
     * Returns the utilization for a given link passed by parameter
     *
     * @param link
     * @return
     */
    public double getUtilizationPerLink(String link) {
        return this.utilizationPerLink.get(link)/(this.numSlotsPerLink.get(link)*totalNetworkOperationTime);
    }

    /**
     * Returns the utilization for a given slot passed by parameter
     *
     * @param slot
     * @return
     */
    public double getUtilizationPerSlot(int slot) {
        int numberOfLinksWithSuchSlot=0;
        for(Link l : mesh.getLinkList()){
            if(l.getNumOfSlots()>=slot){
                numberOfLinksWithSuchSlot++;
            }
        }

        double desUt = this.unusePerSlot[slot-1] / (numberOfLinksWithSuchSlot*totalNetworkOperationTime);

        return 1 - desUt;
    }

    /**
     * Returns the maximum slots by links
     *
     * @return int
     */
    public int getMaxSlotsByLinks(){
        return maxSlotsByLinks;
    }

    @Override
    public String getFileName() {
        return SimulationRequest.Result.FILE_SPECTRUM_UTILIZATION;
    }

    public Set<String> getLinkSet() {
        return this.utilizationPerLink.keySet();
    }
}
