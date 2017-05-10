package grmlsa.trafficGrooming;

import grmlsa.GRMLSA;
import request.RequestForConexion;

public interface TrafficGroomingAlgorithm {
	
	/**
	 * Este m�todo dever� definir de acordo com as pol�ticas adotadas pelo algoritmo qual/quais circuitos ser�o utilizados para fazer agrega��o de tr�fego,
	 * Mesmo na aus�ncia de circuitos para realizar a aglomera��o os algoritmos podem requerer a cria��o de novos circuitos para atender a requisi��o.
	 * @param rfc
	 * @param mesh
	 * @return retorna falso se n�o for poss�vel atender � requisi��o e verdadeira se for poss�vel
	 */
	public boolean searchCircuitsForGrooming(RequestForConexion rfc, GRMLSA grmlsa);
	
	/**
	 * O algoritmo deve definir o que deve ser feito no fim de uma conex�o.
	 * Ex: encerrar o circuito; manter o circuito; reduzir a quantidade de slots alocados, etc.
	 * @param rfc
	 * @param grmlsa
	 */
	public void finalizarConexao(RequestForConexion rfc, GRMLSA grmlsa);
}
