package network;

import java.util.List;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import simulationControl.parsers.PhysicalLayerConfig;

/**
 * This class represents the physical layer of the optical network.
 * 
 * @author Alexandre
 */
public class PhysicalLayer {

	// Allows you to enable or disable transmission quality computing
    private boolean activeQoT; // QoTN
    private boolean activeQoTForOther; // QoTO
	
    private boolean activeAse; // Active the ASE noise of the amplifier
    private boolean activeNli; // Active nonlinear noise in the fibers
    private int typeOfTestQoT; //0, To check for the SNR threshold, or another value, to check for the BER threshold
    private int rateOfFEC; //0 = 07%, or 1 = 20%, or 2 = 28%, other value, without FEC;
	
    private double guardBand; // Guard band between adjacent channels
	
    private double power; // Power per channel, dBm
    private double L; // Size of a span, km
    private double alpha; //dB/km, Fiber loss
    private double gamma; //Fiber nonlinearity
    private double beta2; //ps^2 = E-24, Dispersion parameter
    private double C; //Dispersion compensation rate
    private double centerFrequency; //Frequency of light
	
    private double h; // Constant of Planck
    private double NF; // Amplifier noise figure, dB
    private double pSat; // Saturation power of the amplifier, dBm
    private double A1; // Amplifier noise factor parameter, a1
    private double A2; // Amplifier noise factor parameter, A2
    private double B0; // Optical bandwidth, it has a value of 1 because we are considering SNR

