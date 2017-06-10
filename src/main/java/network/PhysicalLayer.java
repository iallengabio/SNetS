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
	
    private boolean activeAse; //ativa o ruido ASE do amplificador
    private boolean activeNli; //ativa o ruido nao linear nas fibras
    private int typeOfTestQoT; //0, para verificar pelo limiar de SNR, ou outro valor, para verificar pelo limiar de BER
    private int rateOfFEC; //0 = 07%, ou 1 = 20%, ou 2 = 28%, outro valor, sem FEC;
	
    private double guardBand; //banda de guarda entre canais adjacentes
	
    private double power; //potencia por canal, dBm
    private double L; //tamanho de um span, km
    private double alfa; //dB/km, perda da fibra
    private double gama; //nao linearidade da fibra
    private double beta2; //ps^2 = E-24, parametro de dispersao
    private double C; //taxa de compensacao de dispersao
    private double centerFrequency; //frequencia da luz
	
    private double h; //constante de Planck
    private double NF; //figura de ruido do amplificador, dB
    private double pSat; //potencia de saturacao do amplificador, dBm
    private double A1; //parametro do fator de ruido do amplificador
    private double A2; //parametro do fator de ruido do amplificador
    private double B0; //largura de banda optica, esta com valor 1 porque estamos considerando SNR

	
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
        this.alfa = plc.getFiberLoss();
        this.gama = plc.getFiberNonlinearity();
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

	public double getFEC(){
		double fec = 0.0;
		
		if(rateOfFEC == 0){
			fec = 0.07; //para FEC = 7%
		}else if(rateOfFEC == 1){
			fec = 0.20; //para FEC = 20%
		}else if(rateOfFEC == 2){
			fec = 0.28; //para FEC = 28%
		}
		
		return fec;
	}
	
	public double getBERthreshold(){
		double BERthreshold = 1.0E-5;
		
		if(rateOfFEC == 0){
			BERthreshold = 3.8E-3; //para FEC = 7%
		}else if(rateOfFEC == 1){
			BERthreshold = 2.0E-2; //para FEC = 20%
		}else if(rateOfFEC == 2){
			BERthreshold = 4.0E-2; //para FEC = 28%
		}
		
		return BERthreshold;
	}
	
	/**
	 * Artigos:
	 * Physical Layer Transmitter and Routing Optimization to Maximize the Traffic Throughput of a Nonlinear Optical Mesh Network
	 * Quantifying the Impact of Non-linear Impairments on Blocking Load in Elastic Optical Networks
	 * 
	 * @param modulation
	 * @return
	 */
	public double getSNRthreshold(Modulation modulation){
		double SNRdBthreshold = 3.0 * modulation.getLevel();
		return SNRdBthreshold;
	}
	
	public double getGuardBand(){
		return guardBand;
	}
	
	public boolean isAdmissible(Modulation modulation, double SNRdB, double SNRlinear){
		if(typeOfTestQoT == 0){ //verificacao pelo limitar de SNR (dB)
			double SNRdBthreshold = getSNRthreshold(modulation);
			
			if(SNRdB >= SNRdBthreshold){
				return true;
			}
		} else { //verificacao pelo limiar de BER
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
	
	public boolean isAdmissibleModultionBySegment(Circuit circuit, Route route, int indexNodeSource, int indexNodeDestination, Modulation modulation, int spectrumAssigned[]){
		
		double SNR = computeSNRSegment(circuit, circuit.getRequiredBandwidth(), route, indexNodeSource, indexNodeDestination, modulation, spectrumAssigned, true);
		double SNRdB = ratioForDB(SNR);
		circuit.setSNR(SNRdB);
		
		boolean QoT = isAdmissible(modulation, SNRdB, SNR);
		
		return QoT;
	}
	
	
	/**
	 * Artigo: Nonlinear Impairment Aware Resource Allocation in Elastic Optical Networks (2015)
	 *         Modeling of Nonlinear Signal Distortion in Fiber-Optic Networks (2014)
	 * @param request - Request
	 * @param bandwidth - double
	 * @param route - Route
	 * @param indexNodeSource - int - Indice do node do inicio do segmento
	 * @param indexNodeDestination - int - Indice do node do final do segmento
	 * @param modulation - Modulation
	 * @param spectrumAssigned - int[]
	 * @param verifQoT - boolean - Utilizado para verificar se o espectro alocado pela requisicao eh considerado ou nao 
	 *                             no calculo da potencia total que entra nos amplificadores (true, considera, ou false, nao considera)
	 * @return double - SNR (linear)
	 */
	public double computeSNRSegment(Circuit circuit, double bandwidth, Route route, int indexNodeSource, int indexNodeDestination, Modulation modulation, int spectrumAssigned[], boolean verifQoT){
		
		double Ptx = ratioOfDB(power) * 1.0E-3; //W, potencia do transmissor
		double Pase = 0.0;
		double Pnli = 0.0;
		
		int quantSlotsRequeridos = spectrumAssigned[1] - spectrumAssigned[0] + 1; //quantidade de slots requeridos
		double fs = route.getLinkList().firstElement().getSlotSpectrumBand(); //Hz
		double Bsi = quantSlotsRequeridos * fs; //largura da banda da requisicao
		
		double totalSlots = route.getLinkList().firstElement().getNumOfSlots();
		double lowerFrequency = centerFrequency - (fs * (totalSlots / 2.0)); //Hz, retira-se a metade de slots porque centerFrequency = 193.0E+12 eh a frequencia central do espectro optico
		double fi = lowerFrequency + (fs * (spectrumAssigned[0] - 1)) + (Bsi / 2); //frequencia central da requisicao
		
		double I = Ptx / (fs * 4); //densidade de potencia do sinal para 4 slots
		
		Node noOrigem = null;
		Node noDestino = null;
		Link enlace = null;
		
		double G0 = alfa * L; //ganho em dB do amplificador
		Amplifier amp = new Amplifier(G0, pSat, NF, h, centerFrequency, B0, 0.0, A1, A2);
		amp.setActiveAse(1); //ativa o ruido ASE
		amp.setTypeGainAmplifier(1); //seta o tipo de ganho como fixo
		
		for(int i = indexNodeSource; i < indexNodeDestination; i++){
			noOrigem = route.getNode(i);
			noDestino = route.getNode(i + 1);
			enlace = noOrigem.getOxc().linkTo(noDestino.getOxc());
			double Ns = PhysicalLayer.roundUp(enlace.getDistance() / L); //numero de spans
			
			double quantSlotsUsados = enlace.getUsedSlots();
			if(verifQoT){
				quantSlotsUsados += quantSlotsRequeridos;
			}
			
			if(activeNli){
				double noiseNli = Ns * getGnli(circuit, enlace, I, Bsi, fi, gama, beta2, alfa, L, C, Ns, lowerFrequency);
				Pnli = Pnli + noiseNli;
			}
			
			if(activeAse){
				double pinTotal = quantSlotsUsados * fs * I;
				double noiseAse = Ns * 2.0 * amp.getAseByTypeGain(pinTotal, centerFrequency);
				Pase = Pase + noiseAse;
			}
		}
		
		double SNR = I / (Pase + Pnli);
		
		return SNR;
	}
	
	
	//-----------------------------------------------------------------------------
	//Artigo: Nonlinear Impairment Aware Resource Allocation in Elastic Optical Networks (2015)
	public double getGnli(Circuit circuit, Link link, double I, double Bsi, double fi, double gama, double beta2, double alfa, double L, double C, double Ns, double lowerFrequency){
		double alfaLinear = ratioOfDB(alfa);
		if(beta2 < 0.0){
			beta2 = -1.0 * beta2;
		}
		
		//double he = getHe(Ns, alfa, L, Math.E, C);
		//double mi = (3.0 * gama * gama * I * I * I * he) / (2.0 * Math.PI * alfaLinear * beta2);
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
				int sa[] = cricuitTemp.getSpectrumAssigned();
				double numOfSlots = sa[1] - sa[0] + 1;
				double Bsj = numOfSlots * fs; //largura de banda da requisicao
				double fj = lowerFrequency + (fs * (sa[0] - 1)) + (Bsj / 2); //frequencia central da requisicao
				
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
	 * Funcao que retorna o seno hiperbolico inverso do argumento
	 * asinh == arcsinh
	 * @param x - double
	 * @return double
	 */
	public static double arcsinh(double x){
		return Math.log(x + Math.sqrt(x * x + 1.0));
	}
	
	
	
	
	/**
	 * Artigos:
	 * - Error Vector Magnitude as a Performance Measure for Advanced Modulation Formats (equacao 4)
     * - On the Extended Relationships Among EVM, BER and SNR as Performance Metrics (equacao 12)
	 */
	public static double getBER(double SNR, double k2, double L, double M){
		double p1 = ((3.0 * log2(L)) / ((L * L) - 1.0)) * ((Math.sqrt(2.0) * SNR) / (k2 * log2(M)));
		double p2 = erfc(Math.sqrt(p1));
		double ber = ((1.0 - (1.0 / L)) / log2(L)) * p2;
		return ber;
	}
	
	//complementary error function
	public static double erfc(double x){
		return (1.0 - erf(x));
	}
	
	//error function - aproximacao
	//http://www.galileu.esalq.usp.br/mostra_topico.php?cod=240
	public static double erf(double x){
		double a = 0.140012;
		double v = sgn(x) * Math.sqrt(1.0 - Math.exp(-1.0 * (x * x) * (((4.0 / Math.PI) + (a * x * x)) / (1.0 + (a * x * x)))));
		return v;
	}
	
	//funcao sinal
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
	 * Converte uma razao (linear) para decibel
	 * @param ratio
	 * @return dB
	 */
	public static double ratioForDB(double ratio) {
		double dB;
		dB = 10.0 * Math.log10(ratio);
		return dB;
	}

	/**
	 * Converte um valor em dB para um valor linear (ratio)
	 * @param dB
	 * @return ratio
	 */
	public static double ratioOfDB(double dB) {
		double ratio;
		ratio = Math.pow(10.0, (dB / 10.0));
		return ratio;
	}
	
	/**
	 * Logaritmo na base 2
	 * @param x
	 * @return double
	 */
	public static double log2(double x){
		return (Math.log10(x) / Math.log10(2.0));
	}
	
	/**
	 * Arredonda para cima um valor double para int
	 * @param res
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