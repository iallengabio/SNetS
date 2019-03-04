package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import simulationControl.parsers.PhysicalLayerConfig;

/**
 * This class represents the physical layer of the optical network.
 * 
 * @author Alexandre
 */
public class PhysicalLayer implements Serializable {

	// Allows you to enable or disable transmission quality computing
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
	 * @return boolean
	 */
	public boolean isAdmissibleModultion(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[]){
		double SNR = computeSNRSegment(circuit, route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, true);
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
	 * @return boolean
	 */
	public boolean isAdmissibleModultionBySegment(Circuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[]){
		double SNR = computeSNRSegment(circuit, route, sourceNodeIndex, destinationNodeIndex, modulation, spectrumAssigned, true);
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
	 * @param checksOnTotalPower boolean - Used to verify if the spectrum allocated by the request is considered or not in the calculation 
	 *                                     of the total power that enters the amplifiers (true, considers, or false, does not consider)
	 * @return double - SNR (linear)
	 */
	public double computeSNRSegment(Circuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[], boolean checksOnTotalPower){
		
		double I = linearPower / referenceBandwidth; // Signal power density for the reference bandwidth
		double Iase = 0.0;
		double Inli = 0.0;
		
		double numSlotsRequired = spectrumAssigned[1] - spectrumAssigned[0] + 1; // Number of slots required
		double Bsi = (numSlotsRequired - modulation.getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
		double fi = lowerFrequency + (slotBandwidth * (spectrumAssigned[0] - 1.0)) + (Bsi / 2.0); // Central frequency of circuit
		
		Node sourceNode = null;
		Node destinationNode = null;
		Link link = null;
		
		double Ns = 0.0; // Number of line amplifiers
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
			Ns = getNumberOfLineAmplifiers(link.getDistance());
			
			if(activeNLI){
				noiseNli = (Ns + 1.0) * getGnli(circuit, link, linearPower, Bsi, I, fi); // Ns + 1 corresponds to the line amplifiers span more the preamplifier span
				Inli = Inli + noiseNli;
			}
			
			if(activeASE){
				if(typeOfAmplifierGain == 1){
					totalPower = getTotalPowerInTheLink(link, linearPower, Bsi, I, numSlotsRequired, checksOnTotalPower);
				}

				lastFiberSegment = link.getDistance() - (Ns * L);
				if(lastFiberSegment != L){ // check if need to recalculate the gain of the preamplifier
					double spanMeter = lastFiberSegment * 1000; // span in meter
					double preAmpGainLinear = Math.pow(Math.E, alphaLinear * spanMeter) * ratioOfDB(Lsss);
					preAmp.setGain(ratioForDB(preAmpGainLinear));
				}
				
				boosterAmpNoiseAse = boosterAmp.getAseByGain(totalPower, boosterAmp.getGainByType(totalPower, typeOfAmplifierGain));
				lineAmpNoiseAse = Ns * lineAmp.getAseByGain(totalPower, lineAmp.getGainByType(totalPower, typeOfAmplifierGain));
				preAmpNoiseAse = preAmp.getAseByGain(totalPower, preAmp.getGainByType(totalPower, typeOfAmplifierGain));
				
				Iase = Iase + (boosterAmpNoiseAse + lineAmpNoiseAse + preAmpNoiseAse);
			}
		}
		
		if(!fixedPowerSpectralDensity){
			I = linearPower / Bsi; // Signal power spectral density calculated according to the requested bandwidth
		}
		
		double SNR = I / (Iase + Inli);
		
		return SNR;
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
	public double getTotalPowerInTheLink(Link link, double powerI, double Bsi, double I, double numSlotsRequired, boolean checksOnTotalPower){
		double totalPower = 0.0;
		double circuitPower = 0.0;
		int saj[] = null;
		double numOfSlots = 0.0;
		double Bsj = 0.0;
		double powerJ = powerI;
		
		if(checksOnTotalPower){ // Check if it is to add the power of the circuit under test
			totalPower = powerI;
			if(fixedPowerSpectralDensity){
				totalPower = I * Bsi;
			}
		}
		
		TreeSet<Circuit> listCircuits = link.getCircuitList();
		for(Circuit cricuitJ : listCircuits){
			
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
	 * @param circuitI Circuit
	 * @param link Link
	 * @param powerI double
	 * @param BsI double
	 * @param I double
	 * @param fI double
	 * @return double
	 */
	public double getGnli(Circuit circuitI, Link link, double powerI, double BsI, double I, double fI){
		double beta21 = beta2;
		if(beta21 < 0.0){
			beta21 = -1.0 * beta21;
		}
		
		double Gi = I; // Power spectral density of the circuit i
		if(!fixedPowerSpectralDensity){
			Gi = powerI / BsI; // Power spectral density of the circuit i calculated according to the required bandwidth
		}

		double mi = Gi * (3.0 * gamma * gamma) / (2.0 * Math.PI * alphaLinear * beta21);
		double ro =  BsI * BsI * (Math.PI * Math.PI * beta21) / (2.0 * alphaLinear);
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
		double powerJ = powerI;
		double Gj = I; // Power spectral density of the circuit j
		
		TreeSet<Circuit> listCircuits = link.getCircuitList();
		for(Circuit cricuitJ : listCircuits){
			
			if(!circuitI.equals(cricuitJ)){
				saJ = cricuitJ.getSpectrumAssignedByLink(link);
				numOfSlots = saJ[1] - saJ[0] + 1.0;
				
				Bsj = (numOfSlots - cricuitJ.getModulation().getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
				fJ = lowerFrequency + (slotBandwidth * (saJ[0] - 1.0)) + (Bsj / 2.0); // Central frequency of circuit
				
				if(!fixedPowerSpectralDensity){
					Gj = powerJ / Bsj; // Power spectral density of the circuit j calculated according to the required bandwidth
				}
				
				deltaFij = fI - fJ;
				if(deltaFij < 0.0)
					deltaFij = -1.0 * deltaFij;
				
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
	 */
	public void computesDistances(Mesh mesh, List<Modulation> avaliableModulations) {
		
		double totalSlots = mesh.getLinkList().firstElement().getNumOfSlots();
		double beta21 = this.beta2;
		if(beta21 < 0.0){
			beta21 = -1.0 * beta21;
		}
		
		Vector<Link> linkList = mesh.getLinkList();
		double sumLastFiberSegment = 0.0;
		for(int l = 0; l < linkList.size(); l++) {
			double Ns = getNumberOfLineAmplifiers(linkList.get(l).getDistance());
			double lastFiberSegment = linkList.get(l).getDistance() - (Ns * L);
			sumLastFiberSegment += lastFiberSegment;
		}
		double averageLastFiberSegment = sumLastFiberSegment / linkList.size();
		
		Amplifier boosterAmp = new Amplifier(Lsss, pSat, NF, h, centerFrequency, 0, A1, A2);
		Amplifier lineAmp = new Amplifier(L * alpha, pSat, NF, h, centerFrequency, 0, A1, A2);
		Amplifier preAmp = new Amplifier((averageLastFiberSegment * alpha) + Lsss, pSat, NF, h, centerFrequency, 0, A1, A2);
		
		double Pout = PhysicalLayer.ratioOfDB(power) * 1.0E-3; //W, potencia de sinal no transmissor
		double fs = referenceBandwidth;
		
		//double Rs = 28.0E+9; //Baud, taxa de simbolo
		double transmissionRate = 10.0E+9; //bps
		int guardBandSlot = 1;
		double guardBand = guardBandSlot * 12.5E+9;
		
		double pinTotal = -35.0; //dBm
		double pinTotalLinear = PhysicalLayer.ratioOfDB(pinTotal) * 1.0E-3; //Watt
		
		int quantTotalSpansPorEnlace = (int)(30000.0 / L); // quantidade de spans por enlaces
		int quantEnlaces = 1; // quantidade de enlaces
		
		HashMap<Modulation, Double> distModulations = new HashMap<Modulation, Double>(avaliableModulations.size());
		for(int m = 0; m < avaliableModulations.size(); m++) {
			distModulations.put(avaliableModulations.get(m), 0.0);
		}
		
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			
			int slotNumber = mod.requiredSlots(transmissionRate);
			double Bsi = (slotNumber - guardBandSlot) * fs; //Hz
			double circuitSeparation = Bsi + guardBand; //Hz
			
			double G = Pout / fs; //densidade espectral de potencia para um slot
			double Gi = Pout / Bsi; //densidade espectral de potencia do sinal do circuito i
			Gi = G; // para manter a densidade espectral de potencia fixa
			
			int quantCircuitos = (int)(totalSlots / slotNumber); //quantidade de circuitos
			
			ArrayList<Double> cricuitsFrequencies = new ArrayList<Double>(quantCircuitos); //frequencias dos circcuitos
			for(int c = 0; c < quantCircuitos; c++){
				cricuitsFrequencies.add(lowerFrequency + (circuitSeparation * c) + (Bsi / 2.0));
			}
			
			//double k = (cricuitsFrequencies.get(1) - cricuitsFrequencies.get(0)) / Bsi;
			//System.out.println("k = " + k);
			
			for(int ns = 1; ns <= quantTotalSpansPorEnlace; ns++){
				int quantSpansPorEnlace = ns;
				
				for(int c = 0; c < quantCircuitos; c++){
					double fi = cricuitsFrequencies.get(c);
						
					double Nout = 0.0;
					double distance = 0.0;
					
					for(int l = 0; l < quantEnlaces; l++){
						
						//ASE
						double boosterAmpGain = boosterAmp.getGainByType(pinTotalLinear, 0);
						double boosterAmpAse = boosterAmp.getAseByGain(pinTotalLinear, boosterAmpGain); //Watt
						
						double preAmpGain = preAmp.getGainByType(pinTotalLinear, 0);
						double preAmpAse = preAmp.getAseByGain(pinTotalLinear, preAmpGain); //Watt
						
						double lineAmpGain = lineAmp.getGainByType(pinTotalLinear, 0);
						double lineAmpAse = lineAmp.getAseByGain(pinTotalLinear, lineAmpGain); //Watt
						lineAmpAse = (quantSpansPorEnlace - 1.0) * lineAmpAse; //retira o span do pre amplificador
						
						Nout = Nout + (boosterAmpAse + lineAmpAse + preAmpAse);
						
						//NLI
						double mi = Gi * (3.0 * gamma * gamma) / (2.0 * Math.PI * alphaLinear * beta21);
						double ro = Bsi * Bsi * (Math.PI * Math.PI * beta21) / (2.0 * alphaLinear);
						double p1 = Gi * Gi * PhysicalLayer.arcsinh(ro);
						double p2 = 0.0;
						
						for(int j = 0; j < quantCircuitos; j++){
							
							if(c != j){
								double Bsj = Bsi;
								double fj = cricuitsFrequencies.get(j); //frequencia central do circuito j
								
								double deltaFij = fi - fj;
								if(deltaFij < 0.0){
									deltaFij = -1.0 * deltaFij;
								}
								
								double d1 = deltaFij + (Bsj / 2.0);
								double d2 = deltaFij - (Bsj / 2.0);
								
								double d3 = d1 / d2;
								if(d3 < 0.0){
									d3 = -1.0 * d3;
								}
								
								double Gj = Pout / Bsj; //densidade espectral de potencia do sinal do circuito j
								Gj = G; // para manter a densidade espectral de potencia fixa
								
								double ln = Gj * Gj * Math.log(d3);
								p2 += ln;
							}
						}
						
						double gnli = mi * (p1 + p2); 
						gnli = quantSpansPorEnlace * gnli; //
						
						Nout = Nout + gnli;
						
						distance = distance + ((quantSpansPorEnlace - 1.0) * L) + averageLastFiberSegment;
					}
					
					double OSNR = Gi / Nout;
					double OSNRdB = PhysicalLayer.ratioForDB(OSNR);
					
					double modDist = distModulations.get(mod);
					if((OSNRdB >= mod.getSNRthreshold()) && (distance > modDist)){
						distModulations.put(mod, distance);
					}
					
					//System.out.println("distance (km) = " + distance);
					//System.out.println("c" + (c + 1) + ", p(dBm) = " + power + ", Ns: " + quantSpansPorEnlace + ", OSNR(dB) : " + OSNRdB);
				}	
			}
		}
		
		for(int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			mod.setMaxRange(distModulations.get(mod));
			
			//System.out.println("Mod = " + mod.getName() + ", distance = " + distModulations.get(mod));
		}
	}
	
}
