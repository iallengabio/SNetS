package simulationControl.parsers;

/**
 * This class represents the physical layer configuration file, its representation in entity form is 
 * important for the storage and transmission of this type of configuration in the JSON format.
 * 
 * @author Alexandre.
 */
public class PhysicalLayerConfig {

	// Allows you to enable or disable transmission quality computing
    private boolean activeQoT; // QoTN
	private boolean activeQoTForOther; // QoTO
	
	private boolean activeAse; // ativa o ruido ASE do amplificador
	private boolean activeNli; // ativa o ruido nao linear nas fibras
	private int typeOfTestQoT; // 0, para verificar pelo limiar de SNR, ou outro valor, para verificar pelo limiar de BER
	private int rateOfFEC; // 0 = 07%, ou 1 = 20%, ou 2 = 28%, outro valor, sem FEC;
	
	private double guardBand; // banda de guarda entre canais adjacentes
	
	private double power; // potencia por canal, dBm
	private double spanLength; // L, tamanho de um span, km
	private double fiberLoss; // alfa, dB/km, perda da fibra
	private double fiberNonlinearity; // gama, nao linearidade da fibra
	private double fiberDispersion; // beta2, ps^2 = E-24, parametro de dispersao
	private double dispersionCompensationRatio; // C, taxa de compensacao de dispersao
	private double centerFrequency; // v, frequencia da luz
	