	/**
	 * Creates a new instance of PhysicalLayerConfig
	 * 
	 * @param plc PhysicalLayerConfig
	 */
    public PhysicalLayer(PhysicalLayerConfig plc){
        this.activeQoT = plc.isActiveQoT();
        this.activeQoTForOther = plc.isActiveQoTForOther();
    	
        this.activeAse = plc.isActiveAse();
        this.activeNli = plc.isActiveNli();
        this.typeOfTestQoT = plc.getTypeOfTestQoT();
        this.rateOfFEC = plc.getRateOfFEC();
    	
        this.guardBand = plc.getGuardBand();
    	
        this.power = plc.getPower();
        this.L = plc.getSpanLength();
        this.alpha = plc.getFiberLoss();
        this.gamma = plc.getFiberNonlinearity();
        this.beta2 = plc.getFiberDispersion();
        this.C = plc.getDispersionCompensationRatio();
        this.centerFrequency = plc.getCenterFrequency();
    	
        this.h = plc.getConstantOfPlanck();
        this.NF = plc.getNoiseFigureOfOpticalAmplifier();
        this.pSat = plc.getPowerSaturationOfOpticalAmplifier();
        this.A1 = plc.getNoiseFactorModelParameterA1();
        this.A2 = plc.getNoiseFactorModelParameterA2();
        this.B0 = plc.getOpticalNoiseBandwidth();
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
	 * Returns the Size of a span (km)
	 * 
	 * @return the L
	 */
	public double getSpanLength() {
		return L;
	}

	public double getFEC(){
		double fec = 0.0;
		
		if(rateOfFEC == 0){
			fec = 0.07; // FEC = 7%
		}else if(rateOfFEC == 1){
			fec = 0.20; // FEC = 20%
		}else if(rateOfFEC == 2){
			fec = 0.28; // FEC = 28%
		}
		
		return fec;
	}
	
	public double getBERthreshold(){
		double BERthreshold = 1.0E-5;
		
		if(rateOfFEC == 0){
			BERthreshold = 3.8E-3; // FEC = 7%
		}else if(rateOfFEC == 1){
			BERthreshold = 2.0E-2; // FEC = 20%
		}else if(rateOfFEC == 2){
			BERthreshold = 4.0E-2; // FEC = 28%
		}
		
		return BERthreshold;
	}
	
	/**
	 * Based on articles:
	 *  - Physical Layer Transmitter and Routing Optimization to Maximize the Traffic Throughput of a Nonlinear Optical Mesh Network
	 *  - Quantifying the Impact of Non-linear Impairments on Blocking Load in Elastic Optical Networks
	 * 
	 * @param modulation Modulation
	 * @return double
	 */
	public double getSNRthreshold(Modulation modulation){
		double SNRdBthreshold = 3.0 * modulation.getLevel();
		return SNRdBthreshold;
	}
	
	public double getGuardBand(){
		return guardBand;
	}
	
	public boolean isAdmissible(Modulation modulation, double SNRdB, double SNRlinear){
		if(typeOfTestQoT == 0){ //Check by SNR limit (dB)
			double SNRdBthreshold = getSNRthreshold(modulation);
			
			if(SNRdB >= SNRdBthreshold){
				return true;
			}
		} else { //BER threshold check
			double BERthreshold = getBERthreshold();
			
			double k2 = modulation.getK2();
			double Lm = modulation.getLevel();
			double M = modulation.getM();
			double BER = getBER(SNRlinear, k2, Lm, M);
			
			if(BER <= BERthreshold){
				return true;
			}
		}
		return false;
	}
	
	public boolean isAdmissibleModultion(Circuit circuit, Route route, Modulation modulation, int spectrumAssigned[]){
		
		double SNR = computeSNRSegment(circuit, circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, modulation, spectrumAssigned, true);
		double SNRdB = ratioForDB(SNR);
		circuit.setSNR(SNRdB);
		
		boolean QoT = isAdmissible(modulation, SNRdB, SNR);
		
		return QoT;
	}
	
	public boolean isAdmissibleModultionBySegment(Circuit circuit, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[]){
		
		double SNR = computeSNRSegment(circuit, circuit.getRequiredBandwidth(), route, sourceNodeIndex, destinationNodeIndex, modulation, spectrumAssigned, true);
		double SNRdB = ratioForDB(SNR);
		circuit.setSNR(SNRdB);
		
		boolean QoT = isAdmissible(modulation, SNRdB, SNR);
		
		return QoT;
	}
	
	/**
	 * Based on articles: 
	 *  - Nonlinear Impairment Aware Resource Allocation in Elastic Optical Networks (2015)
	 *  - Modeling of Nonlinear Signal Distortion in Fiber-Optic Networks (2014)
	 * @param request - Request
	 * @param bandwidth - double
	 * @param route - Route
	 * @param sourceNodeIndex - int - Segment start node index
	 * @param destinationNodeIndex - int - Segment end node index
	 * @param modulation - Modulation
	 * @param spectrumAssigned - int[]
	 * @param checksOnTotalPower - boolean - Used to verify if the spectrum allocated by the request is considered or not in the calculation 
	 *                             of the total power that enters the amplifiers (true, considers, or false, does not consider)
	 * @return double - SNR (linear)
	 */
	public double computeSNRSegment(Circuit circuit, double bandwidth, Route route, int sourceNodeIndex, int destinationNodeIndex, Modulation modulation, int spectrumAssigned[], boolean checksOnTotalPower){
		
		double Ptx = ratioOfDB(power) * 1.0E-3; //W, Transmitter power
		double Pase = 0.0;
		double Pnli = 0.0;
		
		int numSlotsRequired = spectrumAssigned[1] - spectrumAssigned[0] + 1; // Number of slots required
		double fs = route.getLinkList().firstElement().getSlotSpectrumBand(); //Hz
		double Bsi = numSlotsRequired * fs; // Request bandwidth
		
		double slotsTotal = route.getLinkList().firstElement().getNumOfSlots();
		double lowerFrequency = centerFrequency - (fs * (slotsTotal / 2.0)); //Hz, Half slots are removed because center Frequency = 193.0E + 12 is the central frequency of the optical spectrum
		double fi = lowerFrequency + (fs * (spectrumAssigned[0] - 1)) + (Bsi / 2); // Central frequency of request
		
		double I = Ptx / (fs * 4); // Signal power density for 4 slots
		
		Node sourceNode = null;
		Node destinationNode = null;
		Link link = null;
		
		double G0 = alpha * L; // Gain in dB of the amplifier
		Amplifier amp = new Amplifier(G0, pSat, NF, h, centerFrequency, B0, 0.0, A1, A2);
		amp.setActiveAse(1); // Active the ASE noise
		amp.setTypeGainAmplifier(1); // Sets the gain type to fixed
		
		for(int i = sourceNodeIndex; i < destinationNodeIndex; i++){
			sourceNode = route.getNode(i);
			destinationNode = route.getNode(i + 1);
			link = sourceNode.getOxc().linkTo(destinationNode.getOxc());
			double Ns = PhysicalLayer.roundUp(link.getDistance() / L); // Number of spans
			
			double numSlotsUsed = link.getUsedSlots();
			if(checksOnTotalPower){
				numSlotsUsed += numSlotsRequired;
			}
			
			if(activeNli){
				double noiseNli = Ns * getGnli(circuit, link, I, Bsi, fi, gamma, beta2, alpha, L, C, Ns, lowerFrequency);
				Pnli = Pnli + noiseNli;
			}
			
			if(activeAse){
				double totalPower = numSlotsUsed * fs * I;
				double noiseAse = Ns * 2.0 * amp.getAseByTypeGain(totalPower, centerFrequency);
				Pase = Pase + noiseAse;
			}
		}
		
		double SNR = I / (Pase + Pnli);
		
		return SNR;
	}
	
	/**
	 * Based on article:
	 *  - Nonlinear Impairment Aware Resource Allocation in Elastic Optical Networks (2015)
	 * 
	 * @param circuit Circuit
	 * @param link Link
	 * @param I double
	 * @param Bsi double
	 * @param fi double
	 * @param gama double
	 * @param beta2 double
	 * @param alfa double
	 * @param L double
	 * @param C double
	 * @param Ns double
	 * @param lowerFrequency double
	 * @return double
	 */
	public double getGnli(Circuit circuit, Link link, double I, double Bsi, double fi, double gama, double beta2, double alfa, double L, double C, double Ns, double lowerFrequency){
		double alfaLinear = ratioOfDB(alfa);
		if(beta2 < 0.0){
			beta2 = -1.0 * beta2;
		}
		
		double mi = (3.0 * gama * gama * I * I * I) / (2.0 * Math.PI * alfaLinear * beta2);
		
		double ro = (Math.PI * Math.PI * beta2) / (2.0 * alfaLinear);
		double p1 = arcsinh(ro * Bsi * Bsi);
		double p2 = 0.0;
		
		List<Circuit> listRequests = link.getCircuitList();
		int size = listRequests.size();
		for(int i = 0; i < size; i++){
			Circuit cricuitTemp = listRequests.get(i);
			
			if(!circuit.equals(cricuitTemp)){
				double fs = link.getSlotSpectrumBand();
				int sa[] = cricuitTemp.getSpectrumAssignedByLink(link);
				double numOfSlots = sa[1] - sa[0] + 1;
				double Bsj = numOfSlots * fs; //Request bandwidth
				double fj = lowerFrequency + (fs * (sa[0] - 1)) + (Bsj / 2); //Central frequency of request
				
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
	 * @param x - double
	 * @return double
	 */
	public static double arcsinh(double x){
		return Math.log(x + Math.sqrt(x * x + 1.0));
	}
	
	/**
	 * Based on articles:
	 * - Error Vector Magnitude as a Performance Measure for Advanced Modulation Formats (equation 4)
     * - On the Extended Relationships Among EVM, BER and SNR as Performance Metrics (equation 12)
     * 
     * @param SNR double
     * @param k2 double
     * @param L double
     * @param M double
     * @return double
	 */
	public static double getBER(double SNR, double k2, double L, double M){
		double p1 = ((3.0 * log2(L)) / ((L * L) - 1.0)) * ((Math.sqrt(2.0) * SNR) / (k2 * log2(M)));
		double p2 = erfc(Math.sqrt(p1));
		double ber = ((1.0 - (1.0 / L)) / log2(L)) * p2;
		return ber;
	}
	
	/**
	 * complementary error function
	 * 
	 * @param x double
	 * @return double
	 */
	public static double erfc(double x){
		return (1.0 - erf(x));
	}
	
	/**
	 * error function - approximation
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
		double dB;
		dB = 10.0 * Math.log10(ratio);
		return dB;
	}

	/**
	 * Converts a value in dB to a linear value (ratio)
	 * 
	 * @param dB double
	 * @return double ratio
	 */
	public static double ratioOfDB(double dB) {
		double ratio;
		ratio = Math.pow(10.0, (dB / 10.0));
		return ratio;
	}
	
	/**
	 * Logarithm in base 2
	 * 
	 * @param x
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
}
