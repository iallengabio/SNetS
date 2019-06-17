package network;

import java.io.Serializable;

/**
 * This class represents an optical amplifier in the network.
 * 
 * @author Alexandre
 */
public class Amplifier implements Serializable {

	private double gain; // Unsaturated gain of amplifier in dB
	private double saturationPower; // Saturation power in dBm
	private double noiseFigure; // Noise figure in dB
	private double h; // Constant of Planck (Js)
	private double frequency; // Optical signal frequency (Hz)
	private double position; // Position of the amplifier on the link (Km)
	
	private double gainLinear; // Linear gain
	private double noiseFigureLinear; // Linear noise figure
	private double saturationPowerLinear; // Linear saturation power (Watt)
	
	private double Famp; // Amplifier noise factor
	private double A1; // Parameter for noise factor
	private double A2; // Parameter for noise factor
	
	/**
	 * Creates a new instance of Amplifier.
	 * 
	 * @param gain double - Unsaturated gain
	 * @param saturationPower double - Saturation power
	 * @param noiseFigure double - Noise figure
	 * @param h double - Constant of Planck
	 * @param frequency double - Optical signal frequency
	 * @param position double - Position of the amplifier on the link
	 * @param A1 double - Parameter for noise factor
	 * @param A2 double - Parameter for noise factor
	 */
	public Amplifier(double gain, double saturationPower, double noiseFigure, double h, double frequency, double position, double A1, double A2){
		this.gain = gain; //dB
		this.saturationPower = saturationPower; //dBm
		this.noiseFigure = noiseFigure; //dB
		this.h = h; //Js
		this.frequency = frequency; //Hz
		this.position = position; //Km
		this.A1 = A1;
		this.A2 = A2;
		
        this.gainLinear = PhysicalLayer.ratioOfDB(gain);
        this.noiseFigureLinear = PhysicalLayer.ratioOfDB(noiseFigure);
        this.saturationPowerLinear = PhysicalLayer.ratioOfDB(saturationPower) * 1.0E-3; //convertendo para Watt
	}
	
	/**
	 * Returns the gain
	 * 
	 * @return double gain
	 */
	public double getGain() {
		return gain;
	}
	
	/**
	 * Sets the gain
	 * 
	 * @param gain double
	 */
	public void setGain(double gain) {
		this.gain = gain;
		this.gainLinear = PhysicalLayer.ratioOfDB(gain);
	}
	
	/**
	 * Returns the saturation power
	 * 
	 * @return double saturationPower
	 */
	public double getSaturationPower() {
		return saturationPower;
	}
	
	/**
	 * Sets the saturation power
	 * 
	 * @param saturationPower double
	 */
	public void setSaturationPower(double saturationPower) {
		this.saturationPower = saturationPower; //dBm
		this.saturationPowerLinear = PhysicalLayer.ratioOfDB(saturationPower) * 1.0E-3; //Watt
	}
	
	/**
	 * Returns the noise figure
	 * 
	 * @return double noiseFigure
	 */
	public double getNoiseFigure() {
		return noiseFigure;
	}
	
	/**
	 * Sets the noise figure
	 * 
	 * @param noiseFigure double
	 */
	public void setNoiseFigure(double noiseFigure) {
		this.noiseFigure = noiseFigure;
		this.noiseFigureLinear = PhysicalLayer.ratioOfDB(noiseFigureLinear);
	}
	
	/**
	 * Returns the Constant of Planck
	 * 
	 * @return double h
	 */
	public double getH() {
		return h;
	}
	
	/**
	 * Sets the Constant of Planck
	 * 
	 * @param h double
	 */
	public void setH(double h) {
		this.h = h;
	}
	
	/**
	 * Returns the frequency
	 * 
	 * @return double frequency
	 */
	public double getFrequency() {
		return frequency;
	}
	
	/**
	 * Sets the frequency
	 * 
	 * @param frequency double
	 */
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * Returns the position
	 * 
	 * @return double position
	 */
	public double getPosition() {
		return position;
	}
	
	/**
	 * Sets the position
	 *
	 * @param position double
	 */
	public void setPosition(double position) {
		this.position = position;
	}
	
	/**
	 * Based on article: 
	 *  - OSNR model to consider physical layer impairments in transparent optical networks (2009)
	 * 
	 * @param pinTotal double - linear
	 * @return double - ASE linear
	 */
	public double getAseBySaturation(double pinTotal){
		double gainSaturated = getGainSaturated(pinTotal);
		double famp = getFamp(pinTotal);
		double ase = 0.5 * h * frequency * famp * gainSaturated;
		return ase;
	}
	
