package grmlsa.integrated;

import network.Circuit;
import network.Mesh;

/**
 * Este algoritmo RSA utiliza um roteamento de K-menores caminhos combinado com uma política FirstFit para alocação de espectro
 * 
 * O caminho selecionado será aquele em que puder ser alocada a faixa de espectro mais próxima do slot 0
 * @author Iallen
 *
 */
public class Integrated implements IntegratedRMLSAAlgorithmInterface{

	@Override
	public boolean rsa(Circuit request, Mesh mesh) {
		throw new UnsupportedOperationException();
	}

}
