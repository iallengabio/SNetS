package network;

import grmlsa.Route;
import grmlsa.modulation.Modulation;

import java.util.List;
import java.util.TreeSet;




/**
 * Artgo: Energy Efficiency Analysis for Dynamic Routing in Optical Transport Networks (2012)
 *        Energy efficiency analysis for flexible-grid OFDM-based optical networks (2012)
 *        Energy-efficient routing, modulation and spectrum allocation in elastic optical networks (2017)
 *
 * Obs.: Os trabalhos a seguir apresentam formulas semelhantes aos trabalhos que foram utilizados para a implementacao do consumo de potencia
 *        Traffic and Power-Aware Protection Scheme in Elastic Optical Networks (2012)
 *        Spectrum and energy-efficient survivable routing algorithmin elastic optical network (2016)
 *
 * @author Alexandre, Iallen
 *
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
			PCedfas += Ns * (30.0 + 140.0); // Power consumption for amplifiers in the link
		}
		n = noDestino.getOxc().getLinksList().size();
		PCoxcs += n * 85.0 + 150.0; // Power consumption for an OXC

		double PCt = quantSlotsRequeridos * PCofdm; // Power consumption of transponders
		double PClinks = (quantSlotsRequeridos / totalSlots) * (PCedfas + PCoxcs); // Power consumption along the links
		double PCtotal = PCt + PClinks; // Total power consumption by request

		return PCtotal;
	}


	/**
	 * Este metodo cacula o consumo de energia da rede
	 *
	 * @param cp - ControlPlane
	 * @return double - (W) - potencia consumida pela rede
	 */
	public static double computeNetworkPowerConsumption(ControlPlane cp){

		double PCtrans = computeTranspondersPowerConsumption(cp);
		double PCoxcs = computeOxcsPowerConsumption(cp.getMesh().getNodeList());
		double PClinks = computeLinksPowerConsumption(cp.getMesh().getLinkList());

		double PCtotal = PCtrans + PCoxcs + PClinks;

		return PCtotal;
	}

	/**
	 * Este metodo calcula o consumo de energia dos transponders
	 *
	 * @param cp - ControlePlane
	 * @return double - (W)
	 */
	public static double computeTranspondersPowerConsumption(ControlPlane cp){
		double sumPCtrans = 0.0;

		TreeSet<Circuit> requestList = cp.getConnections();
		for(Circuit circuit : requestList){
			sumPCtrans += 2.0 * computeTransponderPowerConsumption(circuit); //consumo de energia do transmitter e do receiver
		}

		return sumPCtrans;
	}

	/**
	 * Este metodo calcula o consumo de energia do transponder
	 *
	 * @param request - Request
	 * @return double - (W)
	 */
	public static double computeTransponderPowerConsumption(Circuit request){

		double fs = request.getRoute().getLinkList().firstElement().getSlotSpectrumBand(); //Hz, frequencia de um slot
		double tr = (fs * PhysicalLayer.log2(request.getModulation().getM())) / 10E+9; //Gbps, taxa de transmissao de um slot usando o formato de modulacao informado
		double PCofdm = 1.683 * tr + 91.333; //consumo de energia de um slot usando o formato de modulacao informado

		int numSlots = request.getSpectrumAssigned()[1]-request.getSpectrumAssigned()[0]+1; //quantidade de slots requeridos pela requisicao
		double PCtran = numSlots * PCofdm; //consumo de energia de um transponder usando um formato de modulacao que utiliza uma certa quantidade de slots

		return PCtran;
	}

	/**
	 * Este metodo calcula o consumo de energia dos OXCs
	 *
	 * @param nodeList - List<Node>
	 * @return double - (W)
	 */
	public static double computeOxcsPowerConsumption(List<Node> nodeList){
		double sumPCoxcs = 0.0;

		for(Node node : nodeList){
			sumPCoxcs += computeOxcPowerConsumption(node.getOxc()); //consumo de energia para um OXC
		}

		return sumPCoxcs;
	}

	/**
	 * Este metodo calcula o consumo de energia de um OXC
	 *
	 * @param oxc - OXC
	 * @return double - (W)
	 */
	public static double computeOxcPowerConsumption(Oxc oxc){

		double n = oxc.getLinksList().size(); //grau do node
		double PCoxc = n * 85.0 + 150.0; //consumo de energia para um OXC

		return PCoxc;
	}

	/**
	 * Este metodo calcula o consumo de energia dos amplificadores nos enlaces
	 *
	 * @param linkList - List<Link>
	 * @return double - (W)
	 */
	public static double computeLinksPowerConsumption(List<Link> linkList){
		double sumPClinks = 0.0;

		for(Link enlace : linkList){
			sumPClinks += computeLinkPowerConsumption(enlace); //consumo de energia para os amplificadores no enlace
		}

		return sumPClinks;
	}

	/**
	 * Este metodo calcula o consumo de energia dos amplificadores em um dado enlace
	 *
	 * @param link - Link
	 * @return double - (W)
	 */
	public static double computeLinkPowerConsumption(Link link){

		double Ns = PhysicalLayer.getNumberOfAmplifiers(link.getDistance()); // numero de spans
		double PClink = Ns * 200.0; //consumo de energia para os amplificadores no enlace, 60 + 140 = 200

		return PClink;
	}

}
