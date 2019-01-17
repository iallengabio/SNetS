package grmlsa.spectrumAssignment;

import java.util.List;
import java.util.Map;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 *  This class represents the spectrum allocation technique called DispersionAdaptiveFirstLastFit.
 *  Algorithm based on: Dispersion-adaptive first-last fit spectrum allocation scheme for 
 *                      elastic optical networks. (2016)
 *  
 *  DispersionAdaptiveFirstLastFit algorithm uses the beta parameter to choose between FisrtFit or LastFit.
 *  The beta parameter represents a distance in kilometers.
 *  The value of beta must be entered in the configuration file "others" as shown below.
 * {"variables":{
 *               "beta":"3000.0"
 *               }
 * }
 * 
 * @author Alexandre
 */
public class DispersionAdaptiveFirstLastFit implements SpectrumAssignmentAlgorithmInterface {

	private Double beta;
	
    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
        List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());

        int chosen[] = policy(numberOfSlots, composition, circuit, cp);
        circuit.setSpectrumAssigned(chosen);
        
        if (chosen == null)
        	return false;

        return true;
    }

	/**
	 * Applies the FirstFit policy to a certain list of free bands and returns the chosen band
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @return int[]
	 */
	public static int[] firstFit(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int chosen[] = null;
		for (int[] band : freeSpectrumBands) {
			if(band[1] - band[0] + 1 >= numberOfSlots){
				chosen = band.clone();
				chosen[1] = chosen[0] + numberOfSlots - 1;//it is not necessary to allocate the entire band, only the number of slots necessary
				
				if(aceitableDispersion(numberOfSlots, chosen, circuit, cp)) {
					break;
				}
			}
		}
		return chosen;
	}
	
	/**
	 * Applies the LastFit policy to a certain list of free bands and returns the chosen band
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @return int[]
	 */
	public static int[] lastFit(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		int chosen[] = null;
		int band[] = null;
		int i;
		for (i = freeSpectrumBands.size()-1; i >= 0; i--) {
			band = freeSpectrumBands.get(i);
			if(band[1] - band[0] + 1 >= numberOfSlots){
				chosen = band.clone();
				chosen[0] = chosen[1] - numberOfSlots + 1;//it is not necessary to allocate the entire band, only the number of slots necessary
				
				if(aceitableDispersion(numberOfSlots, chosen, circuit, cp)) {
					break;
				}
			}
		}
		return chosen;
	}
	
	/**
	 * Uses the largerBand parameter to choose between FirstFit or LastFit
	 * 
	 * @param numberOfSlots int
	 * @param livres List<int[]>
	 * @param circuit Circuit
	 * @return int[]
	 */
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		if(beta == null){
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			beta = Double.parseDouble((String)uv.get("beta"));
		}
		
		if (circuit.getRoute().getDistanceAllLinks() > beta) {
			return firstFit(numberOfSlots, freeSpectrumBands, circuit, cp);
			
		}else {
			return lastFit(numberOfSlots, freeSpectrumBands, circuit, cp);
		}
	}
	
	/**
	 * Checks the quality of transmission of the circuit using the spectrum band informed
	 * 
	 * @param numberOfSlots int
	 * @param band int[]
	 * @param circuit Circuit
	 * @param cp ControlPlane
	 * @return boolean
	 */
	public static boolean aceitableDispersion(int numberOfSlots, int[] band, Circuit circuit, ControlPlane cp) {
		circuit.setSpectrumAssigned(band);
		
		boolean QoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, circuit.getRoute(), circuit.getModulation(), band);
		return QoT;
	}
	
	public static boolean aceitableDispersion2(int numberOfSlots, int[] band, Circuit circuit, ControlPlane cp) {
		double limiteDispersion = 1.0;
		
		circuit.setSpectrumAssigned(band);
		
		double distance = circuit.getRoute().getDistanceAllLinks();
		double disperionTotal = computeDispersionRequest(circuit) * distance;
		double dispersionPerSlot = disperionTotal / numberOfSlots;
		
		boolean QoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, circuit.getRoute(), circuit.getModulation(), band);
		
		if(QoT && (dispersionPerSlot <= limiteDispersion)) {
			return true;
		}
		
		return false;
	}
	

	public static double computeDispersionRequest(Circuit circuit) {
		int sa[] = circuit.getSpectrumAssigned();
		double fs = circuit.getRoute().getLink(0).getSlotSpectrumBand();
		double numOfSlots = sa[1] - sa[0] + 1;
		double Bsj = numOfSlots * fs; //largura de banda da requisicao
		double lowerFrequency = 193.0E+12 - (fs * (400.0 / 2.0));
		
		double fj = lowerFrequency + (fs * (sa[0] - 1)) + (Bsj / 2); //frequencia central da requisicao
		double c = 299792458.0; //m/s, velocidade da luz;
		double lmabda0 = c / fj;
		double dispersion = computeDispersion(lmabda0);
		//System.out.println("lambda 0 = " + lmabda0 + ", dispersion = " + dispersion);
		
		return dispersion;
	}
	
	public static double computeDispersion(double lambda) {
		
		double c = 299792458.0; //m/s, velocidade da luz;
		double A = 25.0 * 1.0E-6; //micro m
		
		//A - Quenched SiO2
		double lambdaA = 1.284 * 1.0E-6; //micro m
		double aA[] = {0.696750, 0.408218, 0.890815};
		double bA[] = {0.069066 * 1.0E-6, 0.115662 * 1.0E-6, 9.900559 * 1.0E-6};
		
		//B - 13.5 GeO2:86.5 SiO2
		double lambdaB = 1.383 * 1.0E-6; //micro m
		double aB[] = {0.711040, 0.451885, 0.704048};
		double bB[] = {0.064270 * 1.0E-6, 0.1294086 * 1.0E-6, 9.425478 * 1.0E-6};
		
		double n1 = computeN(lambdaB, aB, bB);
		double n2 = computeN(lambdaA, aA, bA);
		
		double k0 = (2.0 * Math.PI) / lambda;
		double t1 = ((n1 * n1) - (n2 * n2));
		double v = k0 * A * Math.pow(t1, 0.5); //= k0 * A * Math.sqrt(t1);
		
		double Beta = (2.0 * Math.PI * computeN(lambdaB, aB, bB)) / lambda;
		//Beta = 4.0E-24 ;//ps
		
		double a2 = (lambda * lambda * v * v) / (4.0 * Math.PI * Math.PI * ((n1 * n1) - (Beta * Beta)));
		double u2 = a2 * (((4.0 * Math.PI * Math.PI) / (lambda * lambda)) * (n1 * n1) - (Beta * Beta));
		
		double Dml = Dmlambda(n1, lambda, c, aB, bB);
		double Dwdl = Dwdlambda(n1, n2, lambda, c, v, u2, aA, bA);
		double Dlambda = Dml + Dwdl;
		
		return Dlambda;
	}
	
	public static double computeN(double lambda, double b[], double a[]) {
		double sum = 0.0;
		for(int i = 0; i < 3; i++) {
			sum += (b[i] * lambda * lambda) / ((lambda * lambda) - (a[i] * a[i]));
		}
		double n = Math.sqrt(1.0 + sum);
		return n;
	}
	
	public static double Dmlambda(double n1, double lambda, double c, double a[], double b[]) {
		double d2lambda2 = (lambda / c) * d2njdlambd2(n1, lambda, a, b);
		return d2lambda2;
	}
	
	public static double Dwdlambda(double n1, double n2, double lambda, double c, double v, double u2, double a[], double b[]) {
		double dlambda = -1.0 * ((2.0 * (n1 - n2) * u2) / (c * lambda * v * v)) * (1.0 - (lambda / n2) * dnjdlambda(n2, lambda, a, b));
		return dlambda;
	}
	
	public static double dnjdlambda(double nj, double lambda, double a[], double b[]) {
		double sum = 0.0;
		for(int i = 0; i < 3; i++) {
			sum += (a[i] * a[i] * b[i]) / Math.pow(((lambda * lambda) - (a[i] * a[i])), 2);
		}
		
		double value = -1.0 * (lambda / nj) * sum;
		
		return value;
	}
	
	
	public static double d2njdlambd2(double nj, double lambda, double a[], double b[]) {
		double sum1 = 0.0;
		for(int i = 0; i < 3; i++) {
			sum1 += (a[i] * a[i] * b[i]) / Math.pow(((lambda * lambda) - (a[i] * a[i])), 2);
		}
		
		double sum2 = 0.0;
		for(int i = 0; i < 3; i++) {
			double p1 = (a[i] * a[i] * b[i]) / (Math.pow(((lambda * lambda) - (a[i] * a[i])), 2) * nj);
			double p2 = ((((a[i] * a[i]) + 3.0 * (lambda * lambda)) / ((lambda * lambda) - a[i] * a[i])) + (((lambda * lambda) * sum1) / (nj * nj)));
			sum2 += p1 * p2;
		}
		
		double value = -1.0 * sum2;
		
		return value;
	}
}
