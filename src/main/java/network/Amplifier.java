package network;

import java.io.Serializable;

/**
 * 
 * 
 * @author Alexandre
 */
public class Amplifier implements Serializable {

	private double gain; //Ganho nao saturado do amplificador em dB
	private double saturationPower; //Potencia de saturacao em dBm
	private double noiseFigure; //Figura de ruido em dB
	private double h; //Constante de Planck (Js)
	private double f; //frequencia optica do sinal (Hz)
	private double B0; //largura de banda optica
	private double position; //posicao do amplificador no enlace (km)
	
	private double gainLinear; //Ganho linear
	private double noiseFigureLinear; //Figura de ruido linear
	private double saturationPowerLinear; //Potencia de saturacao linear, Watt
	
	//para ativar ou desativar o ASE
	private int activeAse;
	//definir o tipo do ganho do amplificadoe (0 = ganho saturado, qualquer outro = ganho fixo)
	private int typeGainAmplifier;
	
	private double Famp; //fator de ruido do amplificador
	private double A1; //parametro para o fator de ruido
	private double A2; //parametro para o fator de ruido
	
	/**
	 * Construtor
	 * @param gain - ganho nao saturado
	 * @param saturationPower - potencia de saturacao
	 * @param noiseFigure - figura de ruido
	 * @param h - Constante de Planck
	 * @param f - frequencia optica do sinal
	 * @param B0 - largura de banda optica
	 * @param position - posicao do amplificador no enlace
	 * @param numberWave - quantidade de comprimentos de onda
	 */
	public Amplifier(double gain, double saturationPower, double noiseFigure, double h, double f, double B0, double position, double A1, double A2){
		this.gain = gain; //dB
		this.saturationPower = saturationPower; //dBm
		this.noiseFigure = noiseFigure; //dB
		this.h = h; //Js
		this.f = f; //Hz
		this.B0 = B0; //Hz
		this.position = position; //km
		this.A1 = A1;
		this.A2 = A2;
		
        this.gainLinear = ComputeQoT.ratioOfDB(gain);
        this.noiseFigureLinear = ComputeQoT.ratioOfDB(noiseFigure);
        this.saturationPowerLinear = ComputeQoT.ratioOfDB(saturationPower) * 1.0E-3; //convertendo para Watt
	}
	/**
	 * Retorna o ganho
	 * @return
	 */
	public double getGain() {
		return gain;
	}
	/**
	 * Configura o ganho
	 * @param gain
	 */
	public void setGain(double gain) {
		this.gain = gain;
		this.gainLinear = ComputeQoT.ratioOfDB(gain);
	}
	/**
	 * Retorna a potencia de saturacao
	 * @return
	 */
	public double getSaturationPower() {
		return saturationPower;
	}
	/**
	 * Configura a potencia de saturacao
	 * @param saturationPower
	 */
	public void setSaturationPower(double saturationPower) {
		this.saturationPower = saturationPower; //dBm
		this.saturationPowerLinear = ComputeQoT.ratioOfDB(saturationPower) * 1.0E-3; //Watt
	}
	/**
	 * Retorna a figura de ruido
	 * @return
	 */
	public double getNoiseFigure() {
		return noiseFigure;
	}
	/**
	 * Configura a figura de ruido
	 * @param noiseFigure
	 */
	public void setNoiseFigure(double noiseFigure) {
		this.noiseFigure = noiseFigure;
		this.noiseFigureLinear = ComputeQoT.ratioOfDB(noiseFigureLinear);
	}
	/**
	 * Retorna a Constante de Planck
	 * @return
	 */
	public double getH() {
		return h;
	}
	/**
	 * Configura a Constante de Planck
	 * @param h
	 */
	public void setH(double h) {
		this.h = h;
	}
	/**
	 * Retorna a frequencia do sinal
	 * @return
	 */
	public double getF() {
		return f;
	}
	/**
	 * Configura a frequencia do sinal
	 * @param f
	 */
	public void setF(double f) {
		this.f = f;
	}
	/**
	 * Retorna a largura de banda optica
	 * @return
	 */
	public double getB0() {
		return B0;
	}
	/**
	 * Configura a largura de banda optica
	 * @param b0
	 */
	public void setB0(double b0) {
		B0 = b0;
	}
	/**
	 * Retorna a posicao do amplificador no enlace
	 * @return
	 */
	public double getPosition() {
		return position;
	}
	/**
	 * Configura a posicao do amplificador no enlace
	 * @param position
	 */
	public void setPosition(double position) {
		this.position = position;
	}
	
