package grmlsa.integrated;

import network.Circuit;
import network.Mesh;


/**
 * Esta interface deverá ser implementada por algoritmos que resolvem o problema RSA de forma integrada.
 * @author Iallen
 *
 */
public interface IntegratedRSAAlgoritm {
	
	/**
	 * Este médodo deve estabelecer para uma determinada requisição uma rota e uma faixa de espectro e retornar true.
	 * Caso não seja possível resolver o problema RSA o método retornará false;
	 * @param request
	 * @return
	 */
	public boolean rsa(Circuit request, Mesh mesh);

}