	/**
	 * Based on articles: 
	 *   - Closed-form expressions for nonlinear transmission performance of densely spaced coherent optical OFDM systems (2010)
	 *   - A Quality-of-Transmission Aware Dynamic Routing and Spectrum Assignment Scheme for Future Elastic Optical Networks (2013)
	 *   - Power, routing, modulation level and spectrum assignment in all-optical and elastic networks (2019)
	 * 
	 * @return double - ASE linear
	 */
	public double getAse(){
		double ase = h * frequency * noiseFigureLinear * (gainLinear - 1.0);
		return ase;
	}
	
	/**
	 * Based on article: 
	 *  - OSNR model to consider physical layer impairments in transparent optical networks (2009)
	 *  
	 * @param pin double - linear
	 * @return double - linear
	 */
	public double getFamp(double pin){
		Famp = noiseFigureLinear * (1.0 + A1 - (A1 / (1.0 + (pin / A2))));
		return Famp;
	}
	
	/**
	 * Method that calculates the saturated gain in relation to the total input power
	 * Based on article: 
	 *  - OSNR model to consider physical layer impairments in transparent optical networks (2009)
	 * 
	 * @param pinTotal double - Total input power, value in Watt
	 * @return double - Saturated gain, linear value
	 */
	public double getGainSaturated(double pinTotal){
		double Pout = gainLinear * pinTotal;
		double g = gainLinear / (1.0 + (Pout / saturationPowerLinear));
		if(g < 1.0){
			g = 1.0;
		}
		return g;
	}
	
	/**
	 * A method that calculates the gain in relation to an input power and verifying the saturation power
	 * 
	 * @param pinTotal double
	 * @return double
	 */
	public double getSaturatedGainByPsat(double totalPin){
		double g = gainLinear;
		//double Pout = totalPin * g;
		double Pout = gain + PhysicalLayer.ratioForDB(totalPin / 1.0E-3); //dBm
		
		if(Pout > saturationPower){
			g = getGainSaturated(totalPin);
		}
		
		return g;
	}
	
	/**
	 * Method toString to override the toString of the Object class
	 * 
	 * @return String
	 */
	public String toString(){
		return "gain = " + this.gain + ", saturaionPower = " + this.saturationPower +", noiseFigure = " + this.noiseFigure + ", h = " + this.h + ", frequency = " + this.frequency + ", position = " + this.position;
	}
	
	/**
	 * Returns the value of gain of the amplifier according to gain type of the amplifier
	 * 
	 * @param pinTotal double - Total power at the amplifier input
	 * @param typeOfAmplifierGain int - Type of amplifier gain
	 * @return double - amplifier gain
	 */
	public double getGainByType(double pinTotal, int typeOfAmplifierGain){
		double gain = 0.0;
		
		if(typeOfAmplifierGain == 0){ // Fixed gain of the amplifier
			gain = this.getGainLinear();
			
		} else { // Saturated gain of the amplifier
			gain = this.getSaturatedGainByPsat(pinTotal);
		}
		
		return gain;
	}
	
	/**
	 * Returns the ASE noise value according to the gain type of the amplifier
	 * 
	 * @param pinTotal double - Total power at the amplifier input
	 * @param gain double - Amplifier gain
	 * @return double - ASE
	 */
	public double getAseByGain(double pinTotal, double gain){
		double aseNoise = 0.0;
		
		if(gain == gainLinear){
			aseNoise = this.getAse();
		}else{
			aseNoise = this.getAseBySaturation(pinTotal);
		}
		
		return aseNoise;
	}
	
	/**
	 * Returns the gain linear
	 * 
	 * @return double gainLinear
	 */
	public double getGainLinear() {
		return gainLinear;
	}
	
	/**
	 * Sets the gain linear
	 * 
	 * @param gainLinear double
	 */
	public void setGainLinear(double gainLinear) {
		this.gainLinear = gainLinear;
	}
	
	/**
	 * Returns the noise figure linear
	 * 
	 * @return double noiseFigureLinear
	 */
	public double getNoiseFigureLinear() {
		return noiseFigureLinear;
	}
	
	/**
	 * Sets the noise figure linear
	 * 
	 * @param noiseFigureLinear double
	 */
	public void setNoiseFigureLinear(double noiseFigureLinear) {
		this.noiseFigureLinear = noiseFigureLinear;
	}
	
	/**
	 * Returns the famp
	 * 
	 * @return double the famp
	 */
	public double getFamp() {
		return Famp;
	}
	
	/**
	 * Sets the famp
	 * 
	 * @param famp double - the famp to set
	 */
	public void setFamp(double famp) {
		Famp = famp;
	}
}
