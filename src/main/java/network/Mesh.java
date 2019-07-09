package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import simulationControl.Util;
import simulationControl.parsers.NetworkConfig;
import simulationControl.parsers.OthersConfig;
import simulationControl.parsers.PhysicalLayerConfig;
import simulationControl.parsers.TrafficConfig;
import util.RandGenerator;

/**
 * This class represents the topology of the network
 * 
 * @author Iallen
 */
public class Mesh implements Serializable {

    private Vector<Node> nodeList;
    private Vector<Link> linkList;
    private Vector<Pair> pairList;
    
    private int guarBand;
    
    private PhysicalLayer physicalLayer;
    private OthersConfig othersConfig;

    private double totalPowerConsumption;
    private double totalPowerConsumptionTransponders;
    private double totalPowerConsumptionOXCs;
    private double totalPowerConsumptionAmplifiers;
    
    private List<Modulation> avaliableModulations;
    private HashMap<String, HashMap<Double, Double>> modTrDistance;

    private Util util;

    /**
     * Creates a new instance of Mesh.
     * 
     * @param nc NetworkConfig
     * @param tc TrafficConfig
     */
    public Mesh(NetworkConfig nc, TrafficConfig tc, PhysicalLayerConfig plc, OthersConfig oc, HashMap<String, HashMap<Double, Double>> modTrDistance, Util util) {
        this.util = util;
        this.guarBand = nc.getGuardBand();
        this.othersConfig = oc;
        RandGenerator randGenerator = new RandGenerator();
        HashMap<String, Node> nodesAux = new HashMap<>();
        
        // Create nodes
        this.nodeList = new Vector<>();
        for (NetworkConfig.NodeConfig nodeConf : nc.getNodes()) {
            Node aux = new Node(nodeConf.getName(), nodeConf.getTransmitters(), nodeConf.getReceivers(), nodeConf.getRegenerators(),nc.getBvtSpectralAmplitude());
            this.nodeList.add(aux);
            nodesAux.put(aux.getName(), aux);
        }

        // Create links
        this.linkList = new Vector<>();
        for (NetworkConfig.LinkConfig linkConf : nc.getLinks()) {
            Link lAux = new Link(nodesAux.get(linkConf.getSource()).getOxc(), nodesAux.get(linkConf.getDestination()).getOxc(), linkConf.getSlots(), linkConf.getSpectrum(), linkConf.getSize());
            linkList.add(lAux);
            nodesAux.get(linkConf.getSource()).getOxc().addLink(lAux);
        }

        // Create the pairs
        this.pairList = new Vector<>();
        HashMap<String, HashMap<String, Pair>> pairsAux = new HashMap<>();
        for (Node src : this.nodeList) {
            pairsAux.put(src.getName(), new HashMap<>());
            for (Node dest : this.nodeList) {
                if(!src.equals(dest)) {
                    Pair pAux = new Pair(src, dest);
                    pairList.add(pAux);
                    pairsAux.get(src.getName()).put(dest.getName(), pAux);
                }
            }
        }
        
        // Add request generators in pairs
        for (TrafficConfig.RequestGeneratorConfig rgc : tc.getRequestGenerators()) {
            Pair p = pairsAux.get(rgc.getSource()).get(rgc.getDestination());
            p.addRequestGenerator(new RequestGenerator(p, rgc.getBandwidth(), rgc.getHoldRate(), rgc.getArrivalRate(), rgc.getArrivalRateIncrease(), randGenerator));
            util.bandwidths.add(rgc.getBandwidth()); //Used to write to the archive the results of the simulation
        }
        
        // Information related to the physical layer of the network
        this.physicalLayer = new PhysicalLayer(plc, this, util);
        
        // Instance the modulation formats
        this.avaliableModulations = new ArrayList<>();
        for (NetworkConfig.ModulationConfig modConf : nc.getModulations()) {
        	Modulation mod = new Modulation(modConf.getName(), modConf.getMaxRange(), modConf.getM(), modConf.getSNR(), plc.getRateOfFEC(), linkList.get(0).getSlotSpectrumBand(), guarBand);
        	avaliableModulations.add(mod);
        }
        
        if (avaliableModulations.isEmpty()) {
        	this.avaliableModulations = ModulationSelector.configureModulations(this);
        }
        
        // Computing of the distances of the modulation formats
        if (physicalLayer.isActiveQoT()) {
        	if (modTrDistance == null) {
        		this.modTrDistance = physicalLayer.computesModulationsDistances(this, this.avaliableModulations);
        	} else {
        		this.modTrDistance = modTrDistance;
        	}
        }
    }
    
