package network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import simulationControl.Util;
import simulationControl.parsers.PhysicalLayerConfig;

/**
 * This class represents the physical layer of the optical network.
 * 
 * @author Alexandre
 */
public class PhysicalLayer implements Serializable {

	// Allows you to enable or disable the calculations of physical layer
    private boolean activeQoT; // QoTN
    private boolean activeQoTForOther; // QoTO
	
    private boolean activeASE; // Active the ASE noise of the amplifier
    private boolean activeNLI; // Active nonlinear noise in the fibers
    
    private double rateOfFEC; // FEC (Forward Error Correction), The most used rate is 7% which corresponds to the BER of 3.8E-3
    private int typeOfTestQoT; //0, To check for the SNR threshold (Signal-to-Noise Ratio), or another value, to check for the BER threshold (Bit Error Rate)
	
    private double power; // Power per channel, dBm
    private double L; // Size of a span, km
    private double alpha; // Fiber loss, dB/km
    private double gamma; // Fiber nonlinearity,  (W*m)^-1
    private double D; // Dispersion parameter, s/m^2
    private double centerFrequency; //Frequency of light
	
    private double h; // Constant of Planck
    private double NF; // Amplifier noise figure, dB
    private double pSat; // Saturation power of the amplifier, dBm
    private double A1; // Amplifier noise factor parameter, A1
    private double A2; // Amplifier noise factor parameter, A2
    private int typeOfAmplifierGain; // Type of amplifier gain, 0 to fixed gain and 1 to saturated gain
    private double amplificationFrequency; // Frequency used for amplification
    
    private double Lsss; // Switch insertion loss, dB
    
    private boolean fixedPowerSpectralDensity; // To enable or disable fixed power spectral density
	private double referenceBandwidth; // Reference bandwidth for power spectral density
    
	private Amplifier boosterAmp; // Booster amplifier
	private Amplifier lineAmp; // Line amplifier
	private Amplifier preAmp; // Pre amplifier
	
	private double linearPower; // Transmitter power, Watt
	private double alphaLinear; // 1/m
	private double beta2; // Group-velocity dispersion
	
	private double slotBandwidth; // Hz
	private double lowerFrequency; // Hz
	
	/**
	 * Creates a new instance of PhysicalLayerConfig
	 * 
	 * @param plc PhysicalLayerConfig
	 */
    public PhysicalLayer(PhysicalLayerConfig plc, Mesh mesh){
        this.activeQoT = plc.isActiveQoT();
        this.activeQoTForOther = plc.isActiveQoTForOther();
    	
        this.activeASE = plc.isActiveASE();
        this.activeNLI = plc.isActiveNLI();
        
        this.typeOfTestQoT = plc.getTypeOfTestQoT();
        this.rateOfFEC = plc.getRateOfFEC();
    	
        this.power = plc.getPower();
        this.L = plc.getSpanLength();
        this.alpha = plc.getFiberLoss();
        this.gamma = plc.getFiberNonlinearity();
        this.D = plc.getFiberDispersion();
        this.centerFrequency = plc.getCenterFrequency();
    	
        this.h = plc.getConstantOfPlanck();
        this.NF = plc.getNoiseFigureOfOpticalAmplifier();
        this.pSat = plc.getPowerSaturationOfOpticalAmplifier();
        this.A1 = plc.getNoiseFactorModelParameterA1();
        this.A2 = plc.getNoiseFactorModelParameterA2();
        this.typeOfAmplifierGain = plc.getTypeOfAmplifierGain();
        this.amplificationFrequency = plc.getAmplificationFrequency();
        
        this.Lsss = plc.getSwitchInsertionLoss();
        
        this.fixedPowerSpectralDensity = plc.isFixedPowerSpectralDensity();
        this.referenceBandwidth = plc.getReferenceBandwidthForPowerSpectralDensity();
        
        this.linearPower = ratioOfDB(power) * 1.0E-3; // converting to Watt
        this.alphaLinear = computeAlphaLinear(alpha);
        this.beta2 = computeBeta2(D, centerFrequency);
        
        double spanMeter = L * 1000; // span in meter
        double attenuationBySpan = Math.pow(Math.E, alphaLinear * spanMeter);
        double boosterAmpGainLinear = ratioOfDB(Lsss);
        double lineAmpGainLinear = attenuationBySpan;
        double preAmpGainLinear = attenuationBySpan * ratioOfDB(Lsss);
        
        this.boosterAmp = new Amplifier(ratioForDB(boosterAmpGainLinear), pSat, NF, h, amplificationFrequency, 0.0, A1, A2);
        this.lineAmp = new Amplifier(ratioForDB(lineAmpGainLinear), pSat, NF, h, amplificationFrequency, 0.0, A1, A2);
        this.preAmp = new Amplifier(ratioForDB(preAmpGainLinear), pSat, NF, h, amplificationFrequency, 0.0, A1, A2);
        
        this.slotBandwidth = mesh.getLinkList().firstElement().getSlotSpectrumBand(); //Hz
        double totalSlots = mesh.getLinkList().firstElement().getNumOfSlots();
		this.lowerFrequency = centerFrequency - (slotBandwidth * (totalSlots / 2.0)); // Hz, Half slots are removed because center Frequency = 193.55E+12 is the central frequency of the optical spectrum
    }
  
