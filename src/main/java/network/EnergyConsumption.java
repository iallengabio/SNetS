package network;

import java.util.List;
import java.util.TreeSet;

import grmlsa.Route;
import grmlsa.modulation.Modulation;

/**
 * This class is responsible for the calculation of the power consumed.
 * 
 * Based on articles:
 *  - Energy Efficiency Analysis for Dynamic Routing in Optical Transport Networks (2012)
 *  - Energy efficiency analysis for flexible-grid OFDM-based optical networks (2012)
 *  - Power-Efficient Protection With Directed p-Cycles for Asymmetric Traffic in Elastic Optical Networks (2016)
 *  - Energy-efficient routing, modulation and spectrum allocation in elastic optical networks (2017)
 *  
 * Obs.: The following works present formulas similar to the works that were used for the implementation of power consumption
 *  - Traffic and Power-Aware Protection Scheme in Elastic Optical Networks (2012)
 *  - Spectrum and energy-efficient survivable routing algorithmin elastic optical network (2016)
 * 
 * @author Alexandre, Iallen
 */
public class EnergyConsumption {
	
	/**
	 * Method used by some algorithms to calculate energy consumption
	 * 
	 * @param circuit - Circuit
	 * @param cp - ControlPlane
	 * @return double - (W) - Power consumed by circuit
	 */
	public static double computePowerConsumptionOfCircuit(Circuit circuit, ControlPlane cp) {
		double pc = EnergyConsumption.computePowerConsumptionBySegment(cp, circuit, circuit.getRequiredBandwidth(), circuit.getRoute(), 0, circuit.getRoute().getNodeList().size() - 1, circuit.getModulation(), circuit.getSpectrumAssigned());
		return pc;
	}
	
	/**
	 * This method calculates the power consumption for a segment
	 * 
	 * @param circuit - Circuit
	 * @param bandwidth - double
	 * @param route - Route
	 * @param indexNodeSource - int
	 * @param indexNodeDestination - int
	 * @param modulation - Modulation
	 * @param spectrumAssigned - int[]
	 * @return double - (W) - Power consumed by segment
	 */
	public static double computePowerConsumptionBySegment(ControlPlane cp, Circuit circuit, double bandwidth, Route route, int indexNodeSource, int indexNodeDestination, Modulation modulation, int spectrumAssigned[]){
		
		double totalSlots = cp.getMesh().getLinkList().firstElement().getNumOfSlots(); //total number of slots in the links
		int quantSlotsRequeridos = spectrumAssigned[1] - spectrumAssigned[0] + 1; //number of slots required by the circuit
		
		double PCtran = computeTransponderPowerConsumption(circuit); //transponder power consumption
		double PCoxcs = computeOxcsPowerConsumption(route.getNodeList()); //OXCs power consumption
		double PCedfas = computeLinksPowerConsumption(route.getLinkList(), cp); //amplifiers power consumption
		
		double PCt = 2.0 * PCtran; //power consumption of the transmitter and receiver
		double PClinks = (quantSlotsRequeridos / totalSlots) * (PCedfas + PCoxcs); //energy consumption along the links and OXCs
		double PCtotal = PCt + PClinks; //total energy consumption of the circuit
    
		return PCtotal;
	}
	
	/**
	 * This method calculates the power consumption of network
	 * 
	 * @param cp - ControlPlane
	 * @return double - (W) - power consumption of network
	 */
	public static double computeNetworkPowerConsumption(ControlPlane cp){
		
		double PCtrans = computeTranspondersPowerConsumption(cp);
		double PCoxcs = computeOxcsPowerConsumption(cp.getMesh().getNodeList());
		double PClinks = computeLinksPowerConsumption(cp.getMesh().getLinkList(), cp);
		
		double PCtotal = PCtrans + PCoxcs + PClinks;
		
		return PCtotal;
	}
	
	/**
	 * This method calculates the power consumption of transponders
	 * 
	 * @param cp - ControlePlane
	 * @return double - (W)
	 */
	public static double computeTranspondersPowerConsumption(ControlPlane cp){
		double sumPCtrans = 0.0;
		
		TreeSet<Circuit> circuitList = cp.getConnections();
		for(Circuit circuit : circuitList){
			sumPCtrans += 2.0 * computeTransponderPowerConsumption(circuit); //power consumption of the transmitter and receiver
		}
		
		return sumPCtrans;
	}
	
	/**
	 * This method calculates the energy consumption of a transponder
	 * 
	 * @param circuit - Circuit
	 * @return double - (W)
	 */
	public static double computeTransponderPowerConsumption(Circuit circuit){
		
		double fs = circuit.getRoute().getLinkList().firstElement().getSlotSpectrumBand(); //Hz, slot frequency
		double tr = (fs * PhysicalLayer.log2(circuit.getModulation().getM())) / 1.0E+9; //Gbps, rate of transmission of a slot using the modulated format
		double PCofdm = 1.683 * tr; //power consumption of a slot using the informed modulation format
		
		int numSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1; //number of slots required by the circuit
		numSlots = numSlots - circuit.getModulation().getGuardBand(); //removing the guard band
		double PCtran = numSlots * PCofdm + 91.333; //power consumption of a transponder using the modulation format reported for a certain number of slots
		
		return PCtran;
	}
	
	/**
	 * This method calculates the energy consumption of OXCs
	 * 
	 * @param nodeList - List<Node>
	 * @return double - (W)
	 */
	public static double computeOxcsPowerConsumption(List<Node> nodeList){
		double sumPCoxcs = 0.0;
		
		for(Node node : nodeList){
			sumPCoxcs += computeOxcPowerConsumption(node); //power consumption for an OXC
		}
		
		return sumPCoxcs;
	}
	
	/**
	 * This method calculates the power consumption of an OXC
	 * 
	 * @param oxc - OXC
	 * @return double - (W)
	 */
	public static double computeOxcPowerConsumption(Node node){
		
		double n = node.getOxc().getLinksList().size(); //node degree
		double a = node.getTxs().getNumberOfTx() + node.getRxs().getNumberOfRx(); //add and drop number
		double PCoxc = n * 85.0 + a * 100.0 + 150.0; //power consumption of an OXC
		
		return PCoxc;
	}
	
	/**
	 * This method calculates or power consumption of amplifiers in the links
	 * 
	 * @param linkList - List<Link>
	 * @param cp - ControlPlane
	 * @return double - (W)
	 */
	public static double computeLinksPowerConsumption(List<Link> linkList, ControlPlane cp){
		double sumPClinks = 0.0;
		
		for(Link enlace : linkList){
			sumPClinks += computeLinkPowerConsumption(enlace, cp); // power consumption for link amplifiers
		}
		
		return sumPClinks;
	}
	
	/**
	 * This method calculates the power consumption of the amplifiers on a given link
	 * 
	 * @param link - Link
	 * @param cp - ControlPlane
	 * @return double - (W)
	 */
	public static double computeLinkPowerConsumption(Link link, ControlPlane cp){
		
		double Ns = cp.getMesh().getPhysicalLayer().getNumberOfAmplifiers(link.getDistance()); //number of amplifiers in a link
		double PClink = Ns * 100.0; //power consumption for the amplifiers in the link (Based on Power-Efficient Protection With Directed p-Cycles for Asymmetric Traffic in Elastic Optical Networks (2016))
		
		return PClink;
	}
}
