package grmlsa.trafficGrooming;

import grmlsa.GRMLSA;
import request.RequestForConexion;

public interface TrafficGroomingAlgorithm {
	
	/**
	 * Este método deverá definir de acordo com as políticas adotadas pelo algoritmo qual/quais circuitos serão utilizados para fazer agregação de tráfego,
	 * Mesmo na ausência de circuitos para realizar a aglomeração os algoritmos podem requerer a criação de novos circuitos para atender a requisição.
	 * @param rfc
	 * @param mesh
	 * @return retorna falso se não for possível atender à requisição e verdadeira se for possível
	 */
	public boolean searchCircuitsForGrooming(RequestForConexion rfc, GRMLSA grmlsa);
	
	/**
	 * O algoritmo deve definir o que deve ser feito no fim de uma conexão.
	 * Ex: encerrar o circuito; manter o circuito; reduzir a quantidade de slots alocados, etc.
	 * @param rfc
	 * @param grmlsa
	 */
	public void finalizarConexao(RequestForConexion rfc, GRMLSA grmlsa);
}