	/**
	 * Returns if QoTN check is active or not
	 * 
	 * @return the activeQoT
	 */
	public boolean isActiveQoT() {
		return activeQoT;
	}

	/**
	 * Returns if QoTO check is active or not
	 * 
	 * @return the activeQoTForOther
	 */
	public boolean isActiveQoTForOther() {
		return activeQoTForOther;
	}
	
	/**
	 * Returns the Size of a span (Km)
	 * 
	 * @return the L
	 */
	public double getSpanLength() {
		return L;
	}
	
	/**
	 * Returns the rate of FEC
	 * 
	 * @return double
	 */
	public double getRateOfFEC(){
		return rateOfFEC;
	}
	
	/**
	 * This method returns the number of amplifiers on a link including the booster and pre
	 * 
	 * @param distance double
	 * @return double double
	 */
	public double getNumberOfAmplifiers(double distance){
		return 2.0 + roundUp((distance / L) - 1.0);
	}
	
	/**
	 * This method returns the number of line amplifiers on a link
	 * 
	 * @param distance double
	 * @return double
	 */
	public double getNumberOfLineAmplifiers(double distance){
		return roundUp((distance / L) - 1.0);
	}
	
	/**
	 * This method calculates the BER threshold based on the SNR threshold of a given modulation format
	 * 
	 * @param modulation Modulation
	 * @return double
	 */
	public double getBERthreshold(Modulation modulation){
		double BERthreshold = getBER(modulation.getSNRthresholdLinear(), modulation.getM());
		return BERthreshold;
	}
	