    /**
     * Returns the modulation transmission range by transmission rate
     * 
     * @param modulation Modulation
     * @param bandwidth double
     * @return double - max transmission range
     */
    public double getModulationDistanceByBandwith(Modulation modulation, double bandwidth) {
    	Double distance = null;
    	
    	if (physicalLayer.isActiveQoT()) {
    		distance = modTrDistance.get(modulation.getName()).get(bandwidth);
    		
    		if(distance == null) {
    			distance = physicalLayer.computeModulationDistanceByBandwidth(modulation, bandwidth, this);
    			modTrDistance.get(modulation.getName()).put(bandwidth, distance);
    		}
			
    	} else {
    		distance = modulation.getMaxRange();
    	}
    	
    	return distance;
    }

    /**
     * Returns a link to a given pair of source and destination nodes
     * 
     * @param source String
     * @param destination String
     * @return Link
     */
    public Link getLink(String source, String destination) {
        for (int i = 0; i < linkList.size(); i++) {
            if ((linkList.get(i).getSource().getName() == source) &&
                    (linkList.get(i).getDestination().getName() == destination)) {
                return linkList.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a Vector with all the links.
     *
     * @return Vector<Link>
     */
    public Vector<Link> getLinkList() {
        return linkList;
    }
    
    /**
     * Getter for property nodeList.
     *
     * @return Vector<Node> Vector with nodes.
     */
    public Vector<Node> getNodeList() {
        return nodeList;
    }

    /**
     * Find a Node based on the name
     *
     * @param name String
     * @return Node
     */
    public Node searchNode(String name) {
        for (int i = 0; i < this.nodeList.size(); i++) {
            Node tmp = nodeList.get(i);
            if (tmp.getName().equals(name)) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Returns the reachable nodes from a given node
     *
     * @param n Node
     * @return List<Node>
     */
    public List<Node> getAdjacents(Node n) {
        List<Node> res = new ArrayList<>();
        for (Oxc o : n.getOxc().getAllAdjacents()) {
            res.add(searchNode(o.getName()));
        }
        return res;
    }
    
    /**
     * Return the list of source and destination node pairs
     * 
     * @return Vector<Pair> the pairList
     */
    public Vector<Pair> getPairList() {
        return pairList;
    }

    /**
     * Returns the guard band
     * 
     * @return int 
     */
    public int getGuardBand() {
        return this.guarBand;
    }

    /**
     * This meter returns the maximum amount of slots in a link between all links in the network
     * 
     * @return int
     */
    public int maximumSlotsByLinks(){
    	int max = 0;
    	for (int i = 0; i < linkList.size(); i++) {
    		int num = linkList.get(i).getNumOfSlots();
    		if(num > max){
    			max = num;
    		}
    	}
    	return max;
    }
    
    /**
     * Returns the physical layer configuration of the network
     * 
     * @return PhysicalLayer
     */
    public PhysicalLayer getPhysicalLayer(){
    	return physicalLayer;
    }
    
    /**
     * Returns the others configuration
     * 
     * @return
     */
    public OthersConfig getOthersConfig() {
        return othersConfig;
    }
    
    /**
     * Returns the total power consumption
     * 
     * @return
     */
    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }
    
    /**
     * Returns the total power consumption by transponders
     * 
     * @return
     */
    public double getTotalPowerConsumptionTransponders() {
        return totalPowerConsumptionTransponders;
    }
    
    /**
     * Returns the total power consumption by OXCs
     * 
     * @return
     */
    public double getTotalPowerConsumptionOXCs() {
        return totalPowerConsumptionOXCs;
    }
    
    /**
     * Returns the total power consumption by amplifiers
     * 
     * @return
     */
    public double getTotalPowerConsumptionAmplifiers() {
        return totalPowerConsumptionAmplifiers;
    }
    
    /**
     * Computes the total power consumption
     * 
     * @param cp ControlPlane
     */
    public void computesPowerConsmption(ControlPlane cp){
    	totalPowerConsumptionTransponders = EnergyConsumption.computeTranspondersPowerConsumption(cp);
    	totalPowerConsumptionOXCs = EnergyConsumption.computeOxcsPowerConsumption(nodeList);
    	totalPowerConsumptionAmplifiers = EnergyConsumption.computeLinksPowerConsumption(linkList, cp);
    	
    	totalPowerConsumption = totalPowerConsumptionTransponders + totalPowerConsumptionOXCs + totalPowerConsumptionAmplifiers;
    }
    
    /**
     * Returns the avaliableModulations
     * 
     * @return List<Modulation> avaliableModulations
     */
	public List<Modulation> getAvaliableModulations() {
		return avaliableModulations;
	}
	
	/**
	 * Sets the avaliableModulations
	 * 
	 * @param avaliableModulations List<Modulation>
	 */
	public void setAvaliableModulations(List<Modulation> avaliableModulations) {
		this.avaliableModulations = avaliableModulations;
	}

	/**
	 * Return the modTrDistance
	 * 
	 * @return HashMap<Modulation, HashMap<Double, Double>>
	 */
	public HashMap<String, HashMap<Double, Double>> getModTrDistance() {
		return modTrDistance;
	}

    public Util getUtil() {
        return util;
    }
}
