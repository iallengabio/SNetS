package grmlsa.spectrumAssignment;

import java.util.List;

import network.Circuit;
import network.Link;


/**
 * Esta interface deverá ser implementada por classes de algoritmos de atribuição de espectro independentes de roteamento
 * @author Iallen
 *
 */
public interface SpectrumAssignmentAlgoritm {
	
	/**
	 * Este método atribui uma faixa de espectro e retorna true.
	 * Caso não seja possível fazer a atribuição deverá retornar false.
	 *
	 * @param numberOfSlots
	 * @param request
	 * @return
	 */
	public boolean assignSpectrum(int numberOfSlots, Circuit request);

}
