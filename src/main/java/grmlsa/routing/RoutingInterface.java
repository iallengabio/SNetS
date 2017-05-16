package grmlsa.routing;

import network.Circuit;
import network.Mesh;


/**
 * Esta classe dever� ser implementada por algoritmos de roteamento independentes de aloca��o de espectro
 * @author Iallen
 *
 */
public interface RoutingInterface {
	
	/**
	 * Este m�todo deve atribuir uma rota para uma determinada requisi��o e retornar true.
	 * Caso n�o seja poss�vel encontrar uma rota o m�todo dever� retornar false.
	 * @return
	 */
	public boolean findRoute(Circuit request, Mesh mesh);

}