	private double constantOfPlanck; // h, constante de Planck
	private double noiseFigureOfOpticalAmplifier; // NF, figura de ruido do amplificador, dB
	private double powerSaturationOfOpticalAmplifier; //pSat, potencia de saturacao do amplificador, dBm
	private double noiseFactorModelParameterA1; // A1, parametro do fator de ruido do amplificador
	private double noiseFactorModelParameterA2; // A2, parametro do fator de ruido do amplificador
	private double opticalNoiseBandwidth; // B0, largura de banda optica, esta com valor 1 porque estamos considerando SNR
	/**
	 * @return the activeQoT
	 */
	public boolean isActiveQoT() {
		return activeQoT;
	}
	/**
	 * @param activeQoT the activeQoT to set
	 */
	public void setActiveQoT(boolean activeQoT) {
		this.activeQoT = activeQoT;
	}
	/**
	 * @return the activeQoTForOther
	 */
	public boolean isActiveQoTForOther() {
		return activeQoTForOther;
	}
	/**
	 * @param activeQoTForOther the activeQoTForOther to set
	 */
	public void setActiveQoTForOther(boolean activeQoTForOther) {
		this.activeQoTForOther = activeQoTForOther;
	}
	/**
	 * @return the activeAse
	 */
	public boolean isActiveAse() {
		return activeAse;
	}
	/**
	 * @param activeAse the activeAse to set
	 */
	public void setActiveAse(boolean activeAse) {
		this.activeAse = activeAse;
	}
	/**
	 * @return the activeNli
	 */
	public boolean isActiveNli() {
		return activeNli;
	}
	/**
	 * @param activeNli the activeNli to set
	 */
	public void setActiveNli(boolean activeNli) {
		this.activeNli = activeNli;
	}
	/**
	 * @return the typeOfTestQoT
	 */
	public int getTypeOfTestQoT() {
		return typeOfTestQoT;
	}
	/**
	 * @param typeOfTestQoT the typeOfTestQoT to set
	 */
	public void setTypeOfTestQoT(int typeOfTestQoT) {
		this.typeOfTestQoT = typeOfTestQoT;
	}
	/**
	 * @return the typeOfFEC
	 */
	public int getRateOfFEC() {
		return rateOfFEC;
	}
	/**
	 * @param rateOfFEC the rateOfFEC to set
	 */
	public void setRateOfFEC(int rateOfFEC) {
		this.rateOfFEC = rateOfFEC;
	}
	/**
	 * @return the guardBand
	 */
	public double getGuardBand() {
		return guardBand;
	}
	/**
	 * @param guardBand the guardBand to set
	 */
	public void setGuardBand(double guardBand) {
		this.guardBand = guardBand;
	}
	/**
	 * @return the power
	 */
	public double getPower() {
		return power;
	}
	/**
	 * @param power the power to set
	 */
	public void setPower(double power) {
		this.power = power;
	}
	/**
	 * @return the spanLength
	 */
	public double getSpanLength() {
		return spanLength;
	}
	/**
	 * @param spanLength the spanLength to set
	 */
	public void setSpanLength(double spanLength) {
		this.spanLength = spanLength;
	}
	/**
	 * @return the fiberLoss
	 */
	public double getFiberLoss() {
		return fiberLoss;
	}
	/**
	 * @param fiberLoss the fiberLoss to set
	 */
	public void setFiberLoss(double fiberLoss) {
		this.fiberLoss = fiberLoss;
	}
	/**
	 * @return the fiberNonlinearity
	 */
	public double getFiberNonlinearity() {
		return fiberNonlinearity;
	}
	/**
	 * @param fiberNonlinearity the fiberNonlinearity to set
	 */
	public void setFiberNonlinearity(double fiberNonlinearity) {
		this.fiberNonlinearity = fiberNonlinearity;
	}
	/**
	 * @return the fiberDispersion
	 */
	public double getFiberDispersion() {
		return fiberDispersion;
	}
	/**
	 * @param fiberDispersion the fiberDispersion to set
	 */
	public void setFiberDispersion(double fiberDispersion) {
		this.fiberDispersion = fiberDispersion;
	}
	/**
	 * @return the dispersionCompensationRatio
	 */
	public double getDispersionCompensationRatio() {
		return dispersionCompensationRatio;
	}
	/**
	 * @param dispersionCompensationRatio the dispersionCompensationRatio to set
	 */
	public void setDispersionCompensationRatio(double dispersionCompensationRatio) {
		this.dispersionCompensationRatio = dispersionCompensationRatio;
	}
	/**
	 * @return the centerFrequency
	 */
	public double getCenterFrequency() {
		return centerFrequency;
	}
	/**
	 * @param centerFrequency the centerFrequency to set
	 */
	public void setCenterFrequency(double centerFrequency) {
		this.centerFrequency = centerFrequency;
	}
	/**
	 * @return the constantOfPlanck
	 */
	public double getConstantOfPlanck() {
		return constantOfPlanck;
	}
	/**
	 * @param constantOfPlanck the constantOfPlanck to set
	 */
	public void setConstantOfPlanck(double constantOfPlanck) {
		this.constantOfPlanck = constantOfPlanck;
	}
	/**
	 * @return the noiseFigureOfOpticalAmplifier
	 */
	public double getNoiseFigureOfOpticalAmplifier() {
		return noiseFigureOfOpticalAmplifier;
	}
	/**
	 * @param noiseFigureOfOpticalAmplifier the noiseFigureOfOpticalAmplifier to set
	 */
	public void setNoiseFigureOfOpticalAmplifier(double noiseFigureOfOpticalAmplifier) {
		this.noiseFigureOfOpticalAmplifier = noiseFigureOfOpticalAmplifier;
	}
	/**
	 * @return the powerSaturationOfOpticalAmplifier
	 */
	public double getPowerSaturationOfOpticalAmplifier() {
		return powerSaturationOfOpticalAmplifier;
	}
	/**
	 * @param powerSaturationOfOpticalAmplifier the powerSaturationOfOpticalAmplifier to set
	 */
	public void setPowerSaturationOfOpticalAmplifier(double powerSaturationOfOpticalAmplifier) {
		this.powerSaturationOfOpticalAmplifier = powerSaturationOfOpticalAmplifier;
	}
	/**
	 * @return the noiseFactorModelParameterA1
	 */
	public double getNoiseFactorModelParameterA1() {
		return noiseFactorModelParameterA1;
	}
	/**
	 * @param noiseFactorModelParameterA1 the noiseFactorModelParameterA1 to set
	 */
	public void setNoiseFactorModelParameterA1(double noiseFactorModelParameterA1) {
		this.noiseFactorModelParameterA1 = noiseFactorModelParameterA1;
	}
	/**
	 * @return the noiseFactorModelParameterA2
	 */
	public double getNoiseFactorModelParameterA2() {
		return noiseFactorModelParameterA2;
	}
	/**
	 * @param noiseFactorModelParameterA2 the noiseFactorModelParameterA2 to set
	 */
	public void setNoiseFactorModelParameterA2(double noiseFactorModelParameterA2) {
		this.noiseFactorModelParameterA2 = noiseFactorModelParameterA2;
	}
	/**
	 * @return the opticalNoiseBandwidth
	 */
	public double getOpticalNoiseBandwidth() {
		return opticalNoiseBandwidth;
	}
	/**
	 * @param opticalNoiseBandwidth the opticalNoiseBandwidth to set
	 */
	public void setOpticalNoiseBandwidth(double opticalNoiseBandwidth) {
		this.opticalNoiseBandwidth = opticalNoiseBandwidth;
	}
	
}
