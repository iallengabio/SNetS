package grmlsa.spectrumAssignment;

import java.util.List;

import network.Circuit;
import network.Link;


/**
 * Esta interface dever� ser implementada por classes de algoritmos de atribui��o de espectro independentes de roteamento
 * @author Iallen
 *
 */
public interface SpectrumAssignmentAlgoritm {
	
	/**
	 * Este m�todo atribui uma faixa de espectro e retorna true.
	 * Caso n�o seja poss�vel fazer a atribui��o dever� retornar false.
	 *
	 * @param numberOfSlots
	 * @param request
	 * @return
	 */
	public boolean assignSpectrum(int numberOfSlots, Circuit request);

}
