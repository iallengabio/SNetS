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
	private double f; // Optical signal frequency (Hz)
	private double position; // Position of the amplifier on the link (Km)
	
	private double gainLinear; // Linear gain
	private double noiseFigureLinear; // Linear noise figure
	private double saturationPowerLinear; // Linear saturation power (Watt)
	
	private int activeAse; // To enable or disable ASE
	private int typeGainAmplifier; // Set the amplifier gain type (0 = saturated gain, any other = fixed gain)
	
	private double Famp; // Amplifier noise factor
	private double A1; // Parameter for noise factor
	private double A2; // Parameter for noise factor
	
	/**
	 * Creates a new instance of Amplifier.
	 * 
	 * @param gain - Unsaturated gain
	 * @param saturationPower - Saturation power
	 * @param noiseFigure - Noise figure
	 * @param h - Constant of Planck
	 * @param f - Optical signal frequency
	 * @param position - Position of the amplifier on the link
	 * @param A1 - Parameter for noise factor
	 * @param A2 - Parameter for noise factor
	 */
	public Amplifier(double gain, double saturationPower, double noiseFigure, double h, double f, double position, double A1, double A2){
		this.gain = gain; //dB
		this.saturationPower = saturationPower; //dBm
		this.noiseFigure = noiseFigure; //dB
		this.h = h; //Js
		this.f = f; //Hz
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
	 * @return
	 */
	public double getGain() {
		return gain;
	}
	
	/**
	 * Sets the gain
	 * 
	 * @param gain
	 */
	public void setGain(double gain) {
		this.gain = gain;
		this.gainLinear = PhysicalLayer.ratioOfDB(gain);
	}
	
	/**
	 * Returns the saturation power
	 * 
	 * @return saturationPower
	 */
	public double getSaturationPower() {
		return saturationPower;
	}
	
	/**
	 * Sets the saturation power
	 * 
	 * @param saturationPower
	 */
	public void setSaturationPower(double saturationPower) {
		this.saturationPower = saturationPower; //dBm
		this.saturationPowerLinear = PhysicalLayer.ratioOfDB(saturationPower) * 1.0E-3; //Watt
	}
	
	/**
	 * Returns the noise figure
	 * 
	 * @return noiseFigure
	 */
	public double getNoiseFigure() {
		return noiseFigure;
	}
	
	/**
	 * Sets the noise figure
	 * 
	 * @param noiseFigure
	 */
	public void setNoiseFigure(double noiseFigure) {
		this.noiseFigure = noiseFigure;
		this.noiseFigureLinear = PhysicalLayer.ratioOfDB(noiseFigureLinear);
	}
	
	/**
	 * Returns the Constant of Planck
	 * 
	 * @return h
	 */
	public double getH() {
		return h;
	}
	
	/**
	 * Sets the Constant of Planck
	 * 
	 * @param h
	 */
	public void setH(double h) {
		this.h = h;
	}
	
	/**
	 * Returns the f
	 * 
	 * @return f
	 */
	public double getF() {
		return f;
	}
	
	/**
	 * Sets the f
	 * 
	 * @param f
	 */
	public void setF(double f) {
		this.f = f;
	}
	
	/**
	 * Returns the position
	 * 
	 * @return position
	 */
	public double getPosition() {
		return position;
	}
	
	/**
	 * Sets the position
	 *
	 * @param position
	 */
	public void setPosition(double position) {
		this.position = position;
	}
	
	/**
	 * Based on article: 
	 *  - OSNR model to consider physical layer impairments in transparent optical networks (2009)
	 * 
	 * @param gainSaturated, linear
	 * @param pin, linear
	 * @param frequency - double
	 * @return double - ASE linear
	 */
	public double getAseBySaturation(double frequency, double pinTotal){
		double gainSaturated = getGainSaturated(pinTotal);
		double famp = getFamp(pinTotal);
		double ase = 0.5 * h * frequency * famp * gainSaturated;
		return ase;
	}
	
	/**
	 * Based on articles: 
	 *   - Closed-form expressions for nonlinear transmission performance of densely spaced coherent optical OFDM systems (2010)
	 *   - A Quality-of-Transmission Aware Dynamic Routing and Spectrum Assignment Scheme for Future Elastic Optical Networks (2013)
	 *   
	 * @param gain, linear
	 * @param frequency
	 * @return double - ASE linear
	 */
	public double getAse(double frequency){
		double ase = 0.5 * h * frequency * noiseFigureLinear * (gainLinear - 1.0);
		return ase;
	}
	
	/**
	 * Based on article: 
	 *  - OSNR model to consider physical layer impairments in transparent optical networks (2009)
	 *  
	 * @param Pin - linear
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
	 * @param pinTotal - Total input power, value in Watt
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
	 * Method toString to override the toString of the Object class
	 * 
	 * @return String
	 */
	public String toString(){
		return "gain = " + this.gain + ", saturaionPower = " + this.saturationPower +", noiseFigure = " + this.noiseFigure + ", h = " + this.h + ", f = " + this.f + ", position = " + this.position;
	}
	
	/**
	 * Returns if the ASE is active or not
	 * 
	 * @return int
	 */
	public int getActiveAse() {
		return activeAse;
	}
	
	/**
	 * Sets is the ASE is active or not
	 * 
	 * @param activeAse
	 */
	public void setActiveAse(int activeAse) {
		this.activeAse = activeAse;
	}
	
	/**
	 * Returns the type of gain of the amplifier
	 * 
	 * @return typeGainAmplifier
	 */
	public int getTypeGainAmplifier() {
		return typeGainAmplifier;
	}
	
	/**
	 * Sets the amplifier gain type
	 * 
	 * @param typeGainAmplifier
	 */
	public void setTypeGainAmplifier(int typeGainAmplifier) {
		this.typeGainAmplifier = typeGainAmplifier;
	}
	
	/**
	 * Returns the value of gain of the amplifier according to gain type of the amplifier
	 * 
	 * @param pinTotal - Total power at the amplifier input
	 * @return double - amplifier gain
	 */
	public double getGainByType(double pinTotal){
		double gain = 0.0;
		
		if(typeGainAmplifier == 0){ // Saturated gain of the amplifier
			gain = this.getGainSaturated(pinTotal); 
			
		} else { // Fixed gain of the amplifier
			gain = this.getGainLinear();
		}
		
		return gain;
	}
	
	/**
	 * Returns the ASE noise value according to the gain type of the amplifier
	 * 
	 * @param pinTotal - Total power at the amplifier input
	 * @param frequency - Signal frequency
	 * @return double - ASE
	 */
	public double getAseByTypeGain(double pinTotal, double frequency){
		double aseNoise = 0.0;
		
		if(activeAse == 1){
			if(typeGainAmplifier == 0){ // Saturated gain of the amplifier
				aseNoise = this.getAseBySaturation(frequency, pinTotal);
				
			}else{ // Fixed gain of the amplifier
				aseNoise = this.getAse(frequency);
			}
		}
		
		return aseNoise;
	}
	
	/**
	 * Returns the gain linear
	 * 
	 * @return gainLinear - double
	 */
	public double getGainLinear() {
		return gainLinear;
	}
	
	/**
	 * Sets the gain linear
	 * 
	 * @param gainLinear - double
	 */
	public void setGainLinear(double gainLinear) {
		this.gainLinear = gainLinear;
	}
	
	/**
	 * Returns the noise figure linear
	 * 
	 * @return noiseFigureLinear
	 */
	public double getNoiseFigureLinear() {
		return noiseFigureLinear;
	}
	
	/**
	 * Sets the noise figure linear
	 * 
	 * @param noiseFigureLinear
	 */
	public void setNoiseFigureLinear(double noiseFigureLinear) {
		this.noiseFigureLinear = noiseFigureLinear;
	}
	
	/**
	 * Returns the famp
	 * 
	 * @return the famp
	 */
	public double getFamp() {
		return Famp;
	}
	
	/**
	 * Sets the famp
	 * 
	 * @param famp the famp to set
	 */
	public void setFamp(double famp) {
		Famp = famp;
	}
}
