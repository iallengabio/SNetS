package grmlsa.routing;

import network.Circuit;
import network.Mesh;


/**
 * Esta classe deverá ser implementada por algoritmos de roteamento independentes de alocação de espectro
 * @author Iallen
 *
 */
public interface RoutingInterface {
	
	/**
	 * Este método deve atribuir uma rota para uma determinada requisição e retornar true.
	 * Caso não seja possível encontrar uma rota o método deverá retornar false.
	 * @return
	 */
	public boolean findRoute(Circuit request, Mesh mesh);

}