	/**
	 * Referencia: artigo "OSNR model to consider physical layer impairments in transparent optical networks", 2009
	 * @param gainSaturated, linear
	 * @param pin, linear
	 * @param frequency - double - frequencia Optica do comprimento de onda
	 * @return double - ase linear
	 */
	public double getAseBySaturation(double frequency, double pinTotal){
		double gainSaturated = getGainSaturated(pinTotal);
		double famp = getFamp(pinTotal);
		double ase = 0.5 * B0 * h * frequency * famp * gainSaturated;
		return ase;
	}
	/**
	 * Referencia: 
	 *   - Closed-form expressions for nonlinear transmission performance of densely spaced coherent optical OFDM systems (2010)
	 *   - A Quality-of-Transmission Aware Dynamic Routing and Spectrum Assignment Scheme for Future Elastic Optical Networks (2013) 
	 * @param gain, linear
	 * @param frequency
	 * @return double - ase linear
	 */
	public double getAse(double frequency){
		double ase = 0.5 * B0 * h * frequency * noiseFigureLinear * (gainLinear - 1.0);
		return ase;
	}
	
	/**
	 * Referencia: artigo "OSNR model to consider physical layer impairments in transparent optical networks", 2009
	 * @param Pin - linear
	 * @return double - linear
	 */
	public double getFamp(double pin){
		Famp = noiseFigureLinear * (1.0 + A1 - (A1 / (1.0 + (pin / A2))));
		return Famp;
	}
	/**
	 * Metodo que calcula o ganho saturado em relacao a potencia de entrada total
	 * Referencia: artigo "OSNR model to consider physical layer impairments in transparent optical networks", 2009
	 * @param pinTotal - potencia total de entrada, valor em Watt
	 * @return double - ganho saturado, valor linear
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
	 * Metodo toString para sobrepor o toString da classe Object
	 */
	public String toString(){
		return "gain = " + this.gain + ", saturaionPower = " + this.saturationPower +", noiseFigure = " + this.noiseFigure + ", h = " + this.h + ", f = " + this.f + ", B0 = " + this.B0 + ", position = " + this.position;
	}
	/**
	 * Retorna a ativacao do ASE
	 * @return int
	 */
	public int getActiveAse() {
		return activeAse;
	}
	/**
	 * Configura a ativacao do ASE
	 * @param activeAse
	 */
	public void setActiveAse(int activeAse) {
		this.activeAse = activeAse;
	}
	/**
	 * Retorna o tipo de ganho do amplificador
	 * @return int
	 */
	public int getTypeGainAmplifier() {
		return typeGainAmplifier;
	}
	/**
	 * Configura o tipo de ganho do amplificador
	 * @param typeGainAmplifier
	 */
	public void setTypeGainAmplifier(int typeGainAmplifier) {
		this.typeGainAmplifier = typeGainAmplifier;
	}
	/**
	 * Retorna o valor do ganho do amplificador de acordo com o tipo de ganho do amplificador
	 * @param pinTotal - potencia total na entrada do amplificador
	 * @return double - ganho do amplificador
	 */
	public double getGainByType(double pinTotal){
		double gain = 0.0;
		
		if(typeGainAmplifier == 0){ //ganho saturado do amplificador
			gain = this.getGainSaturated(pinTotal); 
		} else { //ganho fixo do amplificador
			gain = this.getGainLinear();
		}
		
		return gain;
	}
	/**
	 * Retorna o valor do ruido ASE de acordo com o tipo de ganho do amplificador
	 * @param pinTotal - potencia total na entrada do amplificador
	 * @param frequency - frequencia do sinal
	 * @return double - ASE
	 */
	public double getAseByTypeGain(double pinTotal, double frequency){
		double aseNoise = 0.0;
		
		if(activeAse == 1){
			if(typeGainAmplifier == 0){ //ganho saturado do amplificador
				aseNoise = this.getAseBySaturation(frequency, pinTotal);
			}else{ //ganho fixo do amplificador
				aseNoise = this.getAse(frequency);
			}
		}
		
		return aseNoise;
	}
	/**
	 * Retorna o valor linear do ganho do amplificador
	 * @return gainLinear - double
	 */
	public double getGainLinear() {
		return gainLinear;
	}
	/**
	 * Seta o valor linear do ganho do amplificador 
	 * @param gainLinear - double
	 */
	public void setGainLinear(double gainLinear) {
		this.gainLinear = gainLinear;
	}
	/**
	 * Retorna o valor linear da figura de ruido do amplificador
	 * @return
	 */
	public double getNoiseFigureLinear() {
		return noiseFigureLinear;
	}
	/**
	 * Seta o valor linear da figura de ruido do amplificador
	 * @param noiseFigureLinear
	 */
	public void setNoiseFigureLinear(double noiseFigureLinear) {
		this.noiseFigureLinear = noiseFigureLinear;
	}
	/**
	 * @return the famp
	 */
	public double getFamp() {
		return Famp;
	}
	/**
	 * @param famp the famp to set
	 */
	public void setFamp(double famp) {
		Famp = famp;
	}
}