	/**
	 * Verifies if the calculated SNR for the circuit agrees to the modulation format threshold
	 * 
	 * @param modulation Modulation
	 * @param SNRdB double
	 * @param SNRlinear double
	 * @return boolean
	 */
	public boolean isAdmissible(Modulation modulation, double SNRdB, double SNRlinear){
		if(typeOfTestQoT == 0){ //Check by SNR threshold (dB)
			double SNRdBthreshold = modulation.getSNRthreshold();
			
			if(SNRdB >= SNRdBthreshold){
				return true;
			}
		} else { //Check by BER threshold
			double BERthreshold = getBERthreshold(modulation);
			double BER = getBER(SNRlinear, modulation.getM());
			
			if(BER <= BERthreshold){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Verifies that the QoT of the circuit is acceptable with the modulation format
	 * 
	 * @param circuit Circuit
	 * @param route Route
	 * @param modulation Modulation
	 * @param spectrumAssigned int[]
	 * @param testCircuit Circuit
	 * @param addTestCircuit boolean
	 * @return boolean
	 */
	public boolean isAdmissibleModultion(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[], Circuit testCircuit, boolean addTestCircuit){
		double SNR = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, testCircuit, addTestCircuit);
		double SNRdB = ratioForDB(SNR);
		circuit.setSNR(SNRdB);
		
		boolean QoT = isAdmissible(modulation, SNRdB, SNR);
		
		return QoT;
	}
	
	/**
	 * Verifies that the QoT of the circuit is acceptable with the modulation format for segment
	 * 
	 * @param circuit Circuit
	 * @param route Route
	 * @param sourceNodeIndex int
	 * @param destinationNodeIndex int
	 * @param modulation Modulation
	 * @param spectrumAssigned int[]
	 * @param testCircuit Circuit
	 * @param addTestCircuit boolean
	 * @return boolean
	 */
	public boolean isAdmissibleModultionBySegment(Circuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[], Circuit testCircuit, boolean addTestCircuit){
		double SNR = computeSNRSegment(circuit, route, sourceNodeIndex, destinationNodeIndex, modulation, spectrumAssigned, testCircuit, addTestCircuit);
		double SNRdB = ratioForDB(SNR);
		circuit.setSNR(SNRdB);
		
		boolean QoT = isAdmissible(modulation, SNRdB, SNR);
		
		return QoT;
	}
	
	/**
	 * Based on articles: 
	 *  - Nonlinear Impairment Aware Resource Allocation in Elastic Optical Networks (2015)
	 *  - Modeling of Nonlinear Signal Distortion in Fiber-Optic Networks (2014)
	 *  
	 * @param circuit Circuit
	 * @param route Route
	 * @param sourceNodeIndex int - Segment start node index
	 * @param destinationNodeIndex int - Segment end node index
	 * @param modulation Modulation
	 * @param spectrumAssigned int[]
	 * @param testCircuit Circuit - Circuit used to verify the impact on the other circuit informed
	 * @param addTestCircuit boolean - To add the test circuit to the circuit list
	 * @return double - SNR (linear)
	 */
	public double computeSNRSegment(Circuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[], Circuit testCircuit, boolean addTestCircuit){
		
		double Ptx = ratioOfDB(power) * 1.0E-3; //W, Transmitter power
		double Pase = 0.0;
		double Pnli = 0.0;
		
		int numSlotsRequired = spectrumAssigned[1] - spectrumAssigned[0] + 1; // Number of slots required
		double fs = route.getLinkList().firstElement().getSlotSpectrumBand(); //Hz
		double Bsi = numSlotsRequired * fs; // Circuit bandwidth
		
		double slotsTotal = route.getLinkList().firstElement().getNumOfSlots();
		double lowerFrequency = centerFrequency - (fs * (slotsTotal / 2.0)); //Hz, Half slots are removed because center Frequency = 193.0E + 12 is the central frequency of the optical spectrum
		double fi = lowerFrequency + (fs * (spectrumAssigned[0] - 1)) + (Bsi / 2); // Central frequency of circuit
		
		double I = Ptx / referenceBandwidth; // Signal power density for the number of reference slots
		
		Node sourceNode = null;
		Node destinationNode = null;
		Link link = null;
		TreeSet<Circuit> circuitList = null;
		
		double G0 = alpha * L; // Gain in dB of the amplifier
		Amplifier amp = new Amplifier(G0, pSat, NF, h, centerFrequency, 0.0, A1, A2);
		
		for(int i = sourceNodeIndex; i < destinationNodeIndex; i++){
			sourceNode = route.getNode(i);
			destinationNode = route.getNode(i + 1);
			link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
			double Ns = PhysicalLayer.roundUp(link.getDistance() / L); // Number of spans
			
			circuitList = getCircuitList(link, circuit, testCircuit, addTestCircuit);
			
			if(activeNLI){
				double noiseNli = Ns * getGnli(circuit, link, I, Bsi, fi, gamma, D, alpha, lowerFrequency, circuitList);
				Pnli = Pnli + noiseNli;
			}
			
			if(activeASE){
				double totalPower = 0.0;
				if(typeOfAmplifierGain == 1){
					totalPower = getTotalPowerInTheLink(circuitList, link, Ptx, Bsi, I);
				}
				
				double noiseAse = Ns * 2.0 * amp.getAseByGain(totalPower, amp.getGainByType(totalPower, typeOfAmplifierGain));
				Pase = Pase + noiseAse;
			}
		}
		
		double SNR = I / (Pase + Pnli);
		
		return SNR;
	}
	
	/**
	 * Create a list of the circuits that use the link
	 * 
	 * @param link Link
	 * @param circuit Circuit
	 * @param testCircuit Circuit
	 * @param addTestCircuit boolean
	 * @return TreeSet<Circuit>
	 */
	private TreeSet<Circuit> getCircuitList(Link link, Circuit circuit, Circuit testCircuit, boolean addTestCircuit){
		TreeSet<Circuit> circuitList = new TreeSet<Circuit>();
		
		for (Circuit circtuiTemp : link.getCircuitList()) {
			circuitList.add(circtuiTemp);
		}
		
		if(!circuitList.contains(circuit)){
			circuitList.add(circuit);
		}
		
		if(testCircuit != null && testCircuit.getRoute().containThisLink(link)) {
			
			if(!circuitList.contains(testCircuit) && addTestCircuit) {
				circuitList.add(testCircuit);
			}
			
			if(circuitList.contains(testCircuit) && !addTestCircuit) {
				circuitList.remove(testCircuit);
			}
		}
		
		return circuitList;
	}
	
	/**
	 * Total input power on the link
	 * 
	 * @param link Link
	 * @param powerI double
	 * @param Bsi double
	 * @param I double
	 * @return double
	 */
	public double getTotalPowerInTheLink(TreeSet<Circuit> circuitList, Link link, double powerI, double Bsi, double I){
		double totalPower = 0.0;
		double circuitPower = 0.0;
		int saj[] = null;
		double numOfSlots = 0.0;
		double Bsj = 0.0;
		double powerJ = powerI;
		
		for(Circuit cricuitJ : circuitList){
			
			circuitPower = powerJ;
			if(fixedPowerSpectralDensity){
				saj = cricuitJ.getSpectrumAssignedByLink(link);;
				numOfSlots = saj[1] - saj[0] + 1.0; // Number of slots
				Bsj = (numOfSlots - cricuitJ.getModulation().getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
				
				circuitPower = I * Bsj;
			}
			
			totalPower += circuitPower;
		}
		
		return totalPower;
	}
	
	/**
	 * Based on article:
	 *  - Nonlinear Impairment Aware Resource Allocation in Elastic Optical Networks (2015)
	 * 
	 * @param circuit Circuit
	 * @param link Link
	 * @param Bsi double
	 * @param I double
	 * @param fi double
	 * @return double
	 */
	public double getGnli(Circuit circuit, Link link, double I, double Bsi, double fi, double gamma, double beta2, double alpha, double lowerFrequency, TreeSet<Circuit> circuitList){
		double alphaLinear = ratioOfDB(alpha);
		if(beta2 < 0.0){
			beta2 = -1.0 * beta2;
		}
		
		double mi = (3.0 * gamma * gamma * I * I * I) / (2.0 * Math.PI * alphaLinear * beta2);
		
		double ro = (Math.PI * Math.PI * beta2) / (2.0 * alphaLinear);
		double p1 = arcsinh(ro * Bsi * Bsi);
		double p2 = 0.0;
		
		for(Circuit cricuitTemp : circuitList){
			
			if(!circuit.equals(cricuitTemp)){
				double fs = link.getSlotSpectrumBand();
				int sa[] = cricuitTemp.getSpectrumAssignedByLink(link);
				double numOfSlots = sa[1] - sa[0] + 1;
				double Bsj = numOfSlots * fs; //Circuit bandwidth
				double fj = lowerFrequency + (fs * (sa[0] - 1)) + (Bsj / 2); //Central frequency of circuit
				
				double deltaFij = fi - fj;
				if(deltaFij < 0.0)
					deltaFij = -1.0 * deltaFij;
				
				double d1 = deltaFij + (Bsj / 2);
				double d2 = deltaFij - (Bsj / 2);
				
				double ln = Math.log(d1 / d2);
				p2 += ln;
			}
		}
		
		double gnli = mi * (p1 + p2); 
		return gnli;
	}
	
	/**
	 * Function that returns the inverse hyperbolic sine of the argument
	 * asinh == arcsinh
	 * 
	 * @param x double
	 * @return double
	 */
	public static double arcsinh(double x){
		return Math.log(x + Math.sqrt(x * x + 1.0));
	}
	
	/**
	 * This method returns the BER (Bit Error Rate) for a modulation scheme M-QAM.
	 * Based on articles:
	 *  - Capacity Limits of Optical Fiber Networks (2010)
	 *  - Analise do Impacto do Ruido ASE em Redes Opticas Elasticas Transparentes Utilizando Multiplos Formatos de Modulacao (2015)
	 * 
	 * @param SNR double
	 * @param M double
	 * @return double
	 */
	public static double getBER(double SNR, double M){
		double SNRb = SNR / log2(M); // SNR per bit
		
		double p1 = (3.0 * SNRb * log2(M)) / (2.0 * (M - 1.0));
		double p2 = erfc(Math.sqrt(p1));
		double BER = (2.0 / log2(M)) * ((Math.sqrt(M) - 1.0) / Math.sqrt(M)) * p2;
		
		return BER;
	}
	
	/**
	 * Complementary error function
	 * 
	 * @param x double
	 * @return double
	 */
	public static double erfc(double x){
		return (1.0 - erf(x));
	}
	
	/**
	 * Error function - approximation
	 * http://www.galileu.esalq.usp.br/mostra_topico.php?cod=240
	 * 
	 * @param x double
	 * @return double
	 */
	public static double erf(double x){
		double a = 0.140012;
		double v = sgn(x) * Math.sqrt(1.0 - Math.exp(-1.0 * (x * x) * (((4.0 / Math.PI) + (a * x * x)) / (1.0 + (a * x * x)))));
		return v;
	}
	
	/**
	 * Signal function
	 * 
	 * @param x double
	 * @return double
	 */
	public static double sgn(double x){
		double s = 1.0;
		if(x < 0.0){
			s = s * -1.0;
		}else if(x == 0.0){
			s = 0.0;
		}
		return s;
	}
	
	/**
	 * Converts a ratio (linear) to decibel
	 * 
	 * @param ratio double
	 * @return double dB
	 */
	public static double ratioForDB(double ratio) {
		double dB = 10.0 * Math.log10(ratio);
		return dB;
	}

	/**
	 * Converts a value in dB to a linear value (ratio)
	 * 
	 * @param dB double
	 * @return double ratio
	 */
	public static double ratioOfDB(double dB) {
		double ratio = Math.pow(10.0, (dB / 10.0));
		return ratio;
	}
	
	/**
	 * Logarithm in base 2
	 * 
	 * @param x double
	 * @return double
	 */
	public static double log2(double x){
		return (Math.log10(x) / Math.log10(2.0));
	}
	
	/**
	 * Rounds up a double value for int
	 * 
	 * @param res double
	 * @return int
	 */
	public static int roundUp(double res){
		if(res < 0.0) {
			return 0;
		}
		
		int res2 = (int) res;
		if(res - res2 != 0.0){
			res2++;
		}
		return res2;
	}
	
	/**
	 * Returns the beta2 parameter
	 * 
	 * @param D double
	 * @param frequencia double
	 * @return double
	 */
	public static double computeBeta2(double D, double frequencia){
		double c = 299792458.0; // speed of light, m/s
		double lambda = c / frequencia;
		double beta2 = -1.0 * D * (lambda * lambda) / (2.0 * Math.PI * c);
		return beta2;
	}
	
	/**
	 * Returns the alpha linear value (1/m) of a value in dB/km
	 * 
	 * Example:
	 * 10 * Log10(e^(alpha * km)) = 0.2 dB/km
	 * (alpha * km) * 10 * Log10(e) = 0.2 dB/km
	 * alpha = (0.2 dB/km) / (km * 10 * Log10(e))
	 * alpha = (0.2 dB/km) / (1000 * m * 10 * Log10(e))
	 * alpha = (0.2 dB/km) / (10000 * Log10(e) * m)
	 * alpha (dB/km) = (0.2 dB/km) / (10000 * Log10(e) * m)
	 * alpha (linear) = (0.2 dB/km) / (10000 * Log10(e) * m * dB/km)
	 * alpha (linear) = 4.60517E-5 / m
	 * 
	 * @param alpha double
	 * @return double
	 */
	public static double computeAlphaLinear(double alpha){
		double alphaLinear = alpha / (1.0E+4 * Math.log10(Math.E));
		return alphaLinear;
	}
	
	/**
	 * Calculates the transmission distances of the modulation formats
	 * 
	 * @param mesh Mesh
	 * @param avaliableModulations List<Modulation>
	 * @return HashMap<Modulation, HashMap<Double, Double>>
	 */
	public HashMap<String, HashMap<Double, Double>> computesModulationsDistances(Mesh mesh, List<Modulation> avaliableModulations) {
		//System.out.println("Computing of the distances of the modulation formats");
		
		Set<Double> transmissionRateList = Util.bandwidths;
		HashMap<String, HashMap<Double, Double>> modsTrsDistances = new HashMap<>();
		
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			
			for(double transmissionRate : transmissionRateList) {
				
				HashMap<Double, Double> slotsDist = modsTrsDistances.get(mod.getName());
				if(slotsDist == null) {
					slotsDist = new HashMap<>();
					modsTrsDistances.put(mod.getName(), slotsDist);
				}
				
				Double dist = slotsDist.get(transmissionRate);
				if(dist == null) {
					dist = 0.0;
				}
				slotsDist.put(transmissionRate, dist);
			}
		}
		
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			
			for(double transmissionRate : transmissionRateList) {
				
				double distance = computeModulationDistanceByBandwidth(mod, transmissionRate, mesh);
				modsTrsDistances.get(mod.getName()).put(transmissionRate, distance);
			}
		}
		
//		for(double transmissionRate : transmissionRateList) {
//			System.out.println("TR(Gbps) = " + (transmissionRate / 1.0E+9));
//			
//			for(int m = 0; m < avaliableModulations.size(); m++) {
//				Modulation mod = avaliableModulations.get(m);
//				
//				double modTrDist = modTrDistance.get(mod).get(transmissionRate);
//				System.out.println("Mod = " + mod.getName() + ", distance(km) = " + modTrDist);
//			}
//		}
		
		return modsTrsDistances;
	}
	
	/**
	 * Calculates the distance to a modulation format considering the bandwidth
	 * 
	 * @param mod
	 * @param bandwidth
	 * @param mesh
	 * @return double
	 */
	public double computeModulationDistanceByBandwidth(Modulation mod, double bandwidth, Mesh mesh) {
		
		int totalSlots = mesh.getLinkList().firstElement().getNumOfSlots();
		
		Vector<Link> linkList = mesh.getLinkList();
		double sumLastFiberSegment = 0.0;
		for(int l = 0; l < linkList.size(); l++) {
			double Ns = getNumberOfLineAmplifiers(linkList.get(l).getDistance());
			double lastFiberSegment = linkList.get(l).getDistance() - (Ns * L);
			sumLastFiberSegment += lastFiberSegment;
		}
		double averageLastFiberSegment = sumLastFiberSegment / linkList.size();
		
		double totalDistance = 50000.0; //km
		int quantSpansPorEnlace = (int)(totalDistance / L); // number of spans per link
		
		int slotNumber = mod.requiredSlots(bandwidth);
		int sa[] = new int[2];
		sa[0] = 1;
		sa[1] = sa[0] + slotNumber - 1;
		
		double modTrDistance = 0.0;
		
		for(int ns = 0; ns <= quantSpansPorEnlace; ns++){
			double distance = (ns * L) + averageLastFiberSegment;
			
			Node n1 = new Node("1", 1000, 1000, 0, 100);
			Node n2 = new Node("2", 1000, 1000, 0, 100);
			n1.getOxc().addLink(new Link(n1.getOxc(), n2.getOxc(), totalSlots, slotBandwidth, distance));
			
			Vector<Node> listNodes = new Vector<Node>();
			listNodes.add(n1);
			listNodes.add(n2);
			
			Route route = new Route(listNodes);
			Pair pair = new Pair(n1, n2);
			
			Circuit circuitTemp = new Circuit();
			circuitTemp.setPair(pair);
			circuitTemp.setRoute(route);
			circuitTemp.setModulation(mod);
			circuitTemp.setSpectrumAssigned(sa);
			
			route.getLink(0).addCircuit(circuitTemp);
			
			double OSNR = computeSNRSegment(circuitTemp, circuitTemp.getRoute(), 0, circuitTemp.getRoute().getNodeList().size() - 1, circuitTemp.getModulation(), circuitTemp.getSpectrumAssigned(), null, false);
			double OSNRdB = PhysicalLayer.ratioForDB(OSNR);
			
			if((OSNRdB >= mod.getSNRthreshold()) && (distance > modTrDistance)){
				modTrDistance = distance;
			}
		}
		
		return modTrDistance;
	}
	
	/**
	 * Calculates the transmission distances of the modulation formats
	 * 
	 * @param mesh Mesh
	 * @param avaliableModulations List<Modulation>
	 */
	public void computesDistances(Mesh mesh, List<Modulation> avaliableModulations) {
		
		int totalSlots = mesh.getLinkList().firstElement().getNumOfSlots();
		
		Vector<Link> linkList = mesh.getLinkList();
		double sumLastFiberSegment = 0.0;
		for(int l = 0; l < linkList.size(); l++) {
			double Ns = getNumberOfLineAmplifiers(linkList.get(l).getDistance());
			double lastFiberSegment = linkList.get(l).getDistance() - (Ns * L);
			sumLastFiberSegment += lastFiberSegment;
		}
		double averageLastFiberSegment = sumLastFiberSegment / linkList.size();
		
		double transmissionRate = 10.0E+9; //bps
		double totalDistance = 50000.0; //km
		int quantSpansPerEnlace = (int)(totalDistance / L); // number of spans per link
		
		HashMap<Modulation, Double> distModulations = new HashMap<Modulation, Double>(avaliableModulations.size());
		for(int m = 0; m < avaliableModulations.size(); m++) {
			distModulations.put(avaliableModulations.get(m), 0.0);
		}
		
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			
			int slotNumber = mod.requiredSlots(transmissionRate);
			int sa[] = new int[2];
			sa[0] = 1;
			sa[1] = sa[0] + slotNumber - 1;
			
			for(int ns = 0; ns < quantSpansPerEnlace; ns++){
				double distance = (ns * L) + averageLastFiberSegment;
				
				Node n1 = new Node("1", 1000, 1000, 0, 100);
				Node n2 = new Node("2", 1000, 1000, 0, 100);
				n1.getOxc().addLink(new Link(n1.getOxc(), n2.getOxc(), totalSlots, slotBandwidth, distance));
				
				Vector<Node> listNodes = new Vector<Node>();
				listNodes.add(n1);
				listNodes.add(n2);
				
				Route route = new Route(listNodes);
				Pair pair = new Pair(n1, n2);
				
				Circuit circuitTemp = new Circuit();
				circuitTemp.setPair(pair);
				circuitTemp.setRoute(route);
				circuitTemp.setModulation(mod);
				circuitTemp.setSpectrumAssigned(sa);
				
				route.getLink(0).addCircuit(circuitTemp);
				
				double OSNR = computeSNRSegment(circuitTemp, circuitTemp.getRoute(), 0, circuitTemp.getRoute().getNodeList().size() - 1, circuitTemp.getModulation(), circuitTemp.getSpectrumAssigned(), null, false);
				double OSNRdB = PhysicalLayer.ratioForDB(OSNR);
				
				double modDist = distModulations.get(mod);
				if((OSNRdB >= mod.getSNRthreshold()) && (distance > modDist)){
					distModulations.put(mod, distance);
				}	
			}
		}
		
		// configures the distances found in the modulations
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			mod.setMaxRange(distModulations.get(mod));
			
			//System.out.println("Mod = " + mod.getName() + ", distance = " + distModulations.get(mod));
		}
	}
}
