package network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import request.RequestForConnection;
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
    private double LsssLinear;
    
    private boolean fixedPowerSpectralDensity; // To enable or disable fixed power spectral density
	private double referenceBandwidth; // Reference bandwidth for power spectral density
    
	private Amplifier boosterAmp; // Booster amplifier
	private Amplifier lineAmp; // Line amplifier
	private Amplifier preAmp; // Pre amplifier
	
	private double PowerLinear; // Transmitter power, Watt
	private double alphaLinear; // 1/m
	private double beta2; // Group-velocity dispersion
	private double attenuationBySpanLinear;
	
	private double slotBandwidth; // Hz
	private double lowerFrequency; // Hz
	
	private double polarizationModes; // Number of polarization modes
	
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
        this.LsssLinear = ratioOfDB(Lsss);
        
        this.fixedPowerSpectralDensity = plc.isFixedPowerSpectralDensity();
        this.referenceBandwidth = plc.getReferenceBandwidthForPowerSpectralDensity();
        
        this.polarizationModes = plc.getPolarizationModes();
        if(this.polarizationModes == 0.0) {
        	this.polarizationModes = 2.0;
        }
        
        this.PowerLinear = ratioOfDB(power) * 1.0E-3; // converting to Watt
        this.alphaLinear = computeAlphaLinear(alpha);
        this.beta2 = computeBeta2(D, centerFrequency);
        
        double spanMeter = L * 1000.0; // span in meter
        this.attenuationBySpanLinear = Math.pow(Math.E, alphaLinear * spanMeter);
        double boosterAmpGainLinear = LsssLinear * LsssLinear;
        double lineAmpGainLinear = attenuationBySpanLinear;
        double preAmpGainLinear = attenuationBySpanLinear;
        
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
	 * Return the number of polarization modes
	 * 
	 * @return double
	 */
	public double getPolarizationModes() {
		return this.polarizationModes;
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
	
	public boolean isAdmissible2(double bitRate, Modulation modulation, double OSNRdB, double OSNRlinear){
		if(typeOfTestQoT == 0){ //Check by SNR threshold (dB)
			double SNRdBthreshold = modulation.getSNRthreshold();
			double SNRdBthresholdLinear = modulation.getSNRthresholdLinear();
			
			double B0 = 12.5E+9;
			double OverallBitRate = bitRate * (1.0 + rateOfFEC);
			double OSNRdBthreshold = ratioForDB((OverallBitRate / (2.0 * B0)) * SNRdBthresholdLinear);
			
			if(OSNRdB >= OSNRdBthreshold){
				return true;
			}
		} else { //Check by BER threshold
			double BERthreshold = getBERthreshold(modulation);
			double BER = getBER(OSNRlinear, modulation.getM());
			
			if(BER <= BERthreshold){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Verifies that the QoT of the circuit is acceptable with the modulation format
	 * The circuit in question must not have allocated the network resources
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
		//boolean QoT2 = isAdmissible2(circuit.getRequiredBandwidth(), modulation, SNRdB, SNR);
		
		return QoT;
	}
	
	/**
	 * Verifies that the QoT of the circuit is acceptable with the modulation format for segment
	 * The circuit in question must not have allocated the network resources
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
		//boolean QoT2 = isAdmissible2(circuit.getRequiredBandwidth(), modulation, SNRdB, SNR);
		
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
		
		double numSlotsRequired = spectrumAssigned[1] - spectrumAssigned[0] + 1; // Number of slots required
		double Bsi = (numSlotsRequired - modulation.getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
		double fi = lowerFrequency + (slotBandwidth * (spectrumAssigned[0] - 1.0)) + (Bsi / 2.0); // Central frequency of circuit
		
		Bsi = modulation.getBandwidthFromBitRate(circuit.getRequiredBandwidth());
		
		double circuitPowerLinear = this.PowerLinear;
		if(circuit.getLaunchPowerLinear() != Double.POSITIVE_INFINITY) {
			circuitPowerLinear = circuit.getLaunchPowerLinear();
		}
		
		circuitPowerLinear = circuitPowerLinear / polarizationModes; // Determining the power for each polarization mode
		
		double I = circuitPowerLinear / referenceBandwidth; // Signal power density for the reference bandwidth
		if(!fixedPowerSpectralDensity){
			I = circuitPowerLinear / Bsi; // Signal power spectral density calculated according to the requested bandwidth
		}
		
		double Iase = 0.0;
		double Inli = 0.0;
		
		Node sourceNode = null;
		Node destinationNode = null;
		Link link = null;
		TreeSet<Circuit> circuitList = null;
		
		double Nl = 0.0; // Number of line amplifiers
		double noiseNli = 0.0;
		double totalPower = 0.0;
		double boosterAmpNoiseAse = 0.0;
		double preAmpNoiseAse = 0.0;
		double lineAmpNoiseAse = 0.0;
		double lastFiberSegment = 0.0;
		
		for(int i = sourceNodeIndex; i < destinationNodeIndex; i++){
			sourceNode = route.getNode(i);
			destinationNode = route.getNode(i + 1);
			link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
			Nl = getNumberOfLineAmplifiers(link.getDistance());
			
			circuitList = getCircuitList(link, circuit, testCircuit, addTestCircuit);
			
			if(activeNLI){
				noiseNli = getGnli(circuit, link, circuitPowerLinear, Bsi, I, fi, circuitList); // Computing the NLI for each polarization mode
				noiseNli = (Nl + 1.0) * noiseNli; // Ns + 1 corresponds to the line amplifiers span more the preamplifier span
				Inli = Inli + noiseNli;
			}
			
			if(activeASE){
				if(typeOfAmplifierGain == 1){
					totalPower = getTotalPowerInTheLink(circuitList, link, circuitPowerLinear, I);
				}
				
				// Computing the last span amplifier gain
				lastFiberSegment = link.getDistance() - (Nl * L);
				preAmp.setGain(alpha * lastFiberSegment);
				
				// Computing the ASE for each amplifier type
				boosterAmpNoiseAse = boosterAmp.getAseByGain(totalPower, boosterAmp.getGainByType(totalPower, typeOfAmplifierGain));
				lineAmpNoiseAse = lineAmp.getAseByGain(totalPower, lineAmp.getGainByType(totalPower, typeOfAmplifierGain));
				preAmpNoiseAse = preAmp.getAseByGain(totalPower, preAmp.getGainByType(totalPower, typeOfAmplifierGain));
				
				// Determining the ASE for each polarization mode
				boosterAmpNoiseAse = boosterAmpNoiseAse / polarizationModes; 
				lineAmpNoiseAse = lineAmpNoiseAse / polarizationModes;
				preAmpNoiseAse = preAmpNoiseAse / polarizationModes;
				
				lineAmpNoiseAse = Nl * lineAmpNoiseAse; // Computing ASE for all line amplifier spans
				
				Iase = Iase + (boosterAmpNoiseAse + lineAmpNoiseAse + preAmpNoiseAse);
			}
		}
		
		double SNR = I / (Iase + Inli);
		
		return SNR;
	}

	/**
	 * Create a list of the circuits that use the link
	 * 
	 * @param link Link
	 * @param circuit Circuit
	 * @param testCircuit Circuit
	 * @param addTestCircuit
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
	 * @param numSlotsRequired int
	 * @param checksOnTotalPower boolean
	 * @return double
	 */
	public double getTotalPowerInTheLink(TreeSet<Circuit> circuitList, Link link, double powerI, double I){
		double totalPower = 0.0;
		double circuitPower = 0.0;
		int saj[] = null;
		double numOfSlots = 0.0;
		double Bsj = 0.0;
		double powerJ = powerI;
		
		for(Circuit circuitJ : circuitList){
			
			circuitPower = powerJ;
			if(circuitJ.getLaunchPowerLinear() != Double.POSITIVE_INFINITY) {
				circuitPower = circuitJ.getLaunchPowerLinear();
				circuitPower = circuitPower / polarizationModes; // Determining the power for each polarization mode
			}
			
			if(fixedPowerSpectralDensity){
				saj = circuitJ.getSpectrumAssignedByLink(link);;
				numOfSlots = saj[1] - saj[0] + 1.0; // Number of slots
				Bsj = (numOfSlots - circuitJ.getModulation().getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
				
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
	 * @param circuitI Circuit
	 * @param link Link
	 * @param powerI double
	 * @param BsI double
	 * @param I double
	 * @param fI double
	 * @return double
	 */
	public double getGnli(Circuit circuitI, Link link, double powerI, double BsI, double Gi, double fI, TreeSet<Circuit> circuitList){
		double beta21 = beta2;
		if(beta21 < 0.0){
			beta21 = -1.0 * beta21;
		}
		
		double mi = Gi * (3.0 * gamma * gamma) / (2.0 * Math.PI * alphaLinear * beta21);
		double ro =  BsI * BsI * (Math.PI * Math.PI * beta21) / (2.0 * alphaLinear);
		if(ro < 0.0) {
			ro = -1.0 * ro;
		}
		double p1 = Gi * Gi * arcsinh(ro);
		
		double p2 = 0.0;
		int saJ[] = null;
		double numOfSlots = 0.0;
		double Bsj = 0.0;
		double fJ = 0.0;
		double deltaFij = 0.0;
		double d1 = 0.0;
		double d2 = 0.0;
		double d3 = 0.0;
		double ln = 0.0;
		double powerJ = powerI; // Power of the circuit j
		double Gj = Gi; // Power spectral density of the circuit j
		
		for(Circuit circuitJ : circuitList){
			
			if(!circuitI.equals(circuitJ)){
				saJ = circuitJ.getSpectrumAssignedByLink(link);
				numOfSlots = saJ[1] - saJ[0] + 1.0;
				
				Bsj = (numOfSlots - circuitJ.getModulation().getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
				fJ = lowerFrequency + (slotBandwidth * (saJ[0] - 1.0)) + (Bsj / 2.0); // Central frequency of circuit
				
				Bsj = circuitJ.getModulation().getBandwidthFromBitRate(circuitJ.getRequiredBandwidth());
				
				if(circuitJ.getLaunchPowerLinear() != Double.POSITIVE_INFINITY) {
					powerJ = circuitJ.getLaunchPowerLinear();
					powerJ = powerJ / polarizationModes; // Determining the power for each polarization mode
				}
				
				if(!fixedPowerSpectralDensity){
					Gj = powerJ / Bsj; // Power spectral density of the circuit j calculated according to the required bandwidth
				}
				
				deltaFij = fI - fJ;
				if(deltaFij < 0.0) {
					deltaFij = -1.0 * deltaFij;
				}
				
				d1 = deltaFij + (Bsj / 2.0);
				d2 = deltaFij - (Bsj / 2.0);
				
				d3 = d1 / d2;
				if(d3 < 0.0){
					d3 = -1.0 * d3;
				}
				
				ln = Math.log(d3);
				p2 += Gj * Gj * ln;
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
		double SNRb = SNR / log2(M); // SNR per bit -> provavelmente está errada, isso seria SNR por simbolo
		
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
	 * Returns the power linear
	 * 
	 * @return double
	 */
	public double getPowerLinear() {
		return PowerLinear;
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
		
		Set<Double> bitRateList = Util.bandwidths;
		HashMap<String, HashMap<Double, Double>> modsTrsDistances = new HashMap<>();
		
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			
			for(double bitRate : bitRateList) {
				
				HashMap<Double, Double> slotsDist = modsTrsDistances.get(mod.getName());
				if(slotsDist == null) {
					slotsDist = new HashMap<>();
					modsTrsDistances.put(mod.getName(), slotsDist);
				}
				
				Double dist = slotsDist.get(bitRate);
				if(dist == null) {
					dist = 0.0;
				}
				slotsDist.put(bitRate, dist);
			}
		}
		
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			
			for(double bitRate : bitRateList) {
				
				double distance = computeModulationDistanceByBandwidth(mod, bitRate, mesh);
				modsTrsDistances.get(mod.getName()).put(bitRate, distance);
			}
		}
		
		
//		for(double transmissionRate : transmissionRateList) {
//			System.out.println("TR(Gbps) = " + (transmissionRate / 1.0E+9));
//			
//			for(int m = 0; m < avaliableModulations.size(); m++) {
//				Modulation mod = avaliableModulations.get(m);
//				int slotNumber = mod.requiredSlots(transmissionRate) - mod.getGuardBand();
//				
//				System.out.println("Mod = " + mod.getName() + ", slot num = " + slotNumber);
//			}
//		}
		
		
//		for(double transmissionRate : transmissionRateList) {
//			System.out.println("TR(Gbps) = " + (transmissionRate / 1.0E+9));
//			
//			for(int m = 0; m < avaliableModulations.size(); m++) {
//				Modulation mod = avaliableModulations.get(m);
//				
//				double modTrDist = modsTrsDistances.get(mod.getName()).get(transmissionRate);
//				System.out.println("Mod = " + mod.getName() + ", distance(km) = " + modTrDist);
//			}
//		}
		
		return modsTrsDistances;
	}
	
	/**
	 * Calculates the distance to a modulation format considering the bandwidth
	 * 
	 * @param mod
	 * @param bitRate
	 * @param mesh
	 * @return double
	 */
	public double computeModulationDistanceByBandwidth(Modulation mod, double bitRate, Mesh mesh) {
		
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
		
		int slotNumber = mod.requiredSlots(bitRate);
		int sa[] = new int[2];
		sa[0] = 1;
		sa[1] = sa[0] + slotNumber - 1;
		
		double modTrDistance = 0.0;
		
		for(int ns = 0; ns <= quantSpansPorEnlace; ns++){
			double distance = (ns * L) + averageLastFiberSegment;
			
			Node n1 = new Node("1", 1000, 1000, 0, 1000);
			Node n2 = new Node("2", 1000, 1000, 0, 1000);
			n1.getOxc().addLink(new Link(n1.getOxc(), n2.getOxc(), totalSlots, slotBandwidth, distance));
			
			Vector<Node> listNodes = new Vector<Node>();
			listNodes.add(n1);
			listNodes.add(n2);
			
			Route route = new Route(listNodes);
			Pair pair = new Pair(n1, n2);
			
			RequestForConnection requestTemp = new RequestForConnection();
			requestTemp.setPair(pair);
			requestTemp.setRequiredBandwidth(bitRate);
			
			Circuit circuitTemp = new Circuit();
			circuitTemp.setPair(pair);
			circuitTemp.setRoute(route);
			circuitTemp.setModulation(mod);
			circuitTemp.setSpectrumAssigned(sa);
			circuitTemp.addRequest(requestTemp);
			
			double launchPower = Double.POSITIVE_INFINITY;
			if(!fixedPowerSpectralDensity){
				launchPower = computeMaximumPower2(bitRate, route, 0, route.getNodeList().size() - 1, mod, sa);
			}
			circuitTemp.setLaunchPowerLinear(launchPower);
			
			route.getLink(0).addCircuit(circuitTemp);
			
			double OSNR = computeSNRSegment(circuitTemp, circuitTemp.getRoute(), 0, circuitTemp.getRoute().getNodeList().size() - 1, circuitTemp.getModulation(), circuitTemp.getSpectrumAssigned(), null, false);
			double OSNRdB = PhysicalLayer.ratioForDB(OSNR);
			
			if((OSNRdB >= mod.getSNRthreshold()) && (distance > modTrDistance)){
				modTrDistance = distance;
			}
		}
		
		return modTrDistance;
	}
	
	
	public double computeMaximumPower(Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[]){
		
		//double I = PowerLinear / referenceBandwidth; // Signal power density for the reference bandwidth
		double Iase = 0.0;
		double Inli = 0.0;
		
		double numSlotsRequired = spectrumAssigned[1] - spectrumAssigned[0] + 1; // Number of slots required
		double Bsi = (numSlotsRequired - modulation.getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
		
		Node sourceNode = null;
		Node destinationNode = null;
		Link link = null;
		
		double Ns = 0.0; // Number of line amplifiers
		double totalPower = 0.0;
		
		for(int i = sourceNodeIndex; i < destinationNodeIndex; i++){
			sourceNode = route.getNode(i);
			destinationNode = route.getNode(i + 1);
			link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
			Ns = getNumberOfLineAmplifiers(link.getDistance());
			
			Ns += 1; // acrescentano o span do preamplificador
			for(int span = 0; span < Ns; span++) {
				
				double Snli = 0.0;
				double Sase = 0.0;
				
				if(activeNLI){
					double beta21 = beta2;
					if(beta21 < 0.0){
						beta21 = -1.0 * beta21;
					}
					
					double mi = (3.0 * gamma * gamma) / (2.0 * Math.PI * alphaLinear * beta21 * Bsi * Bsi * Bsi);
					double ro =  Bsi * Bsi * (Math.PI * Math.PI * beta21) / (2.0 * alphaLinear);
					if(ro < 0.0) {
						ro = -1.0 * ro;
					}
					double p1 = arcsinh(ro);
					Snli = mi * p1;
				}
				
				if(activeASE){
					Sase = lineAmp.getAseByGain(totalPower, lineAmp.getGainByType(totalPower, typeOfAmplifierGain));
				}
				
				double lossAndGain = 1.0;
				double lineAmpGain = lineAmp.getGainByType(totalPower, typeOfAmplifierGain);
				for(int p = span + 1; p < Ns; p++) {
					//lossAndGain = lossAndGain * (attenuationBySpanLinear / lineAmpGain);
					lossAndGain = lossAndGain * (lineAmpGain / attenuationBySpanLinear);
				}
				
				Inli += Snli * lossAndGain;
				Iase += Sase * lossAndGain;
			}
		}
		
		double Pmax = Math.cbrt(Iase / (2.0 * Inli));
		
		return Pmax;
	}
	
	public double computeMaximumPower2(double bitRate, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[]){
		
		//double I = PowerLinear / referenceBandwidth; // Signal power density for the reference bandwidth
		//I = I / polarizationModes;
		
		double Iase = 0.0;
		double Inli = 0.0;
		
		double numSlotsRequired = spectrumAssigned[1] - spectrumAssigned[0] + 1; // Number of slots required
		double Bsi = (numSlotsRequired - modulation.getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
		Bsi = modulation.getBandwidthFromBitRate(bitRate);
		
		Node sourceNode = null;
		Node destinationNode = null;
		Link link = null;
		
		double Nl = 0.0; // Number of line amplifiers
		double totalPower = 0.0;
		
		for(int i = sourceNodeIndex; i < destinationNodeIndex; i++){
			sourceNode = route.getNode(i);
			destinationNode = route.getNode(i + 1);
			link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
			Nl = getNumberOfLineAmplifiers(link.getDistance());
			
			// MUX insertion loss
			Inli = Inli / LsssLinear;
			Iase = Iase / LsssLinear;
			
			Inli = Inli * boosterAmp.getGainByType(totalPower, typeOfAmplifierGain);
			Iase = Iase * boosterAmp.getGainByType(totalPower, typeOfAmplifierGain);
			
			//I = I / LsssLinear;
			//I = I * boosterAmp.getGainByType(totalPower, typeOfAmplifierGain);
			
			if(activeASE){
				double Sase = boosterAmp.getAseByGain(totalPower, boosterAmp.getGainByType(totalPower, typeOfAmplifierGain));
				Sase = Sase / polarizationModes;
				Iase = Iase + Sase;
			}
			
			for(int span = 0; span < Nl; span++) {
				
				Inli = Inli / attenuationBySpanLinear;
				Iase = Iase / attenuationBySpanLinear;
				
				Inli = Inli * lineAmp.getGainByType(totalPower, typeOfAmplifierGain);
				Iase = Iase * lineAmp.getGainByType(totalPower, typeOfAmplifierGain);
				
				//I = I / attenuationBySpanLinear;
				//I = I * lineAmp.getGainByType(totalPower, typeOfAmplifierGain);
				
				if(activeNLI){
					double beta21 = beta2;
					if(beta21 < 0.0){
						beta21 = -1.0 * beta21;
					}
					
					double mi = (3.0 * gamma * gamma) / (2.0 * Math.PI * alphaLinear * beta21 * Bsi * Bsi * Bsi);
					double ro =  Bsi * Bsi * (Math.PI * Math.PI * beta21) / (2.0 * alphaLinear);
					if(ro < 0.0) {
						ro = -1.0 * ro;
					}
					double p1 = arcsinh(ro);
					double Snli = mi * p1;
					
					Inli = Inli + Snli;
				}
				
				if(activeASE){
					double Sase = lineAmp.getAseByGain(totalPower, lineAmp.getGainByType(totalPower, typeOfAmplifierGain));
					Sase = Sase / polarizationModes;
					Iase = Iase + Sase;
				}
			}
			
			double lastFiberSegment = link.getDistance() - (Nl * L);
			double attenuationBySpanPreAmpLinear = ratioOfDB(alpha * lastFiberSegment);
			preAmp.setGain(alpha * lastFiberSegment);
			
			Inli = Inli / attenuationBySpanPreAmpLinear;
			Iase = Iase / attenuationBySpanPreAmpLinear;
			
			Inli = Inli * preAmp.getGainByType(totalPower, typeOfAmplifierGain);
			Iase = Iase * preAmp.getGainByType(totalPower, typeOfAmplifierGain);
			
			//I = I / attenuationBySpanPreAmpLinear;
			//I = I * preAmp.getGainByType(totalPower, typeOfAmplifierGain);
			
			if(activeNLI){
				double beta21 = beta2;
				if(beta21 < 0.0){
					beta21 = -1.0 * beta21;
				}
				
				double mi = (3.0 * gamma * gamma) / (2.0 * Math.PI * alphaLinear * beta21 * Bsi * Bsi * Bsi);
				double ro =  Bsi * Bsi * (Math.PI * Math.PI * beta21) / (2.0 * alphaLinear);
				if(ro < 0.0) {
					ro = -1.0 * ro;
				}
				double p1 = arcsinh(ro);
				double Snli = mi * p1;
				
				Inli = Inli + Snli;
			}
			
			if(activeASE){
				double Sase = preAmp.getAseByGain(totalPower, preAmp.getGainByType(totalPower, typeOfAmplifierGain));
				Sase = Sase / polarizationModes;
				Iase = Iase + Sase;
			}
			
			// DEMUX insertion loss
			Inli = Inli / LsssLinear;
			Iase = Iase / LsssLinear;
			
			//I = I / LsssLinear;
		}
		
		double Pmax = Math.cbrt(Iase / (2.0 * Inli));
		
		return Pmax;
	}
	
	public double computePowerByExhaustiveSearch(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[]){
		
		double Pth = 0.0; //W
		double Pmin = 1.0E-11; //W, -80 dBm
		double Pmax = 1.0E-3; //W, 0 dBm
		double Pinc = 1.0E-8; //W, -40 dBm
		double SNR = 0.0;
		double SNRth =  modulation.getSNRthresholdLinear();
		
		while(Pmin < Pmax) {
			
			Pth = Pmin;
			circuit.setLaunchPowerLinear(Pmin);
			
			SNR = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
			
			if(SNR >= SNRth) {
				break;
			}
			
			Pmin += Pinc;
		}
		
		return Pth;
	}
	
	public double computePowerByBinarySearch(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[], double factorMult){
		
		double SNRth = modulation.getSNRthresholdLinear();
		double Pmax = computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned);
		
		double Pmin = 1.0E-11; //W, -80 dBm
		double Pcurrent = Pmin;
		double error = 0.01;
		double SNRdif = 0.0;
		double SNRcurrent = 0.0;
		
		SNRth = SNRth * (1.0 + factorMult);
		//SNRth = SNRth + factorMult;
		
		circuit.setLaunchPowerLinear(Pmax);
		SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if (SNRcurrent - SNRth < 0.0) {
			return Pmax;
		}
		
		circuit.setLaunchPowerLinear(Pmin);
		SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if(SNRcurrent - SNRth > 0.0) {
			return Pmin;
		}
		
		while (true) {
			
			Pcurrent = (Pmin + Pmax) / 2.0;
			
			circuit.setLaunchPowerLinear(Pcurrent);
			SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
			
			SNRdif = SNRcurrent - SNRth;
			
			if (SNRdif < error && SNRdif > 0.0) {
				break;
				
			} else {
				if(SNRdif > 0.0) {
					Pmax = Pcurrent;
					
				}else {
					Pmin = Pcurrent;
				}
			}
		}
		
		return Pcurrent;
	}
	
	public double computePowerByBinarySearch2(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[], double factorMult){
		
		double SNRth = modulation.getSNRthresholdLinear();
		double Pmax = computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned);
		
		double Pmin = 1.0E-11; //W, -80 dBm
		double Pcurrent = Pmin;
		double error = 0.01;
		double SNRdif = 0.0;
		double SNRcurrent = 0.0;
		
		SNRth = SNRth + factorMult;
		
		circuit.setLaunchPowerLinear(Pmax);
		SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if (SNRcurrent - SNRth < 0.0) {
			return Pmax;
		}
		
		circuit.setLaunchPowerLinear(Pmin);
		SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if(SNRcurrent - SNRth > 0.0) {
			return Pmin;
		}
		
		while (true) {
			
			Pcurrent = (Pmin + Pmax) / 2.0;
			
			circuit.setLaunchPowerLinear(Pcurrent);
			SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
			
			SNRdif = SNRcurrent - SNRth;
			
			if (SNRdif < error && SNRdif > 0.0) {
				break;
				
			} else {
				if(SNRdif > 0.0) {
					Pmax = Pcurrent;
					
				}else {
					Pmin = Pcurrent;
				}
			}
		}
		
		return Pcurrent;
	}
	
	public double computePowerSpectralDensityByBinarySearch(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[], double factorMult){
		
		double SNRth = modulation.getSNRthresholdLinear();
		double Pmax = computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned);
		
		double Pmin = 1.0E-11; //W, -80 dBm
		double Pcurrent = Pmin;
		double error = 0.01;
		double SNRdif = 0.0;
		double SNRcurrent = 0.0;
		
		double PSDcurrent = 0.0; //power spectral density current
    	double PSDmax = 0.0;
    	double PSDmin = 0.0;
		
		double slotBandwidth = route.getLinkList().firstElement().getSlotSpectrumBand();
		double numOfSlots = spectrumAssigned[1] - spectrumAssigned[0] + 1.0;
		double Bsi = (numOfSlots - modulation.getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
		
		PSDmax = Pmax / Bsi;
		PSDmin = Pmin / Bsi;
		
		SNRth = SNRth * (1.0 + factorMult);
		//SNRth = SNRth + factorMult;
		
		circuit.setLaunchPowerLinear(Pmax);
		SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if (SNRcurrent - SNRth < 0.0) {
			return Pmax;
		}
		
		circuit.setLaunchPowerLinear(Pmin);
		SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if(SNRcurrent - SNRth > 0.0) {
			return Pmin;
		}
		
		while (true) {
			
			PSDcurrent = (PSDmin + PSDmax) / 2.0;
			Pcurrent = PSDcurrent * Bsi;
			
			circuit.setLaunchPowerLinear(Pcurrent);
			SNRcurrent = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
			
			SNRdif = SNRcurrent - SNRth;
			
			if (SNRdif < error && SNRdif > 0.0) {
				break;
				
			} else {
				if(SNRdif > 0.0) {
					PSDmax = PSDcurrent;
					
				}else {
					PSDmin = PSDcurrent;
				}
			}
		}
		
		return Pcurrent;
	}
	
	public double computePowerByLinearInterpolation(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[]){
		
		double SNRth = modulation.getSNRthresholdLinear();
		
		double Pmin = 1.0E-11; //W, -80 dBm
		circuit.setLaunchPowerLinear(Pmin);
		double SNRmin = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if(SNRmin - SNRth > 0.0) {
			return Pmin;
		}
		
		double Pmax = computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned);
		circuit.setLaunchPowerLinear(Pmax);
		double SNRmax = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, null, false);
		
		if (SNRmax - SNRth < 0.0) {
			return Pmax;
		}
		
		// linear interpolation
		double Pchosen = Pmin + (Pmax - Pmin) * ((SNRth - SNRmin) / (SNRmax - SNRmin));
		
		return Pchosen;
	}
	
	
	public void testCamadaFisica() {
		
		int totalSlots = 320;
		double distance = 1000.0;
		
		Node n1 = new Node("1", 1000, 1000, 0, 100);
		Node n2 = new Node("2", 1000, 1000, 0, 100);
		n1.getOxc().addLink(new Link(n1.getOxc(), n2.getOxc(), totalSlots, slotBandwidth, distance));
		
		Vector<Node> listNodes = new Vector<Node>();
		listNodes.add(n1);
		listNodes.add(n2);
		
		Route route = new Route(listNodes);
		Pair pair = new Pair(n1, n2);
		
		int guardBand = 1;
		Modulation mod_BPSK = new Modulation("BPSK", 10000.0, 2.0, 5.5, 0.12, 12.5E+9, guardBand, 2.0);
		Modulation mod_QPSK = new Modulation("QPSK", 5000.0, 4.0, 8.5, 0.12, 12.5E+9, guardBand, 2.0);
		Modulation mod_8QAM = new Modulation("8QAM", 2500.0, 8.0, 12.5, 0.12, 12.5E+9, guardBand, 2.0);
		
		// circuito 1
		double tr1 = 100.0E+9; //bps
		int slotNumber1 = 5 + guardBand; //mod_QPSK.requiredSlots(tr1);
		int sa1[] = new int[2];
		sa1[0] = 1;
		sa1[1] = sa1[0] + slotNumber1 - 1;
		
		RequestForConnection requestTemp1 = new RequestForConnection();
		requestTemp1.setPair(pair);
		requestTemp1.setRequiredBandwidth(tr1);
		
		Circuit circuit1 = new Circuit();
		circuit1.setPair(pair);
		circuit1.setRoute(route);
		circuit1.setModulation(mod_QPSK);
		circuit1.setSpectrumAssigned(sa1);
		circuit1.addRequest(requestTemp1);
		
		// circuito 2
		double tr2 = 100.0E+9; //bps
		int slotNumber2 = 5 + guardBand; //mod_BPSK.requiredSlots(tr2);
		int sa2[] = new int[2];
		sa2[0] = sa1[1] + 1;
		sa2[1] = sa2[0] + slotNumber2 - 1;
		
		RequestForConnection requestTemp2 = new RequestForConnection();
		requestTemp2.setPair(pair);
		requestTemp2.setRequiredBandwidth(tr2);
		
		Circuit circuit2 = new Circuit();
		circuit2.setPair(pair);
		circuit2.setRoute(route);
		circuit2.setModulation(mod_QPSK);
		circuit2.setSpectrumAssigned(sa2);
		circuit2.addRequest(requestTemp2);
		
		// circuito 3
		double tr3 = 100.0E+9; //bps
		int slotNumber3 = 5 + guardBand; //mod_8QAM.requiredSlots(tr3);
		int sa3[] = new int[2];
		sa3[0] = sa2[1] + 1;
		sa3[1] = sa3[0] + slotNumber3 - 1;
		
		RequestForConnection requestTemp3 = new RequestForConnection();
		requestTemp3.setPair(pair);
		requestTemp3.setRequiredBandwidth(tr3);
		
		Circuit circuit3 = new Circuit();
		circuit3.setPair(pair);
		circuit3.setRoute(route);
		circuit3.setModulation(mod_QPSK);
		circuit3.setSpectrumAssigned(sa3);
		circuit3.addRequest(requestTemp3);
		
		route.getLink(0).addCircuit(circuit1);
		route.getLink(0).addCircuit(circuit2);
		route.getLink(0).addCircuit(circuit3);
		
		double c1_OSNR = computeSNRSegment(circuit1, circuit1.getRoute(), 0, circuit1.getRoute().getNodeList().size() - 1, circuit1.getModulation(), circuit1.getSpectrumAssigned(), null, false);
		double c1_OSNRdB = PhysicalLayer.ratioForDB(c1_OSNR);
		
		double c2_OSNR = computeSNRSegment(circuit2, circuit2.getRoute(), 0, circuit2.getRoute().getNodeList().size() - 1, circuit2.getModulation(), circuit2.getSpectrumAssigned(), null, false);
		double c2_OSNRdB = PhysicalLayer.ratioForDB(c2_OSNR);
		
		double c3_OSNR = computeSNRSegment(circuit3, circuit3.getRoute(), 0, circuit3.getRoute().getNodeList().size() - 1, circuit3.getModulation(), circuit3.getSpectrumAssigned(), null, false);
		double c3_OSNRdB = PhysicalLayer.ratioForDB(c3_OSNR);
		
		
		System.out.println("c1: OSNR(dB) = " + c1_OSNRdB);
		System.out.println("c2: OSNR(dB) = " + c2_OSNRdB);
		System.out.println("c3: OSNR(dB) = " + c3_OSNRdB);
		
		System.out.println("fim");
	}

}
