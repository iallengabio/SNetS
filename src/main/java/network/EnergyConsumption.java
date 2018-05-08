package network;

import grmlsa.Route;
import grmlsa.modulation.Modulation;

/**
 * This class is responsible for the calculation of the power consumed by a circuit.
 * 
 * @author Alexandre
 */
public class EnergyConsumption {

	/**
	 * Based on articles:
	 *  - Energy Efficiency Analysis for Dynamic Routing in Optical Transport Networks (2012)
	 *  - Energy efficiency analysis for flexible-grid OFDM-based optical networks (2012)
	 *  
	 * Obs.: The following works present formulas similar to the works that were used for the implementation of power consumption
	 *  - Traffic and Power-Aware Protection Scheme in Elastic Optical Networks (2012)
	 *  - Spectrum and energy-efficient survivable routing algorithmin elastic optical network (2016)
	 * 
	 * @param bandwidth - double
	 * @param route - Route
	 * @param sourceNodeIndex - int
	 * @param destinationNodeIndex - int
	 * @param modulation - Modulation
	 * @param spectrumAssigned - int[]
	 * @return double - (W) - Power consumed by the request in the informed segment
	 */
	public static double computePowerConsumptionBySegment(ControlPlane cp, double bandwidth, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[]){
		
		Node noOrigem = null;
		Node noDestino = null;
		Link enlace = null;
		
		double L = cp.getMesh().getPhysicalLayer().getSpanLength(); // Size of a span
		int n = 0; // Number of links connected to node (degree of node)
		
		double totalSlots = route.getLinkList().firstElement().getNumOfSlots(); // Total number of slots in the links
		int quantSlotsRequeridos = modulation.requiredSlots(bandwidth); // Number of slots required by the request
		double fs = route.getLinkList().firstElement().getSlotSpectrumBand(); //Hz, Frequency of transmission of a slot
		double tr = (fs * PhysicalLayer.log2(modulation.getM())) / 1000000000.0; //Gbps, Transmission rate of a slot using the modulation format reported
		
		double PCofdm = 1.25 * tr + 31.5; // Power consumption of a slot using the informed modulation format
		double PCoxcs = 0.0; // Power consumption of OXCs
		double PCedfas = 0.0; // Power consumption of amplifiers
		
		for(int i = sourceNodeIndex; i < destinationNodeIndex; i++){
			noOrigem = route.getNode(i);
			noDestino = route.getNode(i + 1);
			enlace = noOrigem.getOxc().linkTo(noDestino.getOxc());
			
			n = noOrigem.getOxc().getLinksList().size();
			PCoxcs += n * 85.0 + 150.0; // Power consumption for an OXC
			
			double Ns = PhysicalLayer.roundUp(enlace.getDistance() / L); // Number of spans
			PCedfas += 30.0 + Ns * 140.0; // Power consumption for amplifiers in the link
		}
		n = noDestino.getOxc().getLinksList().size();
		PCoxcs += n * 85.0 + 150.0; // Power consumption for an OXC
		
		double PCt = quantSlotsRequeridos * PCofdm; // Power consumption of transponders
		double PClinks = (quantSlotsRequeridos / totalSlots) * (PCedfas + PCoxcs); // Power consumption along the links
		double PCtotal = PCt + PClinks; // Total power consumption by request
		
		return PCtotal;
	}

}
