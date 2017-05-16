package grmlsa.integrated;

import network.Circuit;
import network.Mesh;


/**
 * Esta interface dever� ser implementada por algoritmos que resolvem o problema RSA de forma integrada.
 * @author Iallen
 *
 */
public interface IntegratedRSAAlgoritm {
	
	/**
	 * Este m�dodo deve estabelecer para uma determinada requisi��o uma rota e uma faixa de espectro e retornar true.
	 * Caso n�o seja poss�vel resolver o problema RSA o m�todo retornar� false;
	 * @param request
	 * @return
	 */
	public boolean rsa(Circuit request, Mesh mesh);

}
