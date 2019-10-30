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
	
	private boolean activeASE; // Active the ASE noise of the amplifier
	private boolean activeNLI; //  Active nonlinear noise in the fibers
	
	private double rateOfFEC; // Rate of FEC (Forward Error Correction), The most used rate is 7% which corresponds to the BER of 3.8E-3
	private int typeOfTestQoT; // 0, To check for the SNR threshold (Signal-to-Noise Ratio), or another value, to check for the BER threshold (Bit Error Rate)
	
	private double power; // Power per channel, dBm
	private double spanLength; // L, Size of a span, km
	private double fiberLoss; // alpha, dB/km, Fiber loss
	private double fiberNonlinearity; // gamma, Fiber nonlinearity
	private double fiberDispersion; // beta2, ps^2 = E-24, Dispersion parameter
	private double centerFrequency; // v, Frequency of light
	
	private double constantOfPlanck; // h, Constant of Planck
	private double noiseFigureOfOpticalAmplifier; // NF, Amplifier noise figure, dB
	private double powerSaturationOfOpticalAmplifier; //pSat, Saturation power of the amplifier, dBm
	private double noiseFactorModelParameterA1; // A1, Amplifier noise factor parameter
	private double noiseFactorModelParameterA2; // A2, Amplifier noise factor parameter
	private int typeOfAmplifierGain; // Type of amplifier gain, 0 to fixed gain and 1 to saturated gain
	private double amplificationFrequency; // Frequency used for amplification
	
	private double switchInsertionLoss; // dB
	
	private boolean fixedPowerSpectralDensity; // To enable or disable fixed power spectral density
	private double referenceBandwidthForPowerSpectralDensity; // Reference bandwidth for power spectral density
	
	private double polarizationModes; // Number of polarization modes
	
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
	public boolean isActiveASE() {
		return activeASE;
	}
	/**
	 * @param activeASE the activeAse to set
	 */
	public void setActiveASE(boolean activeASE) {
		this.activeASE = activeASE;
	}
	/**
	 * @return the activeNli
	 */
	public boolean isActiveNLI() {
		return activeNLI;
	}
	/**
	 * @param activeNLI the activeNli to set
	 */
	public void setActiveNLI(boolean activeNLI) {
		this.activeNLI = activeNLI;
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
	public double getRateOfFEC() {
		return rateOfFEC;
	}
	/**
	 * @param rateOfFEC the rateOfFEC to set
	 */
	public void setRateOfFEC(double rateOfFEC) {
		this.rateOfFEC = rateOfFEC;
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
	 * @return the typeOfAmplifierGain
	 */
	public int getTypeOfAmplifierGain() {
		return typeOfAmplifierGain;
	}
	/**
	 * @param typeOfAmplifierGain the typeOfAmplifierGain to set
	 */
	public void setTypeOfAmplifierGain(int typeOfAmplifierGain) {
		this.typeOfAmplifierGain = typeOfAmplifierGain;
	}
	/**
	 * @return the amplificationFrequency
	 */
	public double getAmplificationFrequency() {
		return amplificationFrequency;
	}
	/**
	 * @param amplificationFrequency the amplificationFrequency to set
	 */
	public void setAmplificationFrequency(double amplificationFrequency) {
		this.amplificationFrequency = amplificationFrequency;
	}
	/**
	 * @return the switchInsertionLoss
	 */
	public double getSwitchInsertionLoss() {
		return switchInsertionLoss;
	}
	/**
	 * @param switchInsertionLoss the switchInsertionLoss to set
	 */
	public void setSwitchInsertionLoss(double switchInsertionLoss) {
		this.switchInsertionLoss = switchInsertionLoss;
	}
	/**
	 * @return the fixedPowerSpectralDensity
	 */
	public boolean isFixedPowerSpectralDensity() {
		return fixedPowerSpectralDensity;
	}
	/**
	 * @param fixedPowerSpectralDensity the fixedPowerSpectralDensity to set
	 */
	public void setFixedPowerSpectralDensity(boolean fixedPowerSpectralDensity) {
		this.fixedPowerSpectralDensity = fixedPowerSpectralDensity;
	}
	/**
	 * @return the referenceBandwidthForPowerSpectralDensity
	 */
	public double getReferenceBandwidthForPowerSpectralDensity() {
		return referenceBandwidthForPowerSpectralDensity;
	}
	/**
	 * @param referenceBandwidthForPowerSpectralDensity the referenceBandwidthForPowerSpectralDensity to set
	 */
	public void setReferenceBandwidthForPowerSpectralDensity(double referenceBandwidthForPowerSpectralDensity) {
		this.referenceBandwidthForPowerSpectralDensity = referenceBandwidthForPowerSpectralDensity;
	}
	/**
	 * @return the polarizationModes
	 */
	public double getPolarizationModes() {
		return polarizationModes;
	}
	/**
	 * @param polarizationModes the polarizationModes to set
	 */
	public void setPolarizationModes(double polarizationModes) {
		this.polarizationModes = polarizationModes;
	}
}
